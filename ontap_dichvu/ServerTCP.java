import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * ============================================================================
 * SERVER TCP CHUYÊN DỤNG CHO DỊCH VỤ (KHÔNG CÓ CHAT)
 * ============================================================================
 * - Sử dụng ServerSocket và ThreadPool (ExecutorService) để phục vụ nhiều Client đồng thời.
 * - Hỗ trợ đầy đủ các dạng bài thi thực hành:
 *    + Dạng Menu 1, 2, 3, 4 (Nhập nhiều dòng kết thúc bằng dấu chấm '.')
 *    + Dạng Lệnh trực tiếp: DIGIT, DATE, TIME, CALC, LOG...
 * - Không có dịch vụ Chat -> Code cực kỳ ngắn gọn, dễ học, dễ tùy biến khi đi thi.
 * ============================================================================
 */
public class ServerTCP {
    private static final int PORT = 7000;
    // ThreadPool quản lý tối đa 50 kết nối đồng thời
    private static final ExecutorService pool = Executors.newFixedThreadPool(50);

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   TCP SERVER DỊCH VỤ ĐANG KHỞI ĐỘNG (PORT " + PORT + ")...");
        System.out.println("==================================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(">>> Server đã sẵn sàng tiếp nhận Client!");

            while (true) {
                // Chờ Client kết nối
                Socket clientSocket = serverSocket.accept();
                System.out.println("[KẾT NỐI MỚI] Từ: " + clientSocket.getRemoteSocketAddress());

                // Giao cho một Worker Thread trong ThreadPool xử lý
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Lỗi ServerSocket: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Lớp xử lý từng Client riêng biệt trên một Thread độc lập.
     */
    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                // Đọc dữ liệu từ Client với UTF-8 để hỗ trợ tiếng Việt có dấu
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                // Gửi dữ liệu về Client với auto-flush = true
                PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
            ) {
                // Gửi menu chào mừng khi Client vừa kết nối
                guiMenu(out);

                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    System.out.println("[" + socket.getRemoteSocketAddress() + "] Yêu cầu: " + line);

                    // 1. Kiểm tra lệnh thoát
                    if (line.equalsIgnoreCase("EXIT") || line.equalsIgnoreCase("QUIT") || line.equals("0")) {
                        out.println("Tạm biệt! Đóng kết nối.");
                        out.println("END_RESPONSE");
                        break;
                    }

                    // 2. Kiểm tra nếu Client muốn xem lại Menu
                    if (line.equalsIgnoreCase("MENU") || line.equalsIgnoreCase("HELP")) {
                        guiMenu(out);
                        continue;
                    }

                    // 3. Xử lý các lựa chọn Menu (1, 2, 3, 4) - Chuẩn đề thi 7000:
                    // Client gửi nội dung từng dòng, kết thúc bằng dấu chấm '.' trên 1 dòng riêng.
                    if (line.equals("1") || line.equals("2") || line.equals("3") || line.equals("4")) {
                        xuLyDichVuMenu(line, in, out);
                        continue;
                    }

                    // 4. Xử lý các lệnh trực tiếp (DIGIT, DATE, TIME, CALC, LOG,...)
                    String ketQua = DichVuThuVien.xuLyYeuCau(line);
                    out.println(">>> KẾT QUẢ: " + ketQua);
                    out.println("END_RESPONSE");
                }
            } catch (IOException e) {
                System.out.println("[NGẮT KẾT NỐI] Client: " + socket.getRemoteSocketAddress());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }

        /**
         * Gửi bảng Menu các dịch vụ về cho Client
         */
        private void guiMenu(PrintWriter out) {
            out.println(DichVuThuVien.layMenuDichVu("TCP SERVER"));
            out.println("Vui lòng nhập số (1-4) hoặc gõ lệnh trực tiếp:");
            out.println("END_MENU"); // Dấu hiệu kết thúc menu để Client dừng đọc
        }

        /**
         * Xử lý dịch vụ theo chuẩn đề 7000:
         * Nhận liên tục nhiều dòng từ Client cho đến khi gặp dấu '.' hoặc "EXIT."
         */
        private void xuLyDichVuMenu(String chonMenu, BufferedReader in, PrintWriter out) throws IOException {
            out.println(">>> Đã chọn dịch vụ [" + chonMenu + "]. Nhập từng dòng văn bản (nhập '.' trên 1 dòng riêng để gửi kết quả):");
            out.println("READY_FOR_INPUT");

            java.util.List<String> lines = new java.util.ArrayList<>();
            String subLine;
            while ((subLine = in.readLine()) != null) {
                // Nếu gặp dấu chấm đơn lẻ hoặc EXIT. thì kết thúc nhận dữ liệu
                if (subLine.equals(".") || subLine.equalsIgnoreCase("EXIT.")) {
                    break;
                }
                lines.add(subLine);
            }

            out.println("========== KẾT QUẢ DỊCH VỤ " + chonMenu + " ==========");
            switch (chonMenu) {
                case "1":
                    // DV1: Đảo toàn bộ chuỗi & in hoa ký tự đầu
                    for (String l : lines) {
                        out.println(DichVuThuVien.dichVu1_DaoNguocToanBo(l));
                    }
                    break;
                case "2":
                    // DV2: Đảo từ giữ nguyên vị trí & in hoa ký tự đầu
                    for (String l : lines) {
                        out.println(DichVuThuVien.dichVu2_DaoNguocGiuViTri(l));
                    }
                    break;
                case "3":
                    // DV3: Đếm tổng số từ của tất cả các dòng
                    int tong = DichVuThuVien.dichVu3_DemTongSoTu(lines);
                    out.println("Tổng số lượng từ của tất cả các dòng gửi đến là: " + tong + " từ");
                    break;
                case "4":
                    // DV4: Đếm số từ theo từng dòng
                    java.util.List<String> kq4 = DichVuThuVien.dichVu4_DemTuTungDong(lines);
                    for (String item : kq4) {
                        out.println(item);
                    }
                    break;
            }
            out.println("=========================================");
            out.println("END_RESPONSE");
        }
    }
}
