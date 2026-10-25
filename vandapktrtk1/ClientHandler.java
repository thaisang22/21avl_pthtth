import java.io.*;
import java.net.*;

/**
 * ============================================================================
 * LỚP CLIENTHANDLER: Xử lý toàn bộ logic giao tiếp với 1 Client trên 1 Thread riêng
 * ============================================================================
 * Kế thừa lớp Thread (hoặc implement Runnable) để hỗ trợ đa nhiệm (Multi-threading).
 * Nhờ đó, Server có thể phục vụ đồng thời hàng chục/hàng trăm người dùng cùng lúc
 * mà không bị gián đoạn hay phải chờ người khác hoàn thành (Non-blocking giữa các client).
 */
public class ClientHandler extends Thread {
    // Kênh truyền Socket kết nối trực tiếp với máy Client
    private Socket socket;

    // Bộ đệm đọc văn bản từ Client gửi lên (nhận theo từng dòng qua readLine())
    private BufferedReader in;

    // Bộ ghi dữ liệu gửi văn bản từ Server về Client (autoFlush = true để đẩy dữ liệu đi ngay lập tức)
    private PrintWriter out;

    // Tên (Nickname) của người dùng
    public String name;

    // Tên phòng hiện tại mà người dùng đang tham gia (null nếu đang ở sảnh / menu chọn phòng)
    public String currentRoom;

