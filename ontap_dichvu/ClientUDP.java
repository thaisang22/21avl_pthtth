import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * ============================================================================
 * CLIENT UDP CHUYÊN DỤNG CHO DỊCH VỤ (KHÔNG CÓ CHAT)
 * ============================================================================
 * - Gửi yêu cầu dưới dạng DatagramPacket tới Server UDP (localhost:7000).
 * - Nhận gói tin phản hồi và in kết quả ra màn hình.
 * - Có cơ chế `setSoTimeout(5000)` (5 giây) để chống treo khi Server không phản hồi.
 * - Hỗ trợ toàn bộ các lệnh: 1, 2, 3, 4, DIGIT, DATE, TIME, CALC, LOG...
 * ============================================================================
 */
public class ClientUDP {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7000;
    public static final int BUFFER_SIZE = 4096;

    public static void inMenuClient() {
        System.out.println(DichVuThuVien.layMenuDichVu("UDP CLIENT"));
    }

    public static void main(String[] args) {
        System.out.println("--> [UDP CLIENT] Đang kết nối tới " + SERVER_HOST + ":" + SERVER_PORT + "...");
        inMenuClient();

        try (
            DatagramSocket socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in, "UTF-8")
        ) {
            // Đặt thời gian chờ tối đa 5 giây khi nhận phản hồi để tránh treo chương trình
            socket.setSoTimeout(5000);
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            while (true) {
                System.out.print("ClientUDP > ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;

                // Xem menu cục bộ
                if (line.equalsIgnoreCase("MENU") || line.equalsIgnoreCase("HELP")) {
                    inMenuClient();
                    continue;
                }

                // Đóng gói và gửi yêu cầu sang Server
                byte[] sendData = line.getBytes(StandardCharsets.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);

                // Nếu là lệnh thoát thì dừng luôn
                if (line.equals("0") || line.equalsIgnoreCase("EXIT") || line.equalsIgnoreCase("QUIT")) {
                    System.out.println("--> Đã gửi yêu cầu thoát. Tạm biệt!");
                    break;
                }

                // Chờ nhận phản hồi từ Server
                try {
                    byte[] receiveBuffer = new byte[BUFFER_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(receivePacket);

                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                    System.out.println(">>> Server phản hồi:\n" + response);
                } catch (SocketTimeoutException e) {
                    System.err.println("(!) Quá thời gian chờ (Timeout 5s) - Server chưa phản hồi.");
                }
            }

            System.out.println("--> Đã đóng Socket UDP Client an toàn.");
        } catch (Exception e) {
            System.err.println("Lỗi UDP Client: " + e.getMessage());
        }
    }
}
