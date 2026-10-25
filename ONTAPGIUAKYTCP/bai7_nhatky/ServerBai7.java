import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BÀI 7: Server lưu nhật ký tin nhắn
 * - Client gửi "HELLO clientId" trước khi gửi nội dung.
 * - Chỉ chấp nhận clientId gồm chữ, số, dấu gạch ngang và gạch dưới: ^[a-zA-Z0-9_-]+$
 * - Server lưu tin nhắn theo từng client vào file data/logs/{clientId}.txt
 * - Mỗi dòng gồm: [timestamp] [địa chỉ remote] [nội dung]
 * - Kết thúc bằng QUIT thay cho chuỗi HET nằm lẫn trong nội dung.
 */
public class ServerBai7 {
    public static final int PORT = 6007;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 7 (LƯU NHẬT KÝ TIN NHẮN) ĐANG CHẠY CỔNG " + PORT + " ===");
        // Đảm bảo thư mục data/logs tồn tại
        File logDir = new File("data/logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

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
        String remoteAddress = socket.getRemoteSocketAddress().toString();
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        ) {
            String clientId = null;

            // BƯỚC 1: Bắt buộc gửi "HELLO clientId" đầu tiên
            while (true) {
                out.println("Vui lòng gửi 'HELLO <clientId>' để bắt đầu phiên:");
                String firstLine = in.readLine();
                if (firstLine == null) return;

                firstLine = firstLine.trim();
                String[] parts = firstLine.split("\\s+");

                if (parts.length == 2 && parts[0].equalsIgnoreCase("HELLO")) {
                    String candidateId = parts[1];
                    // Chỉ chấp nhận chữ, số, gạch ngang và gạch dưới
                    if (candidateId.matches("^[a-zA-Z0-9_-]+$")) {
                        clientId = candidateId;
                        out.println("OK HELLO " + clientId + " (Bắt đầu gửi nội dung, kết thúc bằng QUIT)");
                        System.out.println("Client xác thực thành công clientId = " + clientId);
                        break;
                    } else {
                        out.println("ERR INVALID_CLIENT_ID (Chỉ cho phép chữ, số, '-' và '_')");
                    }
                } else {
                    out.println("ERR REQUIRE_HELLO_FIRST (Cú pháp bắt buộc: HELLO <clientId>)");
                }
            }

            // BƯỚC 2: Chuẩn bị file log tại data/logs/{clientId}.txt
            File logFile = new File("data/logs/" + clientId + ".txt");
            try (PrintWriter fileWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"), true)) {
                
                String line;
                while ((line = in.readLine()) != null) {
                    // Kết thúc khi gặp lệnh QUIT (đứng riêng một dòng)
                    if (line.trim().equalsIgnoreCase("QUIT")) {
                        out.println("BYE: Đã lưu toàn bộ nhật ký cho " + clientId);
                        break;
                    }

                    // Nếu chuỗi "HET" nằm lẫn trong nội dung, vẫn lưu bình thường (theo yêu cầu đề)
                    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                    String logEntry = String.format("[%s] [%s] %s", timestamp, remoteAddress, line);

                    // Ghi vào file log
                    fileWriter.println(logEntry);
                    System.out.println("Saved -> " + logFile.getPath() + ": " + logEntry);

                    out.println("SAVED: " + line);
                }
            }

        } catch (IOException e) {
            System.out.println("Client ngắt kết nối: " + remoteAddress);
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
