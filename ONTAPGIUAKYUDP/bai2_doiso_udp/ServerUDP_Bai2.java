import java.net.*;

/**
 * BÀI 2 (UDP): Đổi chữ số thành chữ (Server)
 * - Cổng 7002.
 * - Ca biên: 0-9 hợp lệ; rỗng, khoảng trắng, 10, 'a' -> ERR INVALID_DIGIT.
 */
public class ServerUDP_Bai2 {
    public static final int PORT = 7002;
    public static final int BUFFER_SIZE = 1024;
    private static final String[] VIETNAMESE_DIGITS = {
        "Không", "Một", "Hai", "Ba", "Bốn", "Năm", "Sáu", "Bảy", "Tám", "Chín"
    };

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 2 UDP (ĐỔI CHỮ SỐ) ĐANG CHẠY CỔNG " + PORT + " ===");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String line = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                String response;

                // Kiểm tra ca biên bắt buộc: đúng 1 ký tự từ 0-9
                if (line.length() != 1 || line.charAt(0) < '0' || line.charAt(0) > '9') {
                    response = "ERR INVALID_DIGIT";
                } else {
                    int digit = line.charAt(0) - '0';
                    response = VIETNAMESE_DIGITS[digit];
                }

                byte[] sendData = response.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                socket.send(sendPacket);
            }
        } catch (Exception e) {
            System.err.println("Lỗi Server UDP Bài 2: " + e.getMessage());
        }
    }
}
