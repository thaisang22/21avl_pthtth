import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * BÀI 4: Máy tính từ xa (Client)
 */
public class ClientBai4 {
    public static final String HOST = "localhost";
    public static final int PORT = 6004;

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 4 (MÁY TÍNH TỪ XA) KẾT NỐI " + HOST + ":" + PORT + " ===");
        System.out.println("Cú pháp: CALC <toán_tử> <số_1> <số_2> (Ví dụ: CALC + 100 200)");
        System.out.println("Nhập QUIT để thoát.");

        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            while (true) {
                System.out.print("Client > ");
                if (!scanner.hasNextLine()) break;
                String command = scanner.nextLine().trim();

                out.println(command);

                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server đã đóng kết nối.");
                    break;
                }
                System.out.println("Server < " + response);

                if (command.equalsIgnoreCase("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối Client: " + e.getMessage());
        }
    }
}
