import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * ĐỀ THI GIỮA KỲ SOCKET TCP - IT.FIT.IUH
 * --------------------------------------------------------------------------
 * CÂU 2: Client gửi yêu cầu và kết nối tới Server qua IP và port cho trước (7000).
 * CÂU 3: Nhận danh sách dịch vụ, chọn dịch vụ và gửi các dòng kết thúc bằng dấu "."
 * CÂU 5: Duy trì kết nối, đóng kết nối bằng cách gửi chuỗi "EXIT."
 */
public class Client7000 {
    // Câu 2: Địa chỉ IP và Port cho trước
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7000;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" [CÂU 2] CLIENT TCP ĐANG KẾT NỐI TỚI " + SERVER_HOST + ":" + SERVER_PORT);
        System.out.println("==================================================");

        try (
            // Câu 2: Kết nối tới Server qua IP và Port
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("--> Đã kết nối thành công tới Server!\n");

            // =========================================================================
            // CÂU 5: DUY TRÌ KẾT NỐI ĐẾN KHI GỬI CHUỖI "EXIT."
            // =========================================================================
            while (true) {
                // 1. Nhận và hiển thị danh sách dịch vụ từ Server
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("END_MENU")) {
                        break; // Đã nhận hết menu
                    }
                    System.out.println(line);
                }

                // 2. Nhập lựa chọn dịch vụ (1, 2, 3, 4 hoặc EXIT.)
                System.out.print("\nChọn dịch vụ (1-4) hoặc 'EXIT.' để thoát: ");
                if (!scanner.hasNextLine()) break;
                String choice = scanner.nextLine().trim();

                // Gửi lựa chọn lên Server
                out.println(choice);

                // Nếu chọn EXIT. thì chuẩn bị thoát
                if (choice.equalsIgnoreCase("EXIT.")) {
                    // Đọc phản hồi tạm biệt từ server
                    while ((line = in.readLine()) != null) {
                        if (line.equals("END_RESPONSE")) break;
                        System.out.println(line);
                    }
                    System.out.println("Đã đóng kết nối Client theo yêu cầu EXIT.");
                    break;
                }

                // Chờ tín hiệu từ Server
                String status = in.readLine();
                if (status == null) break;

                // Nếu lựa chọn không hợp lệ, đọc thông báo lỗi và quay lại menu
                if (!status.equals("READY_FOR_DATA")) {
                    System.out.println(status);
                    while ((line = in.readLine()) != null) {
                        if (line.equals("END_RESPONSE")) break;
                        System.out.println(line);
                    }
                    continue;
                }

                // 3. CÂU 3: Server đã sẵn sàng -> Nhập các dòng chuỗi, dừng khi gặp dấu "."
                System.out.println("\n[NHẬP DỮ LIỆU] Nhập các dòng chuỗi cần xử lý (Gõ dấu '.' trên một dòng riêng để kết thúc):");
                while (true) {
                    System.out.print("> ");
                    if (!scanner.hasNextLine()) break;
                    String dataLine = scanner.nextLine();
                    out.println(dataLine);

                    // Khi người dùng nhập dấu "." hoặc "EXIT." thì dừng gửi dữ liệu
                    if (dataLine.trim().equals(".") || dataLine.trim().equalsIgnoreCase("EXIT.")) {
                        break;
                    }
                }

                // 4. CÂU 4: Nhận và in kết quả từ Server
                System.out.println("\n--- ĐANG CHỜ KẾT QUẢ TỪ SERVER ---");
                while ((line = in.readLine()) != null) {
                    if (line.equals("END_RESPONSE")) {
                        break; // Đã nhận hết kết quả
                    }
                    System.out.println(line);
                }
                System.out.println("----------------------------------\n");
            }

        } catch (IOException e) {
            System.err.println("Lỗi Client: " + e.getMessage());
        }
    }
}
