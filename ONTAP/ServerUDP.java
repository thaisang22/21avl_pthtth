import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * =========================================================================
 * BỘ KHUNG SERVER UDP TOÀN DIỆN THI GIỮA KỲ (FIT - IUH)
 * -------------------------------------------------------------------------
 * ĐẶC ĐIỂM CỐT LÕI CỦA GIAO THỨC UDP (USER DATAGRAM PROTOCOL):
 * - Không hướng kết nối (Connectionless): Không bắt tay 3 bước, không duy trì Socket riêng.
 * - Truyền nhận theo gói tin (DatagramPacket) thông qua DatagramSocket.
 * - Server nhận diện từng Client thông qua địa chỉ mạng SocketAddress (gồm IP + Port).
 * - Tốc độ nhanh, nhẹ hơn TCP nhưng không đảm bảo thứ tự gói tin.
 *
 * HỖ TRỢ ĐẦY ĐỦ CÁC DẠNG BÀI THI TRÊN UDP:
 * 1. Bài 2: Đổi chữ số (DIGIT hoặc gõ thẳng số 0-9)
 * 2. Bài 3: Ngày giờ hệ thống (DATE, TIME, DATETIME - UDP xử lý độc lập từng gói)
 * 3. Bài 4: Máy tính từ xa (CALC op n1 n2, bắt đủ mã lỗi)
 * 4. Bài 5: Chat đa Client & Chat theo phòng (DichVuChat, Broadcast DatagramPacket)
 * 5. Bài 7: Đăng ký ID & Ghi nhật ký (HELLO, LOG)
 * 6. Đề 7000: Dịch vụ xử lý chuỗi (1, 2, 3)
 * =========================================================================
 */
public class ServerUDP {
    // Port lắng nghe mặc định
    public static final int PORT = 7000;
    // Kích thước bộ đệm nhận gói tin (2048 bytes = 2KB đủ cho mọi chuỗi văn bản thông thường)
    public static final int BUFFER_SIZE = 2048;

