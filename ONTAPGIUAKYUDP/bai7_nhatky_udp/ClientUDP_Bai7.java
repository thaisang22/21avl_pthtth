import java.net.*;
import java.util.Scanner;

/**
 * BÀI 7 (UDP): Server lưu nhật ký tin nhắn qua UDP (Client)
 */
public class ClientUDP_Bai7 {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7007;
    public static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 7 UDP (LƯU NHẬT KÝ) GỬI ĐẾN " + SERVER_HOST + ":" + SERVER_PORT + " ===");
        System.out.println("Bắt đầu bằng lệnh: HELLO <clientId>");
        System.out.println("Gửi các nội dung tin nhắn cần lưu. Nhập QUIT để thoát.");

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

                if (!line.isEmpty()) {
                    byte[] data = line.getBytes("UTF-8");
                    socket.send(new DatagramPacket(data, data.length, serverAddr, SERVER_PORT));
                }

                // Chờ nhận phản hồi
                byte[] buf = new byte[BUFFER_SIZE];
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(p);
                    String res = new String(p.getData(), 0, p.getLength(), "UTF-8");
                    System.out.println("Server UDP < " + res);
                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout: Server không phản hồi.");
                }

                if (line.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi Client UDP Bài 7: " + e.getMessage());
        }
    }
}
