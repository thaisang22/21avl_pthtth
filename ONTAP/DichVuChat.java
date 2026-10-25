import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * =========================================================================
 * BỘ QUẢN LÝ DỊCH VỤ CHAT PHÒNG DÙNG CHUNG (DichVuChat) - FIT IUH
 * -------------------------------------------------------------------------
 * VAI TRÒ:
 * - Độc lập hoàn toàn, cung cấp logic chat phòng dùng chung cho cả ServerTCP và ServerUDP.
 * - Quản lý danh sách phòng: Study, Game, TraiNghiem (và tự động tạo thêm khi có yêu cầu).
 * - Quản lý thành viên theo phòng, đếm số lượng và lấy danh sách tên thành viên.
 * - Tự động Broadcast thông báo hệ thống khi có thành viên vào / rời phòng.
 *
 * TÍNH TOÀN VẸN ĐA LUỒNG (THREAD-SAFETY):
 * - Sử dụng `CopyOnWriteArrayList` cho danh sách phòng: Cho phép vừa đọc danh sách phòng
 *   vừa thêm phòng mới mà không bao giờ bị lỗi `ConcurrentModificationException`.
 * - Sử dụng `ConcurrentHashMap` cho danh sách thành viên: Đảm bảo hàng chục Client
 *   vào / rời phòng cùng một thời điểm mà không bị xung đột bộ nhớ (Race Condition).
 * =========================================================================
 */
public class DichVuChat {

    // Danh sách phòng chat mặc định (Thread-Safe: an toàn tuyệt đối khi vừa duyệt vừa thêm phần tử)
    public static final CopyOnWriteArrayList<String> rooms = new CopyOnWriteArrayList<>(
        Arrays.asList("Study", "Game", "TraiNghiem")
    );

    /**
     * HÀM CHUẨN HOÁ VÀ TÌM PHÒNG (resolveRoom):
     * Giải quyết thông minh đầu vào của Client:
     * - Nếu nhập số thứ tự: "1" -> "Study", "2" -> "Game", "3" -> "TraiNghiem"
     * - Nếu nhập tên chữ: "study", "GAME" -> tìm phòng có sẵn không phân biệt hoa thường
     * - Nếu nhập tên chưa có trong danh sách: Tự động tạo phòng mới và đưa vào rooms
     */
    public static String resolveRoom(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String trimmed = input.trim();

        // 1. Kiểm tra xem người dùng có nhập số thứ tự 1, 2, 3... không
        try {
            int idx = Integer.parseInt(trimmed);
            if (idx >= 1 && idx <= rooms.size()) {
                return rooms.get(idx - 1);
            }
        } catch (NumberFormatException ignored) {}

        // 2. Tìm tên phòng đã có sẵn trong danh sách (so sánh không phân biệt hoa thường)
        for (String r : rooms) {
            if (r.equalsIgnoreCase(trimmed)) {
                return r;
            }
        }

        // 3. Nếu là phòng mới toanh, tự động thêm vào danh sách phòng của hệ thống
        rooms.add(trimmed);
        return trimmed;
    }

    // =========================================================================
    // 1. PHẦN QUẢN LÝ CHAT CHO TCP (Thread-Safe)
    // =========================================================================
    // Map lưu: Nickname (viết thường) -> Đối tượng TCPChatMember
    public static final ConcurrentHashMap<String, TCPChatMember> tcpMembers = new ConcurrentHashMap<>();

    /**
     * Đại diện cho một thành viên kết nối qua TCP.
     * Lưu trữ: Tên, Phòng hiện tại và luồng PrintWriter để gửi tin nhắn về Client đó.
     */
    public static class TCPChatMember {
        public String name;
        public String currentRoom;
        public PrintWriter out;

        public TCPChatMember(String name, PrintWriter out) {
            this.name = name;
            this.out = out;
            this.currentRoom = null; // Mới kết nối chưa ở phòng nào
        }

