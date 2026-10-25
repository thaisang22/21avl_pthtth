import java.io.*;
import java.net.*;
import java.util.*;

/**
 * ============================================================================
 * LỚP CHATSERVER: Quản trị trung tâm của hệ thống Chat Room đa luồng qua TCP Socket
 * ============================================================================
 * Vai trò chính:
 * 1. Mở cổng ServerSocket (cổng 5000) để lắng nghe kết nối từ các Client.
 * 2. Quản lý danh sách các phòng chat (`rooms`) và danh sách các client kết nối (`clients`).
 * 3. Chấp nhận kết nối mới và sinh ra một tiểu trình (Thread) `ClientHandler` độc lập cho từng client.
 * 4. Cung cấp các hàm phát sóng (Broadcast) để gửi tin nhắn đến đúng các thành viên trong cùng một phòng.
 */
public class ChatServer {
    // Cổng TCP Server lắng nghe. Cả Server và Client phải cùng thống nhất số cổng này.
    public static final int PORT = 5000;

    // Danh sách lưu trữ tên các phòng chat có trong hệ thống
    public static ArrayList<String> rooms = new ArrayList<>();

    // Danh sách lưu trữ tất cả các đối tượng ClientHandler đại diện cho các client đang kết nối
    public static ArrayList<ClientHandler> clients = new ArrayList<>();

    /**
     * Khởi tạo các phòng chat mặc định sẵn có khi Server vừa khởi động
     */
    public static void initRooms() {
        rooms.add("Study");
        rooms.add("Game");
        rooms.add("TraiNghiem");
    }

    /**
     * Đếm số lượng thành viên hiện đang ở trong một phòng chat cụ thể
     * @param room Tên phòng cần đếm số người
     * @return Số lượng client đang ở trong phòng đó
     */
    public static int countMembers(String room) {
        int count = 0;
        // Duyệt qua toàn bộ danh sách client đang online trên Server
        for (ClientHandler c : clients) {
            // Nếu thuộc tính currentRoom của client trùng với tên phòng (không phân biệt hoa/thường)
            if (room.equalsIgnoreCase(c.currentRoom)) {
                count++;
            }
        }
        return count;
    }

    /**
     * GỬI TIN NHẮN BROADCAST CHO TẤT CẢ THÀNH VIÊN TRONG PHÒNG (Bao gồm cả người gửi)
     * Thường dùng khi: Có tin nhắn chat mới của một người gửi lên phòng, hoặc thông báo hệ thống chung
     * @param room Tên phòng nhận tin nhắn
     * @param msg Nội dung tin nhắn cần gửi
     */
    public static void broadcast(String room, String msg) {
        for (ClientHandler c : clients) {
            if (room.equalsIgnoreCase(c.currentRoom)) {
                c.sendMessage(msg);
            }
        }
    }

    /**
     * GỬI TIN NHẮN BROADCAST CHO CÁC THÀNH VIÊN TRONG PHÒNG NGOẠI TRỪ 1 CLIENT (Người gửi)
     * Thường dùng khi: Thông báo một người vừa rời phòng (/quit hoặc ngắt kết nối).
     * Người rời phòng không cần nhận lại thông báo "chính mình vừa rời phòng".
     * @param room Tên phòng nhận tin nhắn
     * @param msg Nội dung tin nhắn
     * @param except Client cần loại trừ
     */
    public static void broadcast(String room, String msg, ClientHandler except) {
        for (ClientHandler c : clients) {
            // Chỉ gửi nếu client đó khác client bị loại trừ và đang ở cùng phòng
            if (c != except && room.equalsIgnoreCase(c.currentRoom)) {
                c.sendMessage(msg);
            }
        }
    }

    /**
     * HÀM MAIN: Điểm khởi chạy chương trình Server
     */
    public static void main(String[] args) {
        try {
            // 1. Tạo ServerSocket lắng nghe tại cổng PORT (5000)
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Server dang chay tai cong " + PORT);

            // 2. Nạp danh sách các phòng mặc định ban đầu
            initRooms();
            for (String r : rooms) {
                System.out.println("Da tao phong: " + r);
            }
            System.out.println("Dang cho client ket noi...\n");

            // 3. Vòng lặp vô tận liên tục lắng nghe và phục vụ nhiều Client kết nối tới
            while (true) {
                // server.accept() là hàm BLOCKING: nó sẽ dừng chờ cho đến khi có một Client kết nối tới.
                // Khi có kết nối, nó trả về một Socket riêng biệt dùng để trao đổi dữ liệu với Client đó.
                Socket socket = server.accept();

                // Tạo một Thread ClientHandler riêng để quản lý toàn bộ giao tiếp với Client này
                ClientHandler client = new ClientHandler(socket);

                // Thêm vào danh sách quản lý chung của Server
                clients.add(client);

                // Kích hoạt luồng (gọi phương thức run() của ClientHandler chạy ở chế độ nền độc lập)
                client.start();
            }
        } catch (IOException e) {
            System.out.println("Loi server: " + e.getMessage());
        }
    }
}
