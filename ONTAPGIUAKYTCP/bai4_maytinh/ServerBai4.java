import java.io.*;
import java.net.*;

/**
 * BÀI 4: Máy tính từ xa (Server)
 * Định dạng: CALC <toán_tử> <toán_hạng_1> <toán_hạng_2>
 * Các phản hồi chuẩn theo đề thi:
 * - CALC + 100 200      -> OK 300
 * - CALC / 10 0         -> ERR DIVIDE_BY_ZERO
 * - CALC % 10 3         -> ERR UNSUPPORTED_OPERATOR
 * - CALC + a 2          -> ERR INVALID_NUMBER
 * - Thiếu thành phần    -> ERR INVALID_FORMAT
 * - Lệnh QUIT           -> BYE và đóng kết nối
 */
public class ServerBai4 {
    public static final int PORT = 6004;

    public static void main(String[] args) {
        System.out.println("=== SERVER BÀI 4 (MÁY TÍNH TỪ XA) ĐANG CHẠY CỔNG " + PORT + " ===");
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
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.equalsIgnoreCase("QUIT")) {
                    out.println("BYE");
                    break;
                }

                // Tách các thành phần theo khoảng trắng bất kỳ
                String[] parts = line.split("\\s+");

                // Kiểm tra định dạng bắt đầu bằng CALC và có đủ 4 phần tử
                if (parts.length != 4 || !parts[0].equalsIgnoreCase("CALC")) {
                    out.println("ERR INVALID_FORMAT");
                    continue;
                }

                String op = parts[1];
                String sNum1 = parts[2];
                String sNum2 = parts[3];

                // Kiểm tra toán tử hỗ trợ (+, -, *, /)
                if (!op.equals("+") && !op.equals("-") && !op.equals("*") && !op.equals("/")) {
                    out.println("ERR UNSUPPORTED_OPERATOR");
                    continue;
                }

                // Kiểm tra số hợp lệ (hỗ trợ cả số nguyên và số thực)
                double num1, num2;
                try {
                    num1 = Double.parseDouble(sNum1);
                    num2 = Double.parseDouble(sNum2);
                } catch (NumberFormatException e) {
                    out.println("ERR INVALID_NUMBER");
                    continue;
                }

                // Kiểm tra chia cho 0
                if (op.equals("/") && num2 == 0) {
                    out.println("ERR DIVIDE_BY_ZERO");
                    continue;
                }

                // Tính toán kết quả
                double result = 0;
                switch (op) {
                    case "+": result = num1 + num2; break;
                    case "-": result = num1 - num2; break;
                    case "*": result = num1 * num2; break;
                    case "/": result = num1 / num2; break;
                }

                // Format kết quả: nếu là số nguyên thì in dạng nguyên (ví dụ 300 thay vì 300.0)
                if (result == (long) result) {
                    out.println("OK " + (long) result);
                } else {
                    out.println("OK " + result);
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
