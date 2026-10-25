import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * =========================================================================
 * BỘ KHUNG SERVER TCP TOÀN DIỆN THI GIỮA KỲ (FIT - IUH)
 * -------------------------------------------------------------------------
 * HỖ TRỢ ĐẦY ĐỦ CÁC DẠNG BÀI THI:
 * 1. Đề 7000: Menu 1, 2, 3, 4 (Chuỗi nhận nhiều dòng, kết thúc bằng dấu ".")
 * 2. Bài 2: Đổi chữ số (0-9 thành chữ, bắt đủ ca biên: 10, 'a', chuỗi rỗng)
 * 3. Bài 3: Ngày giờ hệ thống (DATE, TIME, DATETIME dùng DateTimeFormatter)
 * 4. Bài 4: Máy tính từ xa (CALC op n1 n2, bắt lỗi chia 0 DIVIDE_BY_ZERO)
 * 5. Bài 5: Chat đa luồng & Chat phòng (ThreadPool, ConcurrentHashMap, Broadcast)
 * 6. Bài 7: Đăng ký ID & Ghi nhật ký (HELLO clientId, ghi file logs/server.log)
 * =========================================================================
 */
public class ServerTCP {
    // Port mặc định theo đề thi (cổng 7000 hoặc đổi tuỳ theo đề bài)
    public static final int PORT = 7000;