    // Bảng băm Thread-Safe lưu danh sách Client UDP: Địa chỉ mạng (SocketAddress) -> Nickname
    private static final ConcurrentHashMap<SocketAddress, String> udpClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" [UDP SERVER] ĐANG CHẠY CỔNG " + PORT);
        System.out.println(" ĐÃ TÍCH HỢP ĐẦY ĐỦ TOÀN BỘ DỊCH VỤ TRÊN UDP!");
        System.out.println("==================================================");

        // Khởi tạo DatagramSocket gắn vào cổng 7000 để nhận và phát gói tin UDP
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            // Mảng byte làm vùng nhớ đệm để nhận dữ liệu
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                // Tạo gói tin DatagramPacket rỗng sẵn sàng hứng dữ liệu
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                // socket.receive() là hàm chặn (blocking): chờ đến khi có 1 gói tin UDP bắn tới
                socket.receive(packet);

                // Lấy thông tin người gửi: Địa chỉ IP và số Port
                SocketAddress clientAddr = packet.getSocketAddress();
                // Giải mã mảng byte thành chuỗi String chuẩn UTF-8 (chỉ lấy đúng số byte thực tế: packet.getLength())
                String rawRequest = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                String request = rawRequest.trim();

                System.out.println("--> Nhận UDP từ " + clientAddr + ": [" + request + "]");

                // Tự động cấp tên mặc định "User_<port>" cho Client này nếu lần đầu gửi gói tin
                udpClients.putIfAbsent(clientAddr, "User_" + packet.getPort());

                // Giao cho hàm điều phối xử lý logic dịch vụ
                String response = xuLyYeuCau(rawRequest, clientAddr, socket);

                // GỬI PHẢN HỒI LẠI CLIENT: Đóng gói chuỗi kết quả vào DatagramPacket và bắn ngược lại IP:Port của Client
                if (response != null && !response.isEmpty()) {
                    byte[] sendData = response.getBytes("UTF-8");
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi Server UDP: " + e.getMessage());
        }
    }

    /**
     * KHỐI ĐIỀU PHỐI TẤT CẢ DỊCH VỤ TRÊN UDP
     * Tách biệt 2 trạng thái: Khi đang trong phòng chat vs Khi ở ngoài Menu chính.
     */
    private static String xuLyYeuCau(String raw, SocketAddress clientAddr, DatagramSocket socket) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return "ERR EMPTY_REQUEST";

        String[] tokens = trimmed.split("\\s+");
        String cmd = tokens[0].toUpperCase();

        // =====================================================================
        // TRẠNG THÁI 1: KHI CLIENT UDP ĐANG Ở TRONG PHÒNG CHAT
        // =====================================================================
        DichVuChat.UDPChatMember curMem = DichVuChat.udpMembers.get(clientAddr);
        if (curMem != null && curMem.currentRoom != null) {
            // Rời phòng chat
            if (cmd.equals("LEAVE") || cmd.equals("/QUIT")) {
                String leftRoom = DichVuChat.roiPhongUDP(clientAddr, socket);
                return "Bạn đã rời phòng " + (leftRoom != null ? leftRoom : curMem.currentRoom) + ". Trở về Menu chính.";
            }
            // Xem thành viên trong phòng hiện tại
            else if (cmd.equals("USERS")) {
                List<String> list = DichVuChat.layDanhSachThanhVienUDP(curMem.currentRoom);
                return "Thành viên phòng [" + curMem.currentRoom + "] (" + list.size() + " người): " + String.join(", ", list);
            }
            // Xem danh sách phòng
            else if (cmd.equals("ROOMS")) {
                return DichVuChat.layDanhSachPhong(false);
            }
            // Mọi nội dung khác: Gửi trực tiếp tin nhắn chat tới các thành viên trong phòng
            else {
                String chatMsg = trimmed.toUpperCase().startsWith("MSG ") ? trimmed.substring(4).trim() : trimmed;
                DichVuChat.broadcastUDP(socket, curMem.currentRoom, "[" + curMem.name + " @" + curMem.currentRoom + "]: " + chatMsg, clientAddr);
                return "[Bạn @" + curMem.currentRoom + "]: " + chatMsg;
            }
        }

        // =====================================================================
        // TRẠNG THÁI 2: KHI Ở NGOÀI MENU CHÍNH
        // =====================================================================
        switch (cmd) {
            // --- MENU TRỢ GIÚP ---
            case "MENU":
            case "HELP":
                return "--- MENU DỊCH VỤ UDP ---\n"
                     + "• Bài 2 : DIGIT <0-9> (hoặc gõ số trực tiếp)\n"
                     + "• Bài 3 : DATE | TIME | DATETIME\n"
                     + "• Bài 4 : CALC <op> <số1> <số2> (ví dụ: CALC + 100 200)\n"
                     + "• Bài 5 : ROOMS | JOIN <phòng> | USERS | MSG <nội dung>\n"
                     + "• Bài 7 : HELLO <id> | LOG <nội dung>\n"
                     + "• Đề 7000: 1 <chuỗi> (Đảo toàn bộ) | 2 <chuỗi> (Đảo từ)";

            // --- BÀI 3: DỊCH VỤ NGÀY GIỜ ---
            case "DATE":
            case "TIME":
            case "DATETIME":
                return DichVuThuVien.xuLyNgayGio(cmd);

            // --- BÀI 4: MÁY TÍNH TỪ XA ---
            case "CALC":
                return DichVuThuVien.xuLyMayTinh(trimmed);

            // --- BÀI 5: XEM DANH SÁCH PHÒNG CHAT ---
            case "ROOMS":
                return DichVuChat.layDanhSachPhong(false);

            // --- BÀI 5: THAM GIA PHÒNG CHAT (JOIN) ---
            case "JOIN":
                if (tokens.length >= 2) {
                    String room = DichVuChat.resolveRoom(tokens[1]);
                    if (room != null) {
                        String currentNick = udpClients.get(clientAddr);

                        // 1. Nếu người dùng gõ kèm tên: JOIN <phòng> <tên>
                        if (tokens.length >= 3) {
                            String newNick = tokens[2].trim();
                            if (!newNick.matches("^[a-zA-Z0-9_-]+$")) {
                                return "ERR INVALID_CLIENT_ID: Tên chỉ nhận chữ, số, '-' và '_'";
                            }
                            if (isUDPNickTaken(newNick, clientAddr)) {
                                return "ERR NICKNAME_TAKEN: Tên '" + newNick + "' đã được người khác sử dụng! Vui lòng chọn tên khác.";
                            }
                            udpClients.put(clientAddr, newNick);
                            currentNick = newNick;
                        }

                        // 2. Nếu chưa đăng ký tên (vẫn là null hoặc chưa HELLO) -> Yêu cầu đăng ký tên trước
                        if (currentNick == null || currentNick.startsWith("User_")) {
                            return ">> Bạn đang vào dịch vụ phòng chat. Vui lòng đăng ký tên trước bằng lệnh:\n"
                                 + "   HELLO <tên> (Ví dụ: HELLO Nam) rồi gõ lại JOIN, hoặc gõ nhanh: JOIN " + room + " <tên>";
                        }

                        // Đưa vào phòng chat UDP và thông báo hệ thống
                        DichVuChat.thamGiaPhongUDP(clientAddr, currentNick, room, socket);
                        return "--> Bạn đã vào phòng UDP [" + room + "] với tên [" + currentNick + "]!\n"
                             + "--> Gõ tin nhắn để chat, 'USERS' xem thành viên, 'LEAVE' để rời phòng.";
                    }
                    return "ERR: Không tìm thấy phòng!";
                }
                return "Cú pháp: JOIN <tên_phòng> hoặc JOIN <số_thứ_tự> (Ví dụ: JOIN Study hoặc JOIN 1)";

            case "LEAVE":
            case "/QUIT":
                return "Bạn hiện không ở trong phòng chat nào.";

            // Xem danh sách các Client UDP đang online
            case "USERS":
                return "Online UDP (" + udpClients.size() + "): " + String.join(", ", udpClients.values());

            // Gửi tin nhắn Broadcast toàn hệ thống trên UDP
            case "MSG":
                String chatMsg = trimmed.length() > 4 ? trimmed.substring(4).trim() : "";
                String senderNick = udpClients.getOrDefault(clientAddr, "User_" + ((InetSocketAddress)clientAddr).getPort());
                broadcastUDP(socket, "[" + senderNick + "]: " + chatMsg, clientAddr);
                return "[Bạn (toàn server)]: " + chatMsg;

            // --- BÀI 7: LƯU NHẬT KÝ & ĐĂNG KÝ NICKNAME (HELLO / NICK) ---
            case "HELLO":
            case "NICK":
                if (tokens.length >= 2) {
                    String newNick = tokens[1];
                    if (!newNick.matches("^[a-zA-Z0-9_-]+$")) {
                        return "ERR INVALID_CLIENT_ID (Chỉ nhận chữ, số, '-' và '_')";
                    }
                    if (isUDPNickTaken(newNick, clientAddr)) {
                        return "ERR NICKNAME_TAKEN: Tên '" + newNick + "' đã được người khác sử dụng! Vui lòng chọn tên khác.";
                    }
                    udpClients.put(clientAddr, newNick);
                    return "OK HELLO " + newNick + " (Đã đăng ký ID UDP thành công!)";
                }
                return "ERR REQUIRE_HELLO_ID (Cú pháp: HELLO <clientId> hoặc NICK <tên>)";

            // Ghi nhật ký vào file server.log
            case "LOG":
                String id = udpClients.getOrDefault(clientAddr, "clientUDP");
                String logText = trimmed.length() > 4 ? trimmed.substring(4).trim() : "";
                return DichVuThuVien.xuLyGhiLog(id, clientAddr.toString(), logText);

            // --- BÀI 2: ĐỔI CHỮ SỐ (Bắt ca biên khoảng trắng, rỗng, 10, 'a') ---
            case "DIGIT":
                String so = tokens.length > 1 ? tokens[1] : "";
                return DichVuThuVien.xuLyDoiSo(so);

            // =================================================================
            // ⭐ TÙY BIẾN DỊCH VỤ TRÊN UDP (ĐỀ 7000):
            // =================================================================
            case "1":
                String s1 = trimmed.length() > 2 ? trimmed.substring(2).trim() : "";
                return "DV1: " + DichVuThuVien.dichVu1_DaoNguocToanBo(s1);

            case "2":
                String s2 = trimmed.length() > 2 ? trimmed.substring(2).trim() : "";
                return "DV2: " + DichVuThuVien.dichVu2_DaoNguocGiuViTri(s2);

            case "3":
                String s3 = trimmed.length() > 2 ? trimmed.substring(2).trim() : "";
                return "DV3 (Tổng từ): " + DichVuThuVien.demSoTu(s3);

            default:
                // Nếu người dùng nhập thẳng 1 ký tự số
                if (raw.length() == 1 && Character.isDigit(raw.charAt(0))) {
                    return DichVuThuVien.xuLyDoiSo(raw);
                }
                return "ERR: Lệnh không hợp lệ! Gõ 'MENU' để xem danh sách dịch vụ.";
        }
    }

    /**
     * KIỂM TRA TRÙNG TÊN UDP:
     * Quét danh sách udpClients xem tên mới có trùng với ai khác không.
     */
    private static boolean isUDPNickTaken(String desiredNick, SocketAddress currentAddr) {
        for (Map.Entry<SocketAddress, String> entry : udpClients.entrySet()) {
            if (!entry.getKey().equals(currentAddr) && entry.getValue().equalsIgnoreCase(desiredNick)) {
                return true;
            }
        }
        return false;
    }

    /**
     * HÀM BROADCAST UDP:
     * Duyệt qua từng địa chỉ SocketAddress của các Client khác và gửi gói tin DatagramPacket tới từng người.
     */
    private static void broadcastUDP(DatagramSocket socket, String msg, SocketAddress except) {
        try {
            byte[] data = msg.getBytes("UTF-8");
            for (SocketAddress target : udpClients.keySet()) {
                if (!target.equals(except)) {
                    socket.send(new DatagramPacket(data, data.length, target));
                }
            }
        } catch (IOException ignored) {}
    }
}