        // Gửi một dòng tin nhắn tới Client này qua PrintWriter
        public void send(String msg) {
            if (out != null) out.println(msg);
        }
    }

    // Đăng ký Client mới vào hệ thống TCP
    public static void dangKyTCP(String nickname, PrintWriter out) {
        tcpMembers.put(nickname.toLowerCase(), new TCPChatMember(nickname, out));
    }

    // Cập nhật khi Client đổi tên (HELLO hoặc NICK)
    public static void doiTenTCP(String oldNick, String newNick, PrintWriter out) {
        TCPChatMember old = tcpMembers.remove(oldNick.toLowerCase());
        String curRoom = (old != null) ? old.currentRoom : null;
        TCPChatMember newMember = new TCPChatMember(newNick, out);
        newMember.currentRoom = curRoom; // Giữ nguyên phòng chat đang ở (nếu có)
        tcpMembers.put(newNick.toLowerCase(), newMember);
    }

    // Tham gia phòng chat TCP
    public static void thamGiaPhongTCP(String nickname, PrintWriter out, String room) {
        TCPChatMember mem = tcpMembers.computeIfAbsent(nickname.toLowerCase(), k -> new TCPChatMember(nickname, out));
        mem.name = nickname;
        mem.out = out;
        mem.currentRoom = room;
        // Broadcast thông báo cho các bạn khác trong phòng biết có người mới vào
        broadcastTCP(room, "[HỆ THỐNG] " + nickname + " đã tham gia phòng " + room + ".", nickname);
    }

    // Rời phòng chat TCP
    public static String roiPhongTCP(String nickname) {
        TCPChatMember mem = tcpMembers.get(nickname.toLowerCase());
        if (mem != null && mem.currentRoom != null) {
            String room = mem.currentRoom;
            mem.currentRoom = null; // Đưa về null (quay về Menu chính)
            broadcastTCP(room, "[HỆ THỐNG] " + mem.name + " đã rời phòng " + room + ".", mem.name);
            return room;
        }
        return null;
    }

    // Huỷ đăng ký khi Client ngắt kết nối đột ngột
    public static void huyDangKyTCP(String nickname) {
        TCPChatMember mem = tcpMembers.remove(nickname.toLowerCase());
        if (mem != null && mem.currentRoom != null) {
            broadcastTCP(mem.currentRoom, "[HỆ THỐNG] " + mem.name + " đã rời phòng (ngắt kết nối).", mem.name);
        }
    }

    // Đếm số lượng thành viên đang có mặt trong 1 phòng (TCP)
    public static int countTCPMembers(String room) {
        int count = 0;
        for (TCPChatMember m : tcpMembers.values()) {
            if (room.equalsIgnoreCase(m.currentRoom)) count++;
        }
        return count;
    }

    // Lấy danh sách tên thành viên trong 1 phòng (TCP)
    public static List<String> layDanhSachThanhVienTCP(String room) {
        List<String> list = new ArrayList<>();
        for (TCPChatMember m : tcpMembers.values()) {
            if (room.equalsIgnoreCase(m.currentRoom)) {
                list.add(m.name);
            }
        }
        return list;
    }

    // Broadcast tin nhắn trong phòng TCP (trừ người gửi exceptNick)
    public static void broadcastTCP(String room, String message, String exceptNick) {
        for (TCPChatMember m : tcpMembers.values()) {
            if (room.equalsIgnoreCase(m.currentRoom)) {
                if (exceptNick == null || !m.name.equalsIgnoreCase(exceptNick)) {
                    m.send(message);
                }
            }
        }
    }

    // Broadcast toàn hệ thống TCP (gửi cho tất cả Client đang online)
    public static void broadcastAllTCP(String message, String exceptNick) {
        for (TCPChatMember m : tcpMembers.values()) {
            if (exceptNick == null || !m.name.equalsIgnoreCase(exceptNick)) {
                m.send(message);
            }
        }
    }