    // SỬ DỤNG THREAD POOL: Giúp tái sử dụng các luồng (Thread), tránh tràn bộ nhớ khi có hàng trăm Client
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    // BẢNG BĂM ĐỒNG THỜI (Thread-Safe): Quản lý Client đang online dạng Map: Nickname (viết thường) -> ClientHandler
    // Dùng ConcurrentHashMap thay vì HashMap thường để tránh xung đột dữ liệu (Race Condition) khi nhiều luồng truy cập cùng lúc
    public static final ConcurrentHashMap<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" [TCP SERVER] ĐANG CHẠY CỔNG " + PORT + " (ĐA LUỒNG THREADPOOL)");
        System.out.println(" ĐÃ TÍCH HỢP ĐẦY ĐỦ TOÀN BỘ CÁC DỊCH VỤ THI GIỮA KỲ!");
        System.out.println("==================================================");

        // ServerSocket lắng nghe các yêu cầu kết nối từ Client gửi tới cổng 7000
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // accept() là lệnh chặn (blocking): dừng lại chờ đến khi có 1 Client kết nối thành công
                Socket clientSocket = serverSocket.accept();
                System.out.println("--> Client kết nối từ: " + clientSocket.getRemoteSocketAddress());

                // Mỗi Client khi kết nối vào sẽ được giao cho 1 ClientHandler xử lý độc lập
                ClientHandler handler = new ClientHandler(clientSocket);
                // Đưa handler vào Thread Pool để chạy ngầm trên một luồng riêng
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Lỗi Server: " + e.getMessage());
        } finally {
            // Khi Server tắt, giải phóng Thread Pool
            threadPool.shutdown();
        }
    }

    /**
     * HÀM BROADCAST TOÀN SERVER:
     * Gửi tin nhắn từ người gửi tới TẤT CẢ các Client khác đang online (ngoại trừ chính người gửi).
     */
    public static void broadcastChat(String message, String senderNick) {
        for (ClientHandler client : activeClients.values()) {
            if (!client.nickname.equalsIgnoreCase(senderNick)) {
                client.gui(message);
            }
        }
    }

    /**
     * HÀM KIỂM TRA TRÙNG TÊN:
     * Quét toàn bộ danh sách online để đảm bảo không ai bị trùng Nickname.
     */
    public static boolean isNicknameTaken(String desiredNick, String currentNick) {
        for (ClientHandler client : activeClients.values()) {
            if (!client.nickname.equalsIgnoreCase(currentNick) && client.nickname.equalsIgnoreCase(desiredNick)) {
                return true;
            }
        }
        return false;
    }

    /**
     * LỚP CLIENTHANDLER: XỬ LÝ RIÊNG BIỆT CHO MỖI CLIENT
     * Implement Runnable để có thể chạy đa luồng thông qua Thread Pool.
     */
    static class ClientHandler implements Runnable {
        private final Socket socket;         // Socket giao tiếp với Client này
        private BufferedReader in;           // Luồng đọc dữ liệu từ Client gửi lên
        private PrintWriter out;             // Luồng gửi dữ liệu phản hồi về Client
        public String nickname;              // Tên định danh (Nickname) của Client
        public String currentRoom = null;    // Phòng chat hiện tại (null nếu đang ở Menu chính)

        public ClientHandler(Socket socket) {
            this.socket = socket;
            // Ban đầu gán tên mặc định theo số cổng (Port) của Client
            this.nickname = "User_" + socket.getPort();
        }

        // Tiện ích gửi 1 dòng tin nhắn về cho Client này
        public void gui(String msg) {
            if (out != null) out.println(msg);
        }

        @Override
        public void run() {
            try {
                // Khởi tạo luồng đọc/ghi dữ liệu dạng ký tự với bảng mã chuẩn UTF-8 (hỗ trợ tiếng Việt không bị lỗi font)
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true); // autoFlush = true

                // Đăng ký Client vào danh sách online của Server và bộ quản lý DichVuChat
                activeClients.put(this.nickname.toLowerCase(), this);
                DichVuChat.dangKyTCP(this.nickname, out);

                // =====================================================================
                // VÒNG LẶP DUY TRÌ KẾT NỐI ĐẾN KHI CLIENT GỬI "EXIT." HOẶC "QUIT"
                // =====================================================================
                while (true) {
                    // CHỈ GỬI MENU KHI Ở NGOÀI MENU CHÍNH (không in lại Menu khi đang ở trong phòng chat)
                    if (this.currentRoom == null) {
                        out.println("----- DANH SACH DICH VU -----");
                        out.println("1. Dao nguoc toan bo chuoi va in hoa ky tu dau (DV1)");
                        out.println("2. Dao nguoc tu va in hoa ky tu dau, giu nguyen vi tri (DV2)");
                        out.println("3. Dem so luong tu cua tat ca cac dong goi den (DV3)");
                        out.println("4. Dem so luong tu tung dong (DV4)");
                        out.println("Lệnh trực tiếp: DIGIT <0-9> | DATE | TIME | CALC <op> <a <b> | ROOMS | JOIN <phòng> | HELLO <id>");
                        out.println("Nhap 'EXIT.' hoac 'QUIT' de dong ket noi");
                        out.println("END_MENU"); // Đánh dấu kết thúc khối Menu để Client biết
                    }

                    // Đọc dòng lệnh gửi lên từ Client
                    String input = in.readLine();
                    if (input == null) break; // Client đóng kết nối hoặc tắt đột ngột

                    // LỆNH THOÁT KHỎI HỆ THỐNG
                    if (input.trim().equalsIgnoreCase("EXIT.") || (this.currentRoom == null && input.trim().equalsIgnoreCase("QUIT"))) {
                        gui("BYE: Tạm biệt bạn! Server đã đóng kết nối.");
                        out.println("END_RESPONSE");
                        break;
                    }

                    String raw = input;
                    String trimmed = input.trim();
                    if (trimmed.isEmpty()) continue;

                    // Tách các từ trong chuỗi lệnh theo khoảng trắng
                    String[] tokens = trimmed.split("\\s+");
                    String cmd = tokens[0].toUpperCase();

                    // =================================================================
                    // ⭐ ĐIỀU PHỐI DỊCH VỤ: TÁCH BẠCH 2 TRẠNG THÁI (TRONG PHÒNG vs NGOÀI MENU)
                    // =================================================================

                    // -----------------------------------------------------------------
                    // TRẠNG THÁI 1: KHI CLIENT ĐANG TRONG PHÒNG CHAT (currentRoom != null)
                    // Ưu tiên chat tự nhiên, mọi nội dung gõ vào đều là tin nhắn gửi phòng
                    // -----------------------------------------------------------------
                    if (this.currentRoom != null) {
                        // Lệnh rời phòng: LEAVE, /QUIT hoặc QUIT
                        if (cmd.equals("LEAVE") || cmd.equals("/QUIT") || cmd.equals("QUIT")) {
                            String leftRoom = DichVuChat.roiPhongTCP(this.nickname);
                            gui("Bạn đã rời phòng " + (leftRoom != null ? leftRoom : this.currentRoom) + ". Trở về Menu chính.");
                            this.currentRoom = null;
                        }
                        // Lệnh xem danh sách thành viên trong phòng hiện tại
                        else if (cmd.equals("USERS")) {
                            List<String> mems = DichVuChat.layDanhSachThanhVienTCP(this.currentRoom);
                            gui("Thành viên phòng [" + this.currentRoom + "] (" + mems.size() + " người): " + String.join(", ", mems));
                        }
                        // Lệnh xem danh sách tất cả các phòng
                        else if (cmd.equals("ROOMS")) {
                            gui(DichVuChat.layDanhSachPhong(true));
                        }
                        // Mọi nội dung khác: Gửi trực tiếp tin nhắn chat vào phòng (hỗ trợ cả có hoặc không có tiền tố MSG)
                        else {
                            String chatText = trimmed.toUpperCase().startsWith("MSG ") ? trimmed.substring(4).trim() : trimmed;
                            // Gửi tới tất cả các bạn khác trong phòng
                            DichVuChat.broadcastTCP(this.currentRoom, "[" + this.nickname + " @" + this.currentRoom + "]: " + chatText, this.nickname);
                            // Phản hồi lại cho chính người gửi xem
                            gui("[Bạn @" + this.currentRoom + "]: " + chatText);
                        }
                        out.println("END_RESPONSE");
                    }

                    // -----------------------------------------------------------------
                    // TRẠNG THÁI 2: KHI Ở NGOÀI MENU CHÍNH (currentRoom == null)
                    // Xử lý các dịch vụ tính toán, chuỗi, ngày giờ, đăng ký...
                    // -----------------------------------------------------------------
                    else {
                        // --- BÀI 3: DỊCH VỤ NGÀY GIỜ ---
                        if (cmd.equals("DATE") || cmd.equals("TIME") || cmd.equals("DATETIME")) {
                            gui(DichVuThuVien.xuLyNgayGio(cmd));
                            out.println("END_RESPONSE");
                        }
                        // --- BÀI 4: MÁY TÍNH TỪ XA (CALC) ---
                        else if (cmd.equals("CALC")) {
                            gui(DichVuThuVien.xuLyMayTinh(trimmed));
                            out.println("END_RESPONSE");
                        }
                        // --- BÀI 5: XEM DANH SÁCH PHÒNG CHAT ---
                        else if (cmd.equals("ROOMS")) {
                            gui(DichVuChat.layDanhSachPhong(true));
                            out.println("END_RESPONSE");
                        }
                        // --- BÀI 5: THAM GIA PHÒNG CHAT (JOIN) ---
                        else if (cmd.equals("JOIN")) {
                            if (tokens.length >= 2) {
                                // Chuẩn hoá tên phòng (hỗ trợ chọn bằng số 1, 2, 3 hoặc tên chữ "Study")
                                String targetRoom = DichVuChat.resolveRoom(tokens[1]);
                                if (targetRoom != null) {

                                    // YÊU CẦU NHẬP TÊN: Nếu Client vẫn mang tên mặc định "User_port"
                                    if (this.nickname.startsWith("User_")) {
                                        // Cách 1: Người dùng truyền kèm tên luôn: JOIN <phòng> <tên> (Ví dụ: JOIN 1 Nam)
                                        if (tokens.length >= 3) {
                                            String desiredNick = tokens[2].trim();
                                            if (!desiredNick.matches("^[a-zA-Z0-9_-]+$")) {
                                                gui("ERR INVALID_CLIENT_ID: Tên không hợp lệ (chỉ nhận chữ, số, '-', '_')");
                                            } else if (isNicknameTaken(desiredNick, this.nickname)) {
                                                gui("ERR NICKNAME_TAKEN: Tên '" + desiredNick + "' đã được người khác sử dụng!");
                                            } else {
                                                activeClients.remove(this.nickname.toLowerCase());
                                                DichVuChat.doiTenTCP(this.nickname, desiredNick, out);
                                                this.nickname = desiredNick;
                                                activeClients.put(this.nickname.toLowerCase(), this);
                                                gui("OK HELLO " + this.nickname + " (Đã đăng ký tên thành công!)");
                                            }
                                        }

                                        // Cách 2: Nếu chưa có tên hợp lệ, dừng lại hỏi Client nhập tên
                                        while (this.nickname.startsWith("User_")) {
                                            gui(">> Bạn đang vào dịch vụ phòng chat. Vui lòng nhập tên của bạn (hoặc HELLO <tên>):");
                                            String lineNick = in.readLine();
                                            if (lineNick == null) return;
                                            lineNick = lineNick.trim();
                                            if (lineNick.toUpperCase().startsWith("HELLO ")) {
                                                lineNick = lineNick.substring(6).trim();
                                            } else if (lineNick.toUpperCase().startsWith("NICK ")) {
                                                lineNick = lineNick.substring(5).trim();
                                            }

                                            // Bắt lỗi ký tự đặc biệt / rỗng
                                            if (lineNick.isEmpty() || !lineNick.matches("^[a-zA-Z0-9_-]+$")) {
                                                gui("ERR INVALID_CLIENT_ID: Tên không được rỗng và chỉ nhận chữ, số, '-', '_'. Thử lại:");
                                                continue;
                                            }
                                            // Chặn trùng tên
                                            if (isNicknameTaken(lineNick, this.nickname)) {
                                                gui("ERR NICKNAME_TAKEN: Tên '" + lineNick + "' đã có người sử dụng! Vui lòng chọn tên khác:");
                                                continue;
                                            }

                                            // Đăng ký tên mới thành công
                                            activeClients.remove(this.nickname.toLowerCase());
                                            DichVuChat.doiTenTCP(this.nickname, lineNick, out);
                                            this.nickname = lineNick;
                                            activeClients.put(this.nickname.toLowerCase(), this);
                                            gui("OK HELLO " + this.nickname + " (Đã đăng ký tên thành công!)");
                                            break;
                                        }
                                    }

                                    // Thiết lập phòng hiện tại và đưa vào danh sách thành viên phòng
                                    this.currentRoom = targetRoom;
                                    DichVuChat.thamGiaPhongTCP(this.nickname, out, this.currentRoom);
                                    gui("--> Bạn đã vào phòng [" + this.currentRoom + "] với tên [" + this.nickname + "]. Đang ở chế độ Chat!");
                                    gui("--> Nhập tin nhắn bất kỳ để chat, gõ 'USERS' xem thành viên, gõ 'LEAVE' để rời phòng.");
                                } else {
                                    gui("ERR: Không tìm thấy phòng!");
                                }
                            } else {
                                gui("Cú pháp: JOIN <tên_phòng> hoặc JOIN <số_thứ_tự> (Ví dụ: JOIN Study hoặc JOIN 1)");
                            }
                            out.println("END_RESPONSE");
                        }
                        else if (cmd.equals("LEAVE") || cmd.equals("/QUIT")) {
                            gui("Bạn hiện không ở trong phòng chat nào.");
                            out.println("END_RESPONSE");
                        }
                        // Xem danh sách Client đang online trên toàn hệ thống
                        else if (cmd.equals("USERS")) {
                            gui("Danh sách Online hệ thống (" + activeClients.size() + "): " + String.join(", ", activeClients.keySet()));
                            out.println("END_RESPONSE");
                        }
                        // Gửi tin nhắn Broadcast toàn hệ thống (khi đang ở ngoài Menu)
                        else if (trimmed.toUpperCase().startsWith("MSG ")) {
                            String chatText = trimmed.substring(4).trim();
                            broadcastChat("[" + this.nickname + "]: " + chatText, this.nickname);
                            gui("[Bạn (toàn server)]: " + chatText);
                            out.println("END_RESPONSE");
                        }
                        // --- BÀI 7 & ĐĂNG KÝ TÊN: HELLO <clientId> hoặc NICK <tên> ---
                        else if (cmd.equals("HELLO") || cmd.equals("NICK") || cmd.equals("NAME")) {
                            if (tokens.length >= 2) {
                                String newId = tokens[1];
                                if (!newId.matches("^[a-zA-Z0-9_-]+$")) {
                                    gui("ERR INVALID_CLIENT_ID (Chỉ nhận chữ, số, '-' và '_')");
                                } else if (isNicknameTaken(newId, this.nickname)) {
                                    gui("ERR NICKNAME_TAKEN: Tên '" + newId + "' đã được người khác sử dụng! Vui lòng chọn tên khác.");
                                } else {
                                    activeClients.remove(this.nickname.toLowerCase());
                                    DichVuChat.doiTenTCP(this.nickname, newId, out);
                                    this.nickname = newId;
                                    activeClients.put(this.nickname.toLowerCase(), this);
                                    gui("OK HELLO " + this.nickname + " (Đã đăng ký tên thành công!)");
                                }
                            } else {
                                gui("ERR REQUIRE_HELLO_ID (Cú pháp: HELLO <tên> hoặc NICK <tên>)");
                            }
                            out.println("END_RESPONSE");
                        }
                        // Ghi log nhật ký vào file server.log
                        else if (cmd.equals("LOG")) {
                            String logContent = trimmed.length() > 4 ? trimmed.substring(4).trim() : "";
                            String remoteAddr = socket.getRemoteSocketAddress().toString();
                            gui(DichVuThuVien.xuLyGhiLog(this.nickname, remoteAddr, logContent));
                            out.println("END_RESPONSE");
                        }
                        // =============================================================
                        // ⭐ ĐỀ THI CHUỖI 7000 (CHỌN 1, 2, 3, 4)
                        // Nhận nhiều dòng dữ liệu từ Client cho đến khi gặp dòng chỉ có dấu "."
                        // =============================================================
                        else if (trimmed.equals("1") || trimmed.equals("2") || trimmed.equals("3") || trimmed.equals("4")) {
                            gui("READY_FOR_DATA"); // Báo cho Client biết Server đã sẵn sàng nhận dữ liệu nhiều dòng

                            ArrayList<String> lines = new ArrayList<>();
                            String l;
                            while ((l = in.readLine()) != null) {
                                if (l.trim().equals(".") || l.trim().equalsIgnoreCase("EXIT.")) break;
                                lines.add(l);
                            }

                            gui("=== KẾT QUẢ DỊCH VỤ " + trimmed + " ===");
                            switch (trimmed) {
                                case "1":
                                    // DV1: Đảo chuỗi toàn bộ
                                    for (String s : lines) gui(DichVuThuVien.dichVu1_DaoNguocToanBo(s));
                                    break;
                                case "2":
                                    // DV2: Đảo từ giữ nguyên vị trí
                                    for (String s : lines) gui(DichVuThuVien.dichVu2_DaoNguocGiuViTri(s));
                                    break;
                                case "3":
                                    // DV3: Đếm tổng số từ của tất cả các dòng
                                    gui("Tổng số từ: " + DichVuThuVien.dichVu3_DemTongSoTu(lines));
                                    break;
                                case "4":
                                    // DV4: Đếm số từ theo từng dòng riêng biệt
                                    for (String item : DichVuThuVien.dichVu4_DemTuTungDong(lines)) gui(item);
                                    break;
                            }
                            out.println("END_RESPONSE");
                        }
                        // --- BÀI 2: ĐỔI CHỮ SỐ (Bắt ca biên khoảng trắng, rỗng, 10, 'a') ---
                        else if (cmd.equals("DIGIT") || ((raw.length() == 1 && Character.isDigit(raw.charAt(0))) || (raw.contains(" ") && raw.trim().length() <= 2))) {
                            String so = cmd.equals("DIGIT") ? (tokens.length > 1 ? tokens[1] : "") : raw;
                            gui(DichVuThuVien.xuLyDoiSo(so));
                            out.println("END_RESPONSE");
                        }
                        else {
                            gui("ERR: Lệnh không hợp lệ! Xem menu để biết cú pháp.");
                            out.println("END_RESPONSE");
                        }
                    }
                }

            } catch (IOException e) {
                // Xử lý khi Client ngắt kết nối đột ngột (tắt ngang ứng dụng, rớt mạng...)
                System.out.println("Client ngắt kết nối: " + this.nickname + " (" + socket.getRemoteSocketAddress() + ")");
            } finally {
                // DỌN DẸP TÀI NGUYÊN BẮT BUỘC (Tránh rò rỉ bộ nhớ / memory leak)
                if (this.currentRoom != null) {
                    this.currentRoom = null;
                }
                DichVuChat.huyDangKyTCP(this.nickname);
                activeClients.remove(this.nickname.toLowerCase());
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
