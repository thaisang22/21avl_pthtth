import java.net.*;
import java.util.Scanner;

/**
 * BÀI 3: Dịch vụ Ngày Giờ UDP (Client)
 * - Gửi các lệnh: DATE, TIME, DATETIME.
 * - Có thiết lập Socket Timeout để tránh treo khi Server không phản hồi.
 */
public class ClientUDP_Bai3 {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7003;
    public static final int BUFFER_SIZE = 1024;
    public static final int TIMEOUT_MS = 3000; // 3 giây chờ tối đa

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 3 UDP (NGÀY GIỜ) GỬI ĐẾN " + SERVER_HOST + ":" + SERVER_PORT + " ===");
        System.out.println("Lệnh: DATE | TIME | DATETIME (Nhập EXIT để thoát client)");

        try (
            DatagramSocket socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in)
        ) {
            // Thiết lập timeout nhận phản hồi
            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            while (true) {
                System.out.print("Client UDP > ");
                if (!scanner.hasNextLine()) break;
                String command = scanner.nextLine().trim();

                if (command.equalsIgnoreCase("EXIT")) {
                    System.out.println("Client đã thoát.");
                    break;
                }

                // 1. Gửi gói tin lên Server
                byte[] sendData = command.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);

                // 2. Nhận gói tin phản hồi từ Server
                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(receivePacket);
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                    System.out.println("Server UDP < " + response);
                } catch (SocketTimeoutException e) {
                    System.err.println("Lỗi: Quá thời gian chờ phản hồi từ Server (Timeout).");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi Client UDP: " + e.getMessage());
        }
    }
}
