import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * BÀI 7: Server lưu nhật ký tin nhắn (Client)
 */
public class ClientBai7 {
    public static final String HOST = "localhost";
    public static final int PORT = 6007;

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 7 (LƯU NHẬT KÝ) KẾT NỐI TỚI " + HOST + ":" + PORT + " ===");

        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            // Nhận thông điệp chào hỏi đầu tiên từ Server
            String welcome = in.readLine();
            System.out.println("Server < " + welcome);

            while (true) {
                System.out.print("Client > ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine();

                out.println(line);

                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server đã đóng kết nối.");
                    break;
                }
                System.out.println("Server < " + response);

                if (line.trim().equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi Client: " + e.getMessage());
        }
    }
}