    // =========================================================================
    // 2. PHẦN QUẢN LÝ CHAT CHO UDP (Thread-Safe)
    // =========================================================================
    // Map lưu: Địa chỉ mạng SocketAddress -> Đối tượng UDPChatMember
    public static final ConcurrentHashMap<SocketAddress, UDPChatMember> udpMembers = new ConcurrentHashMap<>();

    /**
     * Đại diện cho một thành viên kết nối qua UDP.
     * Lưu trữ: Tên, Phòng hiện tại và địa chỉ SocketAddress (IP + Port) để bắn DatagramPacket.
     */
    public static class UDPChatMember {
        public String name;
        public String currentRoom;
        public SocketAddress address;

        public UDPChatMember(String name, SocketAddress address) {
            this.name = name;
            this.address = address;
            this.currentRoom = null;
        }
    }

    // Tham gia phòng chat UDP
    public static void thamGiaPhongUDP(SocketAddress clientAddr, String nick, String room, DatagramSocket socket) {
        UDPChatMember mem = new UDPChatMember(nick, clientAddr);
        mem.currentRoom = room;
        udpMembers.put(clientAddr, mem);
        // Bắn gói tin thông báo tới các thành viên khác trong phòng
        broadcastUDP(socket, room, "[HỆ THỐNG] " + nick + " đã tham gia phòng " + room + ".", clientAddr);
    }

    // Rời phòng chat UDP
    public static String roiPhongUDP(SocketAddress clientAddr, DatagramSocket socket) {
        UDPChatMember mem = udpMembers.remove(clientAddr);
        if (mem != null && mem.currentRoom != null) {
            String room = mem.currentRoom;
            broadcastUDP(socket, room, "[HỆ THỐNG] " + mem.name + " đã rời phòng " + room + ".", clientAddr);
            return room;
        }
        return null;
    }

    // Đếm số lượng người trong phòng (UDP)
    public static int countUDPMembers(String room) {
        int count = 0;
        for (UDPChatMember m : udpMembers.values()) {
            if (room.equalsIgnoreCase(m.currentRoom)) count++;
        }
        return count;
    }

    // Lấy danh sách tên thành viên trong phòng (UDP)
    public static List<String> layDanhSachThanhVienUDP(String room) {
        List<String> list = new ArrayList<>();
        for (UDPChatMember m : udpMembers.values()) {
            if (room.equalsIgnoreCase(m.currentRoom)) {
                list.add(m.name);
            }
        }
        return list;
    }

    // Broadcast trong phòng UDP bằng DatagramPacket
    public static void broadcastUDP(DatagramSocket socket, String room, String message, SocketAddress exceptAddr) {
        try {
            byte[] data = message.getBytes("UTF-8");
            for (UDPChatMember m : udpMembers.values()) {
                if (room.equalsIgnoreCase(m.currentRoom)) {
                    // Không gửi lại cho chính người gửi
                    if (exceptAddr == null || !m.address.equals(exceptAddr)) {
                        DatagramPacket packet = new DatagramPacket(data, data.length, m.address);
                        socket.send(packet);
                    }
                }
            }
        } catch (IOException ignored) {}
    }

    // =========================================================================
    // 3. TIỆN ÍCH DÙNG CHUNG CHO CẢ TCP VÀ UDP
    // =========================================================================
    /**
     * Tạo chuỗi hiển thị danh sách phòng cùng số lượng thành viên đang online trong phòng đó.
     */
    public static String layDanhSachPhong(boolean isTCP) {
        StringBuilder sb = new StringBuilder("--- DANH SÁCH PHÒNG CHAT ---\n");
        for (int i = 0; i < rooms.size(); i++) {
            String r = rooms.get(i);
            int count = isTCP ? countTCPMembers(r) : countUDPMembers(r);
            sb.append((i + 1)).append(". ").append(r).append(" (").append(count).append(" người)\n");
        }
        sb.append("--> Vào phòng: JOIN <tên_phòng> hoặc JOIN <số> (Ví dụ: JOIN Study hoặc JOIN 1)");
        return sb.toString().trim();
    }
}
