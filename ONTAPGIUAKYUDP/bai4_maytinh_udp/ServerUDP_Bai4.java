import java.net.*;

/**
 * BÀI 4 (BẢN UDP): Máy tính từ xa UDP (Server)
 * Nhận: CALC <toán_tử> <số_1> <số_2>
 * Trả: OK <kết quả> hoặc ERR <mã lỗi>
 */
public class ServerUDP_Bai4 {
    public static final int PORT = 7004;
    public static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 4 UDP (MÁY TÍNH TỪ XA) ĐANG CHẠY CỔNG " + PORT + " ===");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength(), "UTF-8").trim();
                String response = processCalc(request);

                byte[] sendData = response.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                socket.send(sendPacket);
            }
        } catch (Exception e) {
            System.err.println("Lỗi Server UDP Bài 4: " + e.getMessage());
        }
    }

    private static String processCalc(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length != 4 || !parts[0].equalsIgnoreCase("CALC")) {
            return "ERR INVALID_FORMAT";
        }

        String op = parts[1];
        if (!op.equals("+") && !op.equals("-") && !op.equals("*") && !op.equals("/")) {
            return "ERR UNSUPPORTED_OPERATOR";
        }

        double n1, n2;
        try {
            n1 = Double.parseDouble(parts[2]);
            n2 = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            return "ERR INVALID_NUMBER";
        }

        if (op.equals("/") && n2 == 0) {
            return "ERR DIVIDE_BY_ZERO";
        }

        double res = 0;
        switch (op) {
            case "+": res = n1 + n2; break;
            case "-": res = n1 - n2; break;
            case "*": res = n1 * n2; break;
            case "/": res = n1 / n2; break;
        }

        return (res == (long) res) ? ("OK " + (long) res) : ("OK " + res);
    }
}
