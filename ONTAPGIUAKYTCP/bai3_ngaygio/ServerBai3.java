import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BÀI 3: TCP Dịch vụ Ngày Giờ (Server)
 * - Lệnh hỗ trợ: DATE, TIME, DATETIME và QUIT.
 * - DateTimeFormatter:
 *     + Ngày: "dd MM yyyy"
 *     + Giờ: "HH mm ss"
 *     + Ngày giờ: "dd MM yyyy HH mm ss"
 */
public class ServerBai3 {
    public static final int PORT = 6003;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH mm ss");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd MM yyyy HH mm ss");

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 3 TCP (NGÀY GIỜ) ĐANG CHẠY CỔNG " + PORT + " ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client đã kết nối: " + socket.getRemoteSocketAddress());
                new Thread(() -> handleClient(socket)).start();
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
            String command;
            while ((command = in.readLine()) != null) {
                command = command.trim().toUpperCase();
                LocalDateTime now = LocalDateTime.now();

                switch (command) {
                    case "DATE":
                        out.println(now.format(DATE_FORMAT));
                        break;
                    case "TIME":
                        out.println(now.format(TIME_FORMAT));
                        break;
                    case "DATETIME":
                        out.println(now.format(DATETIME_FORMAT));
                        break;
                    case "QUIT":
                        out.println("BYE");
                        return;
                    default:
                        out.println("ERR UNKNOWN_COMMAND (Hỗ trợ: DATE, TIME, DATETIME, QUIT)");
                        break;
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
