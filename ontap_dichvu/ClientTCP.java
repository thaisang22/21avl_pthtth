import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * ============================================================================
 * CLIENT TCP CHUYÊN DỤNG CHO DỊCH VỤ (KHÔNG CÓ CHAT)
 * ============================================================================
 * - Kết nối đến ServerTCP (localhost:7000).
 * - Sử dụng 2 luồng:
 *    1. Luồng phụ (Receiver Thread): Lắng nghe kết quả trả về từ Server và in ra màn hình.
 *    2. Luồng chính (Main Thread): Nhập lệnh từ bàn phím và gửi lên Server.
 * - Hỗ trợ nhập nhiều dòng kết thúc bằng '.' khi chọn menu 1, 2, 3, 4 (Đề thi 7000).
 * - Hỗ trợ các lệnh trực tiếp: DIGIT, DATE, TIME, CALC, LOG...
 * ============================================================================
 */
public class ClientTCP {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7000;

    public static void inMenuClient() {
        System.out.println(DichVuThuVien.layMenuDichVu("TCP CLIENT"));
    }

    public static void main(String[] args) {
        System.out.println("--> Đang kết nối tới TCP Server " + SERVER_HOST + ":" + SERVER_PORT + "...");

        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            Scanner scanner = new Scanner(System.in, "UTF-8")
        ) {
            System.out.println("--> Kết nối thành công tới Server!");

            // 1. LUỒNG PHỤ: Đón nhận kết quả xử lý từ Server gửi về
            Thread receiverThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        // Bỏ qua các cờ báo hiệu nội bộ
                        if (msg.equals("END_MENU") || msg.equals("END_RESPONSE") || msg.equals("READY_FOR_INPUT")) {
                            continue;
                        }
                        System.out.println(msg);
                    }
                } catch (IOException ignored) {
                    // Khi Socket đóng, thoát luồng êm đẹp
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

            // Chờ một chút để Server gửi menu đầu tiên
            Thread.sleep(200);

            // 2. LUỒNG CHÍNH: Nhập từ bàn phím và gửi lên Server
            while (true) {
                System.out.print("Client > ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) continue;

                // Xem lại Menu cục bộ
                if (line.equalsIgnoreCase("MENU") || line.equalsIgnoreCase("HELP")) {
                    inMenuClient();
                    continue;
                }

                // Gửi lệnh lên Server
                out.println(line);

                // Nếu chọn 1, 2, 3, 4 (Đề 7000) -> Chuyển sang chế độ nhập nhiều dòng
                if (line.equals("1") || line.equals("2") || line.equals("3") || line.equals("4")) {
                    System.out.println("[NHẬP DỮ LIỆU] Gõ từng dòng văn bản (Gõ dấu '.' trên 1 dòng riêng để gửi):");
                    while (true) {
                        System.out.print("> ");
                        if (!scanner.hasNextLine()) break;
                        String dataLine = scanner.nextLine();
                        out.println(dataLine);
                        if (dataLine.trim().equals(".") || dataLine.trim().equalsIgnoreCase("EXIT.")) {
                            break;
                        }
                    }
                }

                // Lệnh thoát
                if (line.equals("0") || line.equalsIgnoreCase("EXIT") || line.equalsIgnoreCase("QUIT")) {
                    Thread.sleep(300); // Đợi server gửi lời chào tạm biệt
                    break;
                }
            }

            System.out.println("--> Đã đóng kết nối Client. Kết thúc chương trình.");
        } catch (Exception e) {
            System.err.println("Lỗi Client TCP: " + e.getMessage());
        }
    }
}
