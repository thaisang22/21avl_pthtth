import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * =========================================================================
 * BỘ KHUNG CLIENT TCP TOÀN DIỆN THI GIỮA KỲ (FIT - IUH)
 * -------------------------------------------------------------------------
 * KIẾN TRÚC 2 LUỒNG (MULTI-THREADING):
 * 1. LUỒNG PHỤ (receiverThread): Chạy ngầm (Daemon) chuyên đón nhận dữ liệu từ
 *    Server gửi về (kết quả tính toán, tin nhắn broadcast, thông báo hệ thống)
 *    và in ra màn hình mà không chặn bàn phím.
 * 2. LUỒNG CHÍNH (Main Thread): Lắng nghe người dùng gõ từ bàn phím và gửi lên Server.
 *
 * TÍNH NĂNG ĐẶC BIỆT:
 * - Tự động hiển thị Menu hướng dẫn khi vừa kết nối thành công.
 * - Tự chuyển đổi Prompt: "Client > " (ở ngoài Menu) và "[ChatRoom] > " (khi trong phòng).
 * - Tự kích hoạt chế độ nhập nhiều dòng kết thúc bằng dấu "." khi chọn 1, 2, 3, 4 theo đề 7000.
 * =========================================================================
 */
public class ClientTCP {
    // Địa chỉ IP/Host và Port của Server cần kết nối
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7000;

    /**
     * In Menu tổng quan hướng dẫn cú pháp cho sinh viên dễ nhớ trong phòng thi.
     */
    public static void inMenuClient() {
        System.out.println("\n=====================================================================");
        System.out.println("                 MENU HƯỚNG DẪN DỊCH VỤ CLIENT TCP");
        System.out.println("=====================================================================");
        System.out.println(" • Bài 2: Đổi số (0-9)           : DIGIT <chữ_số> (hoặc gõ thẳng: 5)");
        System.out.println(" • Bài 3: Xem ngày giờ           : DATE | TIME | DATETIME");
        System.out.println(" • Bài 4: Máy tính từ xa         : CALC <toán_tử> <số_1> <số_2>");
        System.out.println("                                   (Ví dụ: CALC + 10 20 hoặc CALC / 10 0)");
        System.out.println(" • Bài 5: Chat phòng (ktrtk1)    : ROOMS (xem phòng) | JOIN <phòng/số>");
        System.out.println("                                   (Vào phòng xong gõ chat tự do, 'LEAVE' để thoát)");
        System.out.println(" • Bài 7: Đăng ký & Ghi nhật ký  : HELLO <clientId> | LOG <nội dung>");
        System.out.println(" • Đề 7000: Chọn 1, 2, 3, 4      : Nhập nhiều dòng, kết thúc bằng dấu '.'");
        System.out.println(" • Xem lại Menu này              : MENU hoặc HELP");
        System.out.println(" • Thoát chương trình            : EXIT. hoặc QUIT");
        System.out.println("=====================================================================");
    }

