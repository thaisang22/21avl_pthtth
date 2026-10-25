import java.net.*;
import java.util.Scanner;

/**
 * ĐỀ THI CHUỖI IUH (PHIÊN BẢN UDP - CỔNG 7000)
 */
public class ClientUDP_7000 {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7000;
    public static final int BUFFER_SIZE = 2048;

    public static void main(String[] args) {
        System.out.println("=== CLIENT ĐỀ THI CHUỖI UDP GỬI TỚI " + SERVER_HOST + ":" + SERVER_PORT + " ===");
        System.out.println("Gõ MENU để xem danh sách dịch vụ. Gõ EXIT để thoát.");

        try (
            DatagramSocket socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in)
        ) {
            socket.setSoTimeout(3000);
            InetAddress serverAddr = InetAddress.getByName(SERVER_HOST);

            while (true) {
                System.out.print("Client UDP > ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();

                if (line.equalsIgnoreCase("EXIT")) {
                    break;
                }

                if (!line.isEmpty()) {
                    byte[] data = line.getBytes("UTF-8");
                    socket.send(new DatagramPacket(data, data.length, serverAddr, SERVER_PORT));
                }

                byte[] buf = new byte[BUFFER_SIZE];
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(p);
                    String res = new String(p.getData(), 0, p.getLength(), "UTF-8");
                    System.out.println("Server UDP < \n" + res);
                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout: Server không phản hồi.");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi Client UDP: " + e.getMessage());
        }
    }
}