    /**
     * Hàm dựng nhận Socket từ server.accept() bàn giao sang
     * @param socket Đối tượng Socket đã thiết lập kết nối TCP
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Phương thức gửi tin nhắn văn bản từ Server về lại máy Client này
     * @param msg Chuỗi tin nhắn cần gửi
     */
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    /**
     * PHƯƠNG THỨC RUN(): Điểm thực thi chính của Thread.
     * Tự động được gọi khi Server thực thi lệnh client.start().
     */
    @Override
    public void run() {
        try {
            // Khởi tạo luồng Đọc/Ghi dữ liệu với bảng mã chuẩn UTF-8 để hỗ trợ Tiếng Việt có dấu
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // =========================================================================
            // GIAI ĐOẠN 1: ĐĂNG KÝ VÀ XÁC THỰC TÊN NGƯỜI DÙNG (NICKNAME)
            // =========================================================================
            out.println("Nhap ten: ");
            while (true) {
                name = in.readLine();
                // Trường hợp Client đột ngột đóng ứng dụng (readLine trả về null)
                if (name == null) {
                    return;
                }
                name = name.trim();

                // Người dùng gõ /exit để thoát ngay từ bước nhập tên
                if (name.equalsIgnoreCase("/exit")) {
                    return;
                }

                // Không cho phép nhập tên rỗng hoặc toàn dấu cách
                if (name.isEmpty()) {
                    out.println("Nhap ten: ");
                    continue;
                }

                // Kiểm tra trùng tên với các Client khác đang online
                boolean trung = false;
                for (ClientHandler c : ChatServer.clients) {
                    // Loại trừ chính mình (c != this) và kiểm tra so khớp không phân biệt hoa thường
                    if (c != this && c.name != null && c.name.equalsIgnoreCase(name)) {
                        trung = true;
                        break;
                    }
                }
                if (trung) {
                    out.println("Ten da duoc su dung, nhap lai: ");
                    continue;
                }

                // Tên hợp lệ -> Thoát vòng lặp nhập tên
                break;
            }

            // Ghi nhận trạng thái trên Server và gửi lời chào mừng Client
            System.out.println(name + " da ket noi");
            out.println("Xin chao " + name + "!\n");

            // =========================================================================
            // GIAI ĐOẠN 2: MENU CHỌN PHÒNG & QUẢN LÝ PHÒNG CHAT
            // =========================================================================
            while (true) {
                // Hiển thị danh sách phòng và số lượng người hiện tại trong mỗi phòng
                out.println("Danh sach phong:");
                for (int i = 0; i < ChatServer.rooms.size(); i++) {
                    String r = ChatServer.rooms.get(i);
                    int count = ChatServer.countMembers(r);
                    out.println((i + 1) + ". " + r + " (" + count + " nguoi)");
                }
                out.println();
                out.println("Go /quit de thoat phong || go /exit de thoat khoi chuong trinh");
                out.println("Chon phong: ");

                // Đọc lựa chọn của người dùng
                String line = in.readLine();
                if (line == null || line.trim().equalsIgnoreCase("/exit")) {
                    break; // Thoát khỏi hệ thống
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // --- Xử lý chọn phòng theo 3 cơ chế ---
                String selectedRoom = null;

                // Cơ chế 1: Nhập Số thứ tự phòng (1, 2, 3...)
                try {
                    int index = Integer.parseInt(line);
                    if (index >= 1 && index <= ChatServer.rooms.size()) {
                        selectedRoom = ChatServer.rooms.get(index - 1);
                    }
                } catch (Exception e) {
                    // Không phải số nguyên, bỏ qua để kiểm tra theo tên
                }

                // Cơ chế 2: Nhập đúng Tên phòng đã có (vd: "Study", "Game")
                if (selectedRoom == null) {
                    for (String r : ChatServer.rooms) {
                        if (r.equalsIgnoreCase(line)) {
                            selectedRoom = r;
                            break;
                        }
                    }
                }

                // Cơ chế 3: Tên phòng chưa hề tồn tại -> TỰ ĐỘNG TẠO PHÒNG MỚI
                if (selectedRoom == null) {
                    selectedRoom = line;
                    ChatServer.rooms.add(selectedRoom);
                    System.out.println("Da tao phong: " + selectedRoom);
                }

                // Gán phòng hiện tại cho client
                currentRoom = selectedRoom;
                System.out.println(name + " vao phong " + currentRoom);
                out.println("Ban da vao phong " + currentRoom + ".");

                // Thông báo tới tất cả người đang trong phòng rằng có người mới tham gia
                ChatServer.broadcast(currentRoom, name + " da tham gia phong.");

                // =====================================================================
                // GIAI ĐOẠN 3: VÒNG LẶP TRAO ĐỔI TIN NHẮN (CHAT LOOP TRONG PHÒNG)
                // =====================================================================
                boolean quit = false;
                String msg;
                while ((msg = in.readLine()) != null) {
                    msg = msg.trim();

                    // LỆNH 1: /quit -> Rời khỏi phòng chat hiện tại để quay lại Menu chọn phòng
                    if (msg.equalsIgnoreCase("/quit")) {
                        System.out.println(name + " da roi phong " + currentRoom);
                        // Thông báo cho các thành viên khác trong phòng (trừ người vừa rời)
                        ChatServer.broadcast(currentRoom, name + " da roi phong " + currentRoom + ".", this);
                        out.println("Ban da roi phong " + currentRoom + ".\n");
                        currentRoom = null;
                        quit = true; // Đánh dấu chỉ rời phòng, tiếp tục vòng lặp Menu chọn phòng
                        break;
                    }

                    // LỆNH 2: /exit -> Thoát hoàn toàn khỏi chương trình
                    if (msg.equalsIgnoreCase("/exit")) {
                        System.out.println(name + " da roi phong " + currentRoom);
                        ChatServer.broadcast(currentRoom, name + " da roi phong " + currentRoom + ".", this);
                        currentRoom = null;
                        break; // Thoát vòng lặp chat (quit = false) -> kết thúc luôn Menu
                    }

                    // TIN NHẮN CHAT BÌNH THƯỜNG: Phát sóng tới toàn bộ người trong phòng
                    if (!msg.isEmpty()) {
                        ChatServer.broadcast(currentRoom, "[" + name + "]: " + msg);
                    }
                }

                // Nếu người dùng chọn /exit (quit == false) hoặc ngắt kết nối mạng -> thoát hẳn
                if (!quit) {
                    break;
                }
            }

        } catch (Exception e) {
            // Bắt ngoại lệ nếu client mất kết nối đột ngột
        } finally {
            // =========================================================================
            // GIAI ĐOẠN 4: DỌN DẸP TÀI NGUYÊN (CLEANUP) - CÂU HỎI VẤN ĐÁP KINH ĐIỂN
            // =========================================================================
            try {
                // 1. Nếu client thoát khi còn đang trong phòng chat -> phát thông báo rời phòng
                if (currentRoom != null) {
                    System.out.println(name + " da roi phong " + currentRoom);
                    ChatServer.broadcast(currentRoom, name + " da roi phong " + currentRoom + ".", this);
                    currentRoom = null;
                }

                // 2. Xóa đối tượng client khỏi danh sách quản lý của Server
                // (Nếu không xóa: tin nhắn broadcast vẫn cố gửi tới socket đã chết -> lỗi rò rỉ bộ nhớ)
                ChatServer.clients.remove(this);

                // 3. Đóng Socket để giải phóng cổng kết nối và luồng I/O của hệ điều hành
                socket.close();

                if (name != null) {
                    System.out.println(name + " da ngat ket noi");
                }
            } catch (Exception e) {
            }
        }
    }
}
