import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * BÀI 5: Chat TCP nhiều client (Client)
 * - Sử dụng 2 luồng: 1 luồng đọc bàn phím gửi lên server, 1 luồng nhận tin nhắn broadcast từ server.
 */
public class ClientBai5 {
    public static final String HOST = "localhost";
    public static final int PORT = 6005;

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 5 (CHAT TCP) KẾT NỐI TỚI " + HOST + ":" + PORT + " ===");

        try {
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            Scanner scanner = new Scanner(System.in);

            // Luồng phụ: Liên tục lắng nghe tin nhắn từ Server và in ra màn hình
            Thread receiverThread = new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println(serverMsg);
                        if (serverMsg.equalsIgnoreCase("BYE")) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Đã ngắt kết nối với Server.");
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            // Luồng chính: Đọc từ console và gửi lên server
            while (true) {
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine();
                out.println(line);

                if (line.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

            // Chờ một chút rồi đóng kết nối
            Thread.sleep(300);
            socket.close();
            System.out.println("Client đã thoát.");

        } catch (Exception e) {
            System.err.println("Lỗi Client: " + e.getMessage());
        }
    }
}
