import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * BÀI 3: TCP Dịch vụ Ngày Giờ (Client)
 */
public class ClientBai3 {
    public static final String HOST = "localhost";
    public static final int PORT = 6003;

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 3 TCP (NGÀY GIỜ) KẾT NỐI " + HOST + ":" + PORT + " ===");
        System.out.println("Lệnh khả dụng: DATE | TIME | DATETIME | QUIT");

        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            while (true) {
                System.out.print("Client > ");
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine().trim();

                out.println(input);

                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server đã ngắt kết nối.");
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
