import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * BÀI 2: TCP Đổi chữ số thành chữ (Client)
 */
public class ClientBai2 {
    public static final String HOST = "localhost";
    public static final int PORT = 6002;

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 2 (KẾT NỐI TỚI " + HOST + ":" + PORT + ") ===");
        System.out.println("Nhập chữ số từ 0 đến 9 để dịch, hoặc nhập QUIT để thoát:");

        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            while (true) {
                System.out.print("Client > ");
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine();

                out.println(input);

                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server đã đóng kết nối.");
                    break;
                }
                System.out.println("Server < " + response);

                if (input.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối Client: " + e.getMessage());
        }
    }
}
