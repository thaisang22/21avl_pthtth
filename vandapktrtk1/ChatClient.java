import java.io.*;
import java.net.*;

/**
 * ============================================================================
 * LỚP CHATCLIENT: Ứng dụng phía người dùng để tham gia hệ thống Chat qua TCP Socket
 * ============================================================================
 * Kiến trúc hoạt động ĐA LUỒNG (2 Threads chạy song song):
 * 1. LUỒNG PHỤ (readThread): Chuyên nhận và hiển thị tin nhắn từ Server về màn hình Console.
 * 2. LUỒNG CHÍNH (Main Thread): Chuyên đợi người dùng gõ phím và gửi nội dung lên Server.
 * 
 * TẠI SAO CẦN 2 LUỒNG TRÊN CLIENT? (Câu hỏi vấn đáp trọng tâm):
 * Vì lệnh `console.readLine()` (chờ gõ phím) và `in.readLine()` (chờ server gửi tin) đều là 
 * các thao tác Blocking (dừng chờ). Nếu chỉ dùng 1 luồng, khi người dùng đang suy nghĩ chưa gõ phím,
 * Client sẽ bị đứng yên và không thể nhận được tin nhắn chat tức thời của những người khác!
 */
public class ChatClient {
    // Địa chỉ IP của máy chủ Server (localhost = 127.0.0.1, tức máy hiện tại)
    public static String HOST = "localhost";

    // Cổng kết nối TCP trùng khớp với PORT trên ChatServer
    public static int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Dang ket noi den " + HOST + ":" + PORT + "...");

        try {
            // Bước 1: Yêu cầu thiết lập kết nối TCP tới Server (gửi gói tin bắt tay 3 bước TCP SYN)
            Socket socket = new Socket(HOST, PORT);

            // Luồng đọc văn bản từ bàn phím người dùng nhập vào
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

            // Luồng đọc văn bản từ Socket mà Server truyền về
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            // Luồng gửi dữ liệu văn bản từ Client lên Server.
            // Tham số true bật chế độ 'autoFlush', tự động đẩy dữ liệu đi ngay sau mỗi lần out.println()
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            System.out.println("Ket noi thanh cong!\n");

            // =========================================================================
            // LUỒNG 1: TIỂU TRÌNH PHỤ (READ THREAD) - NHẬN TIN NHẮN TỪ SERVER
            // =========================================================================
            Thread readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String msg;
                        // Vòng lặp liên tục chờ nhận từng dòng tin nhắn từ Server
                        while ((msg = in.readLine()) != null) {
                            // Xử lý giao diện: Nếu câu hỏi nhập liệu kết thúc bằng ": " (vd: "Nhap ten: ", "Chon phong: ")
                            // Dùng print thay vì println để con trỏ chuột dừng ngay sau dấu hai chấm cho người dùng gõ
                            if (msg.endsWith(": ")) {
                                System.out.print(msg);
                            } else {
                                System.out.println(msg);
                            }
                        }
                    } catch (IOException e) {
                        // Ngoại lệ xảy ra khi kết nối bị ngắt hoặc socket.close() từ luồng chính
                    }
                }
            });
            // Bắt đầu chạy luồng phụ ở chế độ ngầm
            readThread.start();

            // =========================================================================
            // LUỒNG 2: TIỂU TRÌNH CHÍNH (MAIN THREAD) - ĐỌC BÀN PHÍM VÀ GỬI LÊN SERVER
            // =========================================================================
            String input;
            while ((input = console.readLine()) != null) {
                // Gửi nội dung đã gõ qua mạng lên Server
                out.println(input);

                // Nếu người dùng gõ lệnh thoát /exit
                if (input.equalsIgnoreCase("/exit")) {
                    // Tạm dừng 200ms để đảm bảo gói tin /exit đã kịp gửi đi qua Socket trước khi đóng
                    Thread.sleep(200);
                    break;
                }
            }

            // Đóng socket giải phóng cổng mạng
            socket.close();

        } catch (Exception e) {
            System.out.println("Loi ket noi: " + e.getMessage());
        }
    }
}
