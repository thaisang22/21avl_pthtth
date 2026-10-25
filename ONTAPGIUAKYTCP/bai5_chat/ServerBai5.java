import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * BÀI 5: Chat TCP nhiều client (Server)
 * - Mỗi client chọn nickname duy nhất sau khi kết nối.
 * - Broadcast tin nhắn tới tất cả client còn lại.
 * - Server dùng Thread Pool (ExecutorService) và cấu trúc Thread-safe (ConcurrentHashMap).
 * - Hỗ trợ các lệnh: USERS, MSG <nội_dung>, QUIT.
 * - Khi client ngắt bất thường, server tự động loại bỏ và tiếp tục chạy ổn định.
 */
public class ServerBai5 {
    public static final int PORT = 6005;

    // Cấu trúc Thread-safe lưu trữ danh sách client đang online: Nickname -> Handler
    private static final ConcurrentHashMap<String, ClientChatHandler> activeClients = new ConcurrentHashMap<>();

    // Sử dụng Thread Pool quản lý các luồng client kết nối
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 5 (CHAT TCP MULTI-CLIENT) ĐANG CHẠY CỔNG " + PORT + " ===");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientChatHandler handler = new ClientChatHandler(socket);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    // Broadcast tin nhắn từ sender tới TẤT CẢ các client khác
    public static void broadcast(String message, String senderNickname) {
        for (ClientChatHandler client : activeClients.values()) {
            if (!client.getNickname().equalsIgnoreCase(senderNickname)) {
                client.sendMessage(message);
            }
        }
    }

    // Broadcast thông báo hệ thống tới tất cả mọi người
    public static void broadcastSystem(String message) {
        for (ClientChatHandler client : activeClients.values()) {
            client.sendMessage("[HỆ THỐNG] " + message);
        }
    }

    // Handler xử lý cho từng Client
    static class ClientChatHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname = null;

        public ClientChatHandler(Socket socket) {
            this.socket = socket;
        }

        public String getNickname() {
            return nickname;
        }

        public void sendMessage(String msg) {
            if (out != null) {
                out.println(msg);
            }
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                // 1. Bước đăng ký Nickname duy nhất
                while (true) {
                    out.println("Vui lòng nhập Nickname của bạn:");
                    String inputNick = in.readLine();
                    if (inputNick == null) return; // Client ngắt sớm

                    inputNick = inputNick.trim();
                    if (inputNick.isEmpty() || inputNick.contains(" ")) {
                        out.println("ERR: Nickname không được rỗng hoặc chứa khoảng trắng!");
                        continue;
                    }

                    // Kiểm tra tính duy nhất (Thread-safe với putIfAbsent)
                    if (activeClients.putIfAbsent(inputNick.toLowerCase(), this) == null) {
                        this.nickname = inputNick;
                        out.println("OK: Chào mừng " + this.nickname + "! Lệnh hỗ trợ: USERS, MSG <nội dung>, QUIT");
                        System.out.println("Client đăng ký thành công: " + this.nickname + " từ " + socket.getRemoteSocketAddress());
                        broadcastSystem("Người dùng [" + this.nickname + "] vừa tham gia phòng chat.");
                        break;
                    } else {
                        out.println("ERR: Nickname đã tồn tại! Vui lòng chọn tên khác.");
                    }
                }

                // 2. Vòng lặp nhận và xử lý lệnh
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equalsIgnoreCase("QUIT")) {
                        out.println("BYE");
                        break;
                    }

                    if (line.equalsIgnoreCase("USERS")) {
                        // Trả về danh sách nickname online
                        out.println("Danh sách online (" + activeClients.size() + "): " + String.join(", ", activeClients.keySet()));
                    } else if (line.toUpperCase().startsWith("MSG ")) {
                        String content = line.substring(4).trim();
                        if (content.isEmpty()) {
                            out.println("ERR: Nội dung tin nhắn không được rỗng!");
                        } else {
                            broadcast("[" + this.nickname + "]: " + content, this.nickname);
                            out.println("[Bạn -> Mọi người]: " + content);
                        }
                    } else {
                        out.println("ERR Cú pháp không hợp lệ! Dùng: USERS, MSG <nội dung>, QUIT");
                    }
                }

            } catch (IOException e) {
                // Xử lý ngắt kết nối đột ngột (client tắt ngang, rớt mạng)
                System.out.println("Client ngắt kết nối bất thường: " + (nickname != null ? nickname : socket.getRemoteSocketAddress()));
            } finally {
                // Thu hồi tài nguyên & loại bỏ khỏi danh sách online
                if (nickname != null) {
                    activeClients.remove(nickname.toLowerCase());
                    broadcastSystem("Người dùng [" + nickname + "] đã rời phòng chat.");
                }
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
