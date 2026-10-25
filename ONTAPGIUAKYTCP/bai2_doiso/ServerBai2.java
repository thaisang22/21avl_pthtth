import java.io.*;
import java.net.*;

/**
 * BÀI 2: TCP Đổi chữ số thành chữ (Server)
 * - Nhận 1 ký tự từ 0-9: Trả về cách đọc tiếng Việt tương ứng.
 * - Ca biên: 0, 9, chuỗi rỗng, 10, ký tự 'a', có khoảng trắng -> ERR INVALID_DIGIT.
 * - Cho phép gửi nhiều yêu cầu tới khi nhận QUIT.
 * - Dùng UTF-8 và giao thức mỗi thông điệp một dòng.
 */
public class ServerBai2 {
    public static final int PORT = 6002;
    private static final String[] VIETNAMESE_DIGITS = {
        "Không", "Một", "Hai", "Ba", "Bốn", "Năm", "Sáu", "Bảy", "Tám", "Chín"
    };

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 2 (ĐỔI CHỮ SỐ THÀNH CHỮ) SẴN SÀNG TẠI CỔNG " + PORT + " ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client đã kết nối: " + clientSocket.getRemoteSocketAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                // Kiểm tra lệnh thoát QUIT
                if (line.equalsIgnoreCase("QUIT")) {
                    out.println("BYE");
                    break;
                }

                // KIỂM TRA CA BIÊN BẮT BUỘC:
                // Phải đúng chính xác 1 ký tự số từ '0' đến '9'
                // Nếu có khoảng trắng (" 5 "), chuỗi rỗng (""), chữ ("a"), 2 chữ số ("10") -> Báo lỗi
                if (line.length() != 1 || line.charAt(0) < '0' || line.charAt(0) > '9') {
                    out.println("ERR INVALID_DIGIT");
                } else {
                    int digit = line.charAt(0) - '0';
                    out.println(VIETNAMESE_DIGITS[digit]);
                }
            }
        } catch (IOException e) {
            System.out.println("Client ngắt kết nối: " + socket.getRemoteSocketAddress());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