    public static void main(String[] args) {
        System.out.println("--> Đang kết nối tới Server " + SERVER_HOST + ":" + SERVER_PORT + "...");

        // Cú pháp try-with-resources: Tự động đóng Socket, Stream và Scanner khi thoát khỏi khối try
        try (
            // 1. Khởi tạo kết nối Socket tới Server theo IP và Port
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            // 2. Luồng đọc dữ liệu từ Server gửi về (chuẩn UTF-8)
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            // 3. Luồng gửi dữ liệu lên Server (autoFlush = true để đẩy dữ liệu đi ngay)
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            // 4. Scanner đọc dữ liệu người dùng gõ từ bàn phím Console
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("--> Kết nối thành công tới Server!");
            inMenuClient();

            // =====================================================================
            // LUỒNG PHỤ 1: NHẬN PHẢN HỒI BẤT ĐỒNG BỘ TỪ SERVER
            // =====================================================================
            Thread receiverThread = new Thread(() -> {
                try {
                    String serverMsg;
                    // readLine() liên tục chờ tin nhắn từ Server
                    while ((serverMsg = in.readLine()) != null) {
                        // Lọc bỏ các dấu hiệu kết thúc giao thức để giao diện sạch đẹp
                        if (serverMsg.equals("END_MENU") || serverMsg.equals("END_RESPONSE")) {
                            continue;
                        }
                        // In tin nhắn nhận được từ Server ra màn hình
                        System.out.println(serverMsg);

                        // Nếu Server gửi thông báo BYE thì thoát luồng nhận
                        if (serverMsg.startsWith("BYE")) {
                            break;
                        }
                    }
                } catch (IOException ignored) {
                    // Khi Socket bị đóng, readLine() sẽ ném IOException -> kết thúc luồng êm đẹp
                }
            });
            // setDaemon(true): Luồng ngầm này sẽ tự động dừng khi luồng chính (Main) kết thúc
            receiverThread.setDaemon(true);
            receiverThread.start();

            // Biến cờ theo dõi Client đang ở Menu hay đang ở trong phòng chat
            boolean inChatRoom = false;

            // =====================================================================
            // LUỒNG CHÍNH 2: ĐỌC DỮ LIỆU TỪ BÀN PHÍM VÀ GỬI LÊN SERVER
            // =====================================================================
            while (true) {
                // Đổi prompt trực quan để người dùng biết mình đang ở đâu
                System.out.print(inChatRoom ? "[ChatRoom] > " : "Client > ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine();

                if (line.trim().isEmpty()) continue;

                String trimmed = line.trim();

                // Cập nhật trạng thái phòng chat dựa trên lệnh người dùng gõ
                if (trimmed.toUpperCase().startsWith("JOIN ")) {
                    inChatRoom = true;
                } else if (trimmed.equalsIgnoreCase("LEAVE") || trimmed.equalsIgnoreCase("/QUIT")) {
                    inChatRoom = false;
                }

                // Xem lại Menu cục bộ phía Client
                if (trimmed.equalsIgnoreCase("MENU") || trimmed.equalsIgnoreCase("HELP")) {
                    inMenuClient();
                    continue;
                }

                // Gửi nguyên dòng lệnh vừa nhập lên Server
                out.println(line);

                // =================================================================
                // ĐỀ THI 7000: NẾU CHỌN 1, 2, 3, 4 (ở Menu chính)
                // Chuyển sang chế độ nhập nhiều dòng chuỗi, dừng khi gặp dòng "."
                // =================================================================
                if (!inChatRoom && (trimmed.equals("1") || trimmed.equals("2") || trimmed.equals("3") || trimmed.equals("4"))) {
                    System.out.println("[NHẬP DỮ LIỆU] Nhập các dòng chuỗi (Gõ dấu '.' trên một dòng riêng để kết thúc):");
                    while (true) {
                        System.out.print("> ");
                        if (!scanner.hasNextLine()) break;
                        String dataLine = scanner.nextLine();
                        // Gửi từng dòng dữ liệu lên Server
                        out.println(dataLine);
                        // Khi gặp dấu chấm hoặc EXIT. thì dừng gửi dữ liệu
                        if (dataLine.trim().equals(".") || dataLine.trim().equalsIgnoreCase("EXIT.")) {
                            break;
                        }
                    }
                }

                // LỆNH THOÁT CLIENT
                if (trimmed.equalsIgnoreCase("EXIT.") || (!inChatRoom && trimmed.equalsIgnoreCase("QUIT"))) {
                    // Nghỉ 300ms để luồng nhận kịp in phản hồi BYE từ Server trước khi đóng
                    Thread.sleep(300);
                    break;
                }
            }

            System.out.println("Đã đóng kết nối Client an toàn.");

        } catch (Exception e) {
            System.err.println("Lỗi Client TCP: " + e.getMessage());
        }
    }
}
