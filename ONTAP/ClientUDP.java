import java.net.*;
import java.util.Scanner;

/**
 * =========================================================================
 * BỘ KHUNG CLIENT UDP TOÀN DIỆN THI GIỮA KỲ (FIT - IUH)
 * -------------------------------------------------------------------------
 * KIẾN TRÚC 2 LUỒNG CHO CLIENT UDP:
 * 1. LUỒNG PHỤ (receiverThread): Chạy ngầm liên tục gọi `socket.receive(p)` để
 *    hứng các gói tin phản hồi hoặc tin chat Broadcast từ Server UDP gửi tới.
 * 2. LUỒNG CHÍNH (Main Thread): Đọc phím từ người dùng, đóng gói thành DatagramPacket
 *    và bắn tới địa chỉ Server (Host + Port).
 *
 * ĐẶC TÍNH:
 * - Client UDP không cần kết nối trước (không có Socket.connect()).
 * - Sử dụng DatagramSocket() rỗng để hệ điều hành tự gán một cổng ngẫu nhiên khả dụng.
 * =========================================================================
 */
public class ClientUDP {
    // Địa chỉ Server UDP cần gửi tới
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7000;
    // Bộ đệm 2KB để nhận dữ liệu phản hồi
    public static final int BUFFER_SIZE = 2048;

    /**
     * In Menu hướng dẫn các lệnh Client UDP hỗ trợ
     */
    public static void inMenuClient() {
        System.out.println("\n=====================================================================");
        System.out.println("                 MENU DỊCH VỤ CLIENT UDP");
        System.out.println("=====================================================================");
        System.out.println(" • Bài 2: Đổi chữ số             : DIGIT <chữ_số> (hoặc gõ số: 5)");
        System.out.println(" • Bài 3: Ngày giờ               : DATE | TIME | DATETIME");
        System.out.println(" • Bài 4: Máy tính từ xa         : CALC <toán_tử> <số_1> <số_2>");
        System.out.println(" • Bài 5: Chat phòng (ktrtk1)    : ROOMS (xem phòng) | JOIN <phòng/số>");
        System.out.println("                                   (Vào phòng xong gõ chat tự do, 'LEAVE' để thoát)");
        System.out.println(" • Bài 7: Đăng ký & Ghi log      : HELLO <clientId> | LOG <nội dung>");
        System.out.println(" • Đề 7000: Xử lý chuỗi          : 1 <chuỗi> | 2 <chuỗi> | 3 <nội dung>");
        System.out.println(" • Xem lại Menu                  : MENU hoặc HELP");
        System.out.println(" • Thoát Client                  : EXIT hoặc QUIT");
        System.out.println("=====================================================================");
    }

    public static void main(String[] args) {
        System.out.println("--> [UDP CLIENT] Khởi động, gửi yêu cầu tới " + SERVER_HOST + ":" + SERVER_PORT);
        inMenuClient();

        try {
            // Khởi tạo DatagramSocket phía Client (hệ thống tự động cấp một cổng rảnh ngẫu nhiên)
            DatagramSocket socket = new DatagramSocket();
            // Lấy địa chỉ IP của Server từ tên miền hoặc localhost
            InetAddress serverAddr = InetAddress.getByName(SERVER_HOST);
            Scanner scanner = new Scanner(System.in);

            // =====================================================================
            // LUỒNG PHỤ 1: CHẠY NGẦM HỨNG GÓI TIN UDP TỪ SERVER
            // =====================================================================
            Thread receiverThread = new Thread(() -> {
                byte[] buf = new byte[BUFFER_SIZE];
                try {
                    while (!socket.isClosed()) {
                        DatagramPacket p = new DatagramPacket(buf, buf.length);
                        // Chờ nhận gói tin phản hồi từ Server
                        socket.receive(p);
                        // Chuyển mảng byte nhận được thành chuỗi UTF-8
                        String res = new String(p.getData(), 0, p.getLength(), "UTF-8");
                        System.out.println("\nServer UDP < " + res);
                        System.out.print("Client UDP > ");
                    }
                } catch (Exception ignored) {
                    // Khi socket đóng, receive() ném ngoại lệ và luồng kết thúc an toàn
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            boolean inChatRoom = false;

            // =====================================================================
            // LUỒNG CHÍNH 2: ĐỌC DỮ LIỆU TỪ BÀN PHÍM VÀ BẮN GÓI TIN UDP
            // =====================================================================
            while (true) {
                System.out.print(inChatRoom ? "[ChatRoom UDP] > " : "Client UDP > ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;

                // Cập nhật trạng thái Prompt khi vào / rời phòng chat
                if (line.toUpperCase().startsWith("JOIN ")) {
                    inChatRoom = true;
                } else if (line.equalsIgnoreCase("LEAVE") || line.equalsIgnoreCase("/QUIT")) {
                    inChatRoom = false;
                }

                // Lệnh thoát Client
                if (line.equalsIgnoreCase("EXIT") || line.equalsIgnoreCase("QUIT")) {
                    System.out.println("Đang thoát Client UDP...");
                    break;
                }

                // Xem lại Menu
                if (line.equalsIgnoreCase("MENU") || line.equalsIgnoreCase("HELP")) {
                    inMenuClient();
                    continue;
                }

                // ĐÓNG GÓI DỮ LIỆU: Chuyển chuỗi thành mảng byte UTF-8
                byte[] data = line.getBytes("UTF-8");
                // Tạo gói tin DatagramPacket chứa: mảng byte, độ dài, địa chỉ Server IP và Port
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddr, SERVER_PORT);
                // Gửi gói tin đi
                socket.send(sendPacket);
            }

            // Đóng Socket giải phóng tài nguyên mạng
            socket.close();
            System.out.println("Đã đóng Client UDP an toàn.");

        } catch (Exception e) {
            System.err.println("Lỗi Client UDP: " + e.getMessage());
        }
    }
}
