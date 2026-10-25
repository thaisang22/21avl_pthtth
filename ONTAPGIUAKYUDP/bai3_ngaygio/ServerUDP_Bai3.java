import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BÀI 3: Dịch vụ Ngày Giờ UDP (Server)
 * - Lệnh hỗ trợ: DATE, TIME, DATETIME.
 * - UDP là giao thức phi kết nối (Connectionless) nên KHÔNG CẦN lệnh QUIT.
 * - DateTimeFormatter:
 *     + DATE: "dd MM yyyy"
 *     + TIME: "HH mm ss"
 *     + DATETIME: "dd MM yyyy HH mm ss"
 */
public class ServerUDP_Bai3 {
    public static final int PORT = 7003;
    public static final int BUFFER_SIZE = 1024;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH mm ss");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd MM yyyy HH mm ss");

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 3 UDP (NGÀY GIỜ) ĐANG CHẠY CỔNG " + PORT + " ===");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket); // Chờ nhận yêu cầu từ client

                String command = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim().toUpperCase();
                System.out.println("Nhận yêu cầu: [" + command + "] từ " + receivePacket.getAddress() + ":" + receivePacket.getPort());

                LocalDateTime now = LocalDateTime.now();
                String responseText;

                switch (command) {
                    case "DATE":
                        responseText = now.format(DATE_FORMAT);
                        break;
                    case "TIME":
                        responseText = now.format(TIME_FORMAT);
                        break;
                    case "DATETIME":
                        responseText = now.format(DATETIME_FORMAT);
                        break;
                    default:
                        responseText = "ERR UNKNOWN_COMMAND (Hỗ trợ: DATE, TIME, DATETIME)";
                        break;
                }

                // Gửi phản hồi ngược lại cho Client
                byte[] sendData = responseText.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort()
                );
                socket.send(sendPacket);
            }
        } catch (Exception e) {
            System.err.println("Lỗi Server UDP: " + e.getMessage());
        }
    }
}
