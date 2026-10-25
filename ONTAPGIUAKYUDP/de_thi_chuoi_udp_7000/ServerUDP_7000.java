import java.net.*;
import java.util.*;

/**
 * ĐỀ THI CHUỖI IUH (PHIÊN BẢN UDP - CỔNG 7000)
 * -------------------------------------------------------------
 * Đầy đủ 4 dịch vụ theo đúng đề thi:
 * - DV 1: Đảo toàn bộ chuỗi và in hoa ký tự đầu
 * - DV 2: Đảo từ giữ nguyên vị trí và in hoa ký tự đầu
 * - DV 3: Đếm tổng số từ
 * - DV 4: Đếm số từ từng dòng
 */
public class ServerUDP_7000 {
    public static final int PORT = 7000;
    public static final int BUFFER_SIZE = 2048;

    public static void main(String[] args) {
        System.out.println("=== SERVER ĐỀ THI CHUỖI UDP ĐANG CHẠY CỔNG " + PORT + " ===");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength(), "UTF-8").trim();
                System.out.println("--> Nhận yêu cầu UDP từ " + packet.getSocketAddress() + ": " + request);

                String response = xuLyDeThi(request);

                byte[] sendData = response.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                socket.send(sendPacket);
            }
        } catch (Exception e) {
            System.err.println("Lỗi Server UDP: " + e.getMessage());
        }
    }

    private static String inHoaChuDau(String w) {
        if (w == null || w.isEmpty()) return "";
        if (w.length() == 1) return w.toUpperCase();
        return Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase();
    }

    private static String xuLyDeThi(String request) {
        if (request.equalsIgnoreCase("MENU")) {
            return "--- DANH SÁCH DỊCH VỤ UDP ---\n"
                 + "1 <chuỗi>: Đảo ngược toàn bộ chuỗi & in hoa chữ đầu\n"
                 + "2 <chuỗi>: Đảo ngược từ giữ nguyên vị trí & in hoa chữ đầu\n"
                 + "3 <dòng 1 \n dòng 2...>: Đếm tổng số từ\n"
                 + "4 <dòng 1 \n dòng 2...>: Đếm số từ từng dòng";
        }

        if (request.startsWith("1 ")) {
            String str = request.substring(2).trim();
            String[] words = str.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = words.length - 1; i >= 0; i--) {
                sb.append(inHoaChuDau(new StringBuilder(words[i]).reverse().toString()));
                if (i > 0) sb.append(" ");
            }
            return "DV1: " + sb.toString();
        }

        if (request.startsWith("2 ")) {
            String str = request.substring(2).trim();
            String[] words = str.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                sb.append(inHoaChuDau(new StringBuilder(words[i]).reverse().toString()));
                if (i < words.length - 1) sb.append(" ");
            }
            return "DV2: " + sb.toString();
        }

        if (request.startsWith("3 ")) {
            String content = request.substring(2);
            String[] words = content.trim().split("\\s+");
            return "Tổng số từ: " + (content.trim().isEmpty() ? 0 : words.length);
        }

        if (request.startsWith("4 ")) {
            String content = request.substring(2);
            String[] lines = content.split("###|\\n");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                String l = lines[i].trim();
                int cnt = l.isEmpty() ? 0 : l.split("\\s+").length;
                sb.append("Dòng ").append(i + 1).append(": ").append(cnt).append(" từ\n");
            }
            return sb.toString().trim();
        }

        return "Cú pháp: MENU | 1 <chuỗi> | 2 <chuỗi> | 3 <nội dung>";
    }
}
