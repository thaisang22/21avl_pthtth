import java.io.*;
import java.net.*;
import java.util.*;

/**
 * ĐỀ THI GIỮA KỲ SOCKET TCP - IT.FIT.IUH
 * --------------------------------------------------------------------------
 * CÂU 1: Server lắng nghe tại port 7000, sử dụng đa luồng (Thread) quản lý client.
 * CÂU 3: Gửi danh sách 4 dịch vụ, nhận các dòng dữ liệu dừng lại khi gặp dấu "."
 * CÂU 4: Xử lý yêu cầu và gửi trả kết quả chính xác về cho client.
 * CÂU 5: Duy trì kết nối liên tục, chỉ đóng kết nối khi nhận chuỗi "EXIT."
 */
public class Server7000 {
    // Câu 1: Server tạo socket tại port 7000
    public static final int PORT = 7000;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" [CÂU 1] SERVER TCP KHỞI ĐỘNG TẠI CỔNG " + PORT);
        System.out.println(" Sẵn sàng chấp nhận và tạo luồng phục vụ client...");
        System.out.println("==================================================");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Chờ yêu cầu kết nối từ client
                Socket clientSocket = serverSocket.accept();
                System.out.println("--> [CÂU 1] Client đã kết nối từ: " + clientSocket.getRemoteSocketAddress());

                // Câu 1: Sử dụng luồng (Thread) để quản lý từng client độc lập
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server: " + e.getMessage());
        }
    }

    // Lớp xử lý luồng riêng cho từng Client
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Khởi tạo luồng I/O UTF-8
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                // =========================================================================
                // CÂU 5 (Quan trọng): Vòng lặp duy trì kết nối cho đến khi nhận chuỗi "EXIT."
                // =========================================================================
                while (true) {
                    // CÂU 3: Server gửi danh sách các dịch vụ cung cấp tới client
                    out.println("----- DANH SACH DICH VU -----");
                    out.println("1. Dao nguoc toan bo chuoi va in hoa ky tu dau cua tung tu");
                    out.println("2. Dao nguoc tu va in hoa ky tu dau cua tung tu, giu nguyen vi tri");
                    out.println("3. Dem so luong tu cua tat ca cac dong goi den");
                    out.println("4. Dem so luong tu tung dong");
                    out.println("Nhap 'EXIT.' de dong ket noi");
                    out.println("END_MENU"); // Đánh dấu đã gửi xong menu

                    // Đọc lựa chọn dịch vụ từ Client
                    String choice = in.readLine();
                    if (choice == null) {
                        break; // Client ngắt kết nối đột ngột
                    }

                    choice = choice.trim();

                    // CÂU 5: Client đóng kết nối bằng cách gửi chuỗi "EXIT."
                    if (choice.equalsIgnoreCase("EXIT.")) {
                        System.out.println("Client " + socket.getRemoteSocketAddress() + " yêu cầu EXIT.");
                        out.println("Tạm biệt! Server đã đóng kết nối.");
                        out.println("END_RESPONSE");
                        break; // Thoát vòng lặp để đóng socket
                    }

                    // Kiểm tra lựa chọn hợp lệ (từ 1 đến 4)
                    if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3") && !choice.equals("4")) {
                        out.println("ERR: Lựa chọn không hợp lệ! Vui lòng chọn từ 1 đến 4 hoặc nhập EXIT.");
                        out.println("END_RESPONSE");
                        continue; // Gửi lại menu
                    }

                    // Báo cho Client biết Server đã sẵn sàng nhận các dòng dữ liệu
                    out.println("READY_FOR_DATA");

                    // CÂU 3: Nhận các dòng chuỗi cho đến khi nhận được dấu "."
                    ArrayList<String> lines = new ArrayList<>();
                    String line;
                    while ((line = in.readLine()) != null) {
                        // Nếu dòng là dấu "." -> kết thúc nhập liệu
                        if (line.trim().equals(".")) {
                            break;
                        }
                        // Nếu trong lúc nhập liệu người dùng nhập EXIT. thì cũng ngắt
                        if (line.trim().equalsIgnoreCase("EXIT.")) {
                            break;
                        }
                        lines.add(line);
                    }

                    // =========================================================================
                    // CÂU 4: Server xử lý yêu cầu và gửi trả kết quả thành công đến client
                    // =========================================================================
                    out.println("=== KẾT QUẢ XỬ LÝ (DỊCH VỤ " + choice + ") ===");
                    switch (choice) {
                        case "1":
                            // Dịch vụ 1: Đảo ngược toàn bộ chuỗi và in hoa ký tự đầu từng từ
                            // Ví dụ: PHAT TRIEN HE THONG TICH HOP -> Poh Hcit Gnoht Eh Neirt Tahp
                            for (String l : lines) {
                                out.println(xuLyDichVu1(l));
                            }
                            break;

                        case "2":
                            // Dịch vụ 2: Đảo ngược từ và in hoa ký tự đầu từng từ, giữ nguyên vị trí
                            // Ví dụ: PHAT TRIEN HE THONG TICH HOP -> Tahp Neirt Eh Gnoht Hcit Poh
                            for (String l : lines) {
                                out.println(xuLyDichVu2(l));
                            }
                            break;

                        case "3":
                            // Dịch vụ 3: Đếm số lượng từ của tất cả các dòng gửi đến
                            int tongSoTu = 0;
                            for (String l : lines) {
                                tongSoTu += demTuTrongChuoi(l);
                            }
                            out.println("Tổng số lượng từ của tất cả các dòng: " + tongSoTu);
                            break;

                        case "4":
                            // Dịch vụ 4: Đếm số lượng từ từng dòng
                            for (int i = 0; i < lines.size(); i++) {
                                int count = demTuTrongChuoi(lines.get(i));
                                out.println("Dòng " + (i + 1) + ": " + count + " từ");
                            }
                            break;
                    }

                    // Báo hiệu đã gửi xong kết quả của lần xử lý này
                    out.println("END_RESPONSE");
                }

            } catch (IOException e) {
                System.out.println("Client ngắt kết nối: " + socket.getRemoteSocketAddress());
            } finally {
                try {
                    socket.close();
                    System.out.println("Đã đóng kết nối với Client: " + socket.getRemoteSocketAddress());
                } catch (IOException ignored) {}
            }
        }

        // =====================================================================
        // CÁC HÀM XỬ LÝ LOGIC CHUẨN ĐỀ THI
        // =====================================================================

        // Hàm viết hoa chữ cái đầu, chữ cái sau viết thường
        private String inHoaChuDau(String w) {
            if (w == null || w.isEmpty()) return "";
            if (w.length() == 1) return w.toUpperCase();
            return Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase();
        }

        // Dịch vụ 1: Đảo ngược toàn bộ chuỗi và in hoa ký tự đầu
        private String xuLyDichVu1(String str) {
            if (str == null || str.trim().isEmpty()) return "";
            String[] words = str.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            // Duyệt từ cuối về đầu để đảo toàn bộ thứ tự từ
            for (int i = words.length - 1; i >= 0; i--) {
                String reversedWord = new StringBuilder(words[i]).reverse().toString();
                sb.append(inHoaChuDau(reversedWord));
                if (i > 0) sb.append(" ");
            }
            return sb.toString();
        }

        // Dịch vụ 2: Đảo ngược từ và in hoa chữ đầu, giữ nguyên vị trí
        private String xuLyDichVu2(String str) {
            if (str == null || str.trim().isEmpty()) return "";
            String[] words = str.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            // Duyệt từ đầu đến cuối để giữ nguyên vị trí từ
            for (int i = 0; i < words.length; i++) {
                String reversedWord = new StringBuilder(words[i]).reverse().toString();
                sb.append(inHoaChuDau(reversedWord));
                if (i < words.length - 1) sb.append(" ");
            }
            return sb.toString();
        }

        // Đếm số từ trong 1 chuỗi
        private int demTuTrongChuoi(String str) {
            if (str == null || str.trim().isEmpty()) return 0;
            return str.trim().split("\\s+").length;
        }
    }
}
