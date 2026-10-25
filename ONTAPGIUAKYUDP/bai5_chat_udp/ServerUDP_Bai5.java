import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * BÀI 5 (UDP): Chat nhiều Client qua UDP (Server)
 * - Cổng 7005.
 * - Quản lý client bằng cấu trúc Thread-safe (ConcurrentHashMap<SocketAddress, String>).
 * - Lệnh: JOIN <nickname>, MSG <nội dung>, USERS, QUIT.
 * - Broadcast tin nhắn tới tất cả client đang online qua DatagramPacket.
 */
public class ServerUDP_Bai5 {
    public static final int PORT = 7005;
    public static final int BUFFER_SIZE = 1024;

    // Quản lý SocketAddress -> Nickname (Thread-safe)
    private static final ConcurrentHashMap<SocketAddress, String> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 5 UDP (CHAT MULTI-CLIENT) ĐANG CHẠY CỔNG " + PORT + " ===");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                SocketAddress clientAddr = packet.getSocketAddress();
                String msg = new String(packet.getData(), 0, packet.getLength(), "UTF-8").trim();

                String[] tokens = msg.split("\\s+");
                String command = tokens[0].toUpperCase();

                switch (command) {
                    case "JOIN":
                        if (tokens.length >= 2) {
                            String nick = tokens[1];
                            if (clients.containsValue(nick.toLowerCase())) {
                                sendDirect(socket, clientAddr, "ERR: Nickname đã tồn tại!");
                            } else {
                                clients.put(clientAddr, nick);
                                sendDirect(socket, clientAddr, "OK: Chào mừng " + nick + "!");
                                broadcast(socket, "[HỆ THỐNG] " + nick + " đã tham gia phòng chat UDP.", clientAddr);
                            }
                        } else {
                            sendDirect(socket, clientAddr, "ERR Cú pháp: JOIN <nickname>");
                        }
                        break;

                    case "USERS":
                        sendDirect(socket, clientAddr, "Online (" + clients.size() + "): " + String.join(", ", clients.values()));
                        break;

                    case "MSG":
                        String sender = clients.getOrDefault(clientAddr, "Ẩn_danh");
                        String chatText = msg.length() > 4 ? msg.substring(4).trim() : "";
                        broadcast(socket, "[" + sender + "]: " + chatText, clientAddr);
                        sendDirect(socket, clientAddr, "[Bạn]: " + chatText);
                        break;

                    case "QUIT":
                        String leavingNick = clients.remove(clientAddr);
                        if (leavingNick != null) {
                            sendDirect(socket, clientAddr, "BYE");
                            broadcast(socket, "[HỆ THỐNG] " + leavingNick + " đã rời phòng chat.", clientAddr);
                        }
                        break;

                    default:
                        sendDirect(socket, clientAddr, "Lệnh hỗ trợ: JOIN <nick>, MSG <nội dung>, USERS, QUIT");
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi Server UDP Chat: " + e.getMessage());
        }
    }

    private static void sendDirect(DatagramSocket socket, SocketAddress target, String message) throws IOException {
        byte[] data = message.getBytes("UTF-8");
        socket.send(new DatagramPacket(data, data.length, target));
    }

    private static void broadcast(DatagramSocket socket, String message, SocketAddress except) throws IOException {
        byte[] data = message.getBytes("UTF-8");
        for (SocketAddress target : clients.keySet()) {
            if (!target.equals(except)) {
                socket.send(new DatagramPacket(data, data.length, target));
            }
        }
    }
}
