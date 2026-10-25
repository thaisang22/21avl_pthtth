import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BÀI 7 (UDP): Server lưu nhật ký tin nhắn qua UDP
 * - Cổng 7007.
 * - Client gửi "HELLO clientId" trước khi gửi nội dung.
 * - Regex clientId: ^[a-zA-Z0-9_-]+$
 * - Lưu vào data/logs/{clientId}.txt: [timestamp] [remoteAddress] [nội dung]
 */
public class ServerUDP_Bai7 {
    public static final int PORT = 7007;
    public static final int BUFFER_SIZE = 1024;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Quản lý SocketAddress -> clientId
    private static final ConcurrentHashMap<SocketAddress, String> authenticatedClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 7 UDP (LƯU NHẬT KÝ) ĐANG CHẠY CỔNG " + PORT + " ===");
        File dir = new File("data/logs");
        if (!dir.exists()) dir.mkdirs();

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                SocketAddress clientAddr = packet.getSocketAddress();
                String line = new String(packet.getData(), 0, packet.getLength(), "UTF-8").trim();

                String response;
                String[] parts = line.split("\\s+");

                // 1. Kiểm tra lệnh HELLO clientId
                if (parts.length == 2 && parts[0].equalsIgnoreCase("HELLO")) {
                    String candidateId = parts[1];
                    if (candidateId.matches("^[a-zA-Z0-9_-]+$")) {
                        authenticatedClients.put(clientAddr, candidateId);
                        response = "OK HELLO " + candidateId + " (Sẵn sàng nhận log!)";
                        System.out.println("Xác thực ID thành công: " + candidateId + " từ " + clientAddr);
                    } else {
                        response = "ERR INVALID_CLIENT_ID (Chỉ cho phép chữ, số, '-' và '_')";
                    }
                }
                // 2. Lệnh QUIT
                else if (line.equalsIgnoreCase("QUIT")) {
                    String id = authenticatedClients.remove(clientAddr);
                    response = "BYE: Đã đóng phiên ghi log cho " + (id != null ? id : "client");
                }
                // 3. Ghi log nội dung
                else {
                    String currentClientId = authenticatedClients.get(clientAddr);
                    if (currentClientId == null) {
                        response = "ERR REQUIRE_HELLO_FIRST (Cú pháp: HELLO <clientId>)";
                    } else {
                        File logFile = new File("data/logs/" + currentClientId + ".txt");
                        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"), true)) {
                            String time = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                            String logEntry = String.format("[%s] [%s] %s", time, clientAddr.toString(), line);
                            pw.println(logEntry);
                            response = "SAVED: " + line;
                            System.out.println("Saved -> " + logFile.getPath() + ": " + logEntry);
                        } catch (IOException e) {
                            response = "ERR WRITE_FILE: " + e.getMessage();
                        }
                    }
                }

                // Gửi phản hồi lại Client
                byte[] sendData = response.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                socket.send(sendPacket);
            }
        } catch (Exception e) {
            System.err.println("Lỗi Server UDP Bài 7: " + e.getMessage());
        }
    }
}
