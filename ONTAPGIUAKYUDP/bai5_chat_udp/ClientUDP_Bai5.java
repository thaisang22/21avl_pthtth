import java.net.*;
import java.util.Scanner;

/**
 * BÀI 5 (UDP): Chat nhiều Client qua UDP (Client)
 * - Chạy 2 luồng: 1 luồng hứng tin nhắn broadcast từ Server, 1 luồng gửi tin bàn phím.
 */
public class ClientUDP_Bai5 {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7005;
    public static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 5 UDP (CHAT) GỬI ĐẾN " + SERVER_HOST + ":" + SERVER_PORT + " ===");
        System.out.println("Bắt đầu bằng: JOIN <nickname>");
        System.out.println("Các lệnh khác: MSG <nội dung> | USERS | QUIT");

        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(SERVER_HOST);
            Scanner scanner = new Scanner(System.in);

            // Luồng phụ nhận tin nhắn bất đồng bộ từ Server UDP
            Thread receiver = new Thread(() -> {
                byte[] buf = new byte[BUFFER_SIZE];
                try {
                    while (!socket.isClosed()) {
                        DatagramPacket p = new DatagramPacket(buf, buf.length);
                        socket.receive(p);
                        String msg = new String(p.getData(), 0, p.getLength(), "UTF-8");
                        System.out.println("\n" + msg);
                        System.out.print("Client UDP > ");
                    }
                } catch (Exception ignored) {}
            });
            receiver.setDaemon(true);
            receiver.start();

            // Luồng chính đọc phím và gửi lên Server
            while (true) {
                System.out.print("Client UDP > ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();

                if (!line.isEmpty()) {
                    byte[] data = line.getBytes("UTF-8");
                    socket.send(new DatagramPacket(data, data.length, serverAddr, SERVER_PORT));
                }

                if (line.equalsIgnoreCase("QUIT")) {
                    Thread.sleep(300);
                    break;
                }
            }

            socket.close();
            System.out.println("Đã thoát Client UDP.");
        } catch (Exception e) {
            System.err.println("Lỗi Client UDP Chat: " + e.getMessage());
        }
    }
}
