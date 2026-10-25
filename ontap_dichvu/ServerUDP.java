import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * ============================================================================
 * SERVER UDP CHUYÊN DỤNG CHO DỊCH VỤ (KHÔNG CÓ CHAT)
 * ============================================================================
 * - Giao thức UDP: Không hướng kết nối (Connectionless), xử lý theo từng gói tin (DatagramPacket).
 * - Sử dụng một DatagramSocket duy nhất trên PORT 7000.
 * - Nhận yêu cầu từ Client -> Gọi hàm xử lý từ DichVuThuVien -> Gửi trả gói tin kết quả.
 * - Không duy trì trạng thái phiên, không có dịch vụ Chat -> Cực kỳ tối giản, hiệu năng cao.
 * ============================================================================
 */
public class ServerUDP {
    public static final int PORT = 7000;
    public static final int BUFFER_SIZE = 4096; // 4KB

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   UDP SERVER DỊCH VỤ ĐANG KHỞI ĐỘNG (PORT " + PORT + ")...");
        System.out.println("==================================================");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println(">>> Server UDP đã sẵn sàng tiếp nhận gói tin!");
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                // Tạo packet để hứng dữ liệu đến
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket); // Chặn chờ gói tin

                SocketAddress clientAddress = receivePacket.getSocketAddress();
                String request = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8).trim();

                System.out.println("[UDP NHẬN] Từ: " + clientAddress + " | Nội dung: " + request);

                // Xử lý yêu cầu
                String responseText;
                if (request.equalsIgnoreCase("MENU") || request.equalsIgnoreCase("HELP")) {
                    responseText = layMenu();
                } else if (request.equalsIgnoreCase("EXIT") || request.equalsIgnoreCase("QUIT") || request.equals("0")) {
                    responseText = "BYE - Đã đóng phiên làm việc UDP.";
                } else {
                    responseText = DichVuThuVien.xuLyYeuCau(request);
                }

                // Gửi kết quả về cho Client
                byte[] sendData = responseText.getBytes(StandardCharsets.UTF_8);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress);
                socket.send(sendPacket);
            }
        } catch (IOException e) {
            System.err.println("Lỗi UDP Server: " + e.getMessage());
        }
    }

    private static String layMenu() {
        return DichVuThuVien.layMenuDichVu("UDP SERVER");
    }
}
