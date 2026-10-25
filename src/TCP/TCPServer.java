package TCP;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TCPServer {
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(7000)) {
            System.out.println("TCP Server dang cho tai cong 7000...");
            while (true) {
                Socket client = server.accept();
                new Thread(() -> serve(client)).start();
            }
        }
    }

    private static void serve(Socket socket) {
        try (Socket client = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {
            out.println("=== MENU DICH VU ===");
            out.println("1. Dao nguoc toan bo chuoi va viet hoa dau tu");
            out.println("2. Dao nguoc tung tu, giu vi tri va viet hoa dau tu");
            out.println("3. Dem tong so tu tat ca cac dong");
            out.println("4. Dem so tu tung dong");
            out.println("END_MENU");
            String choice;
            while ((choice = in.readLine()) != null) {
                if (choice.equalsIgnoreCase("EXIT")) { out.println("Tam biet!"); break; }
                int service;
                try { service = Integer.parseInt(choice); }
                catch (NumberFormatException e) { service = -1; }
                if (service < 1 || service > 4) {
                    out.println("Lua chon khong hop le"); out.println("END_RESULT"); continue;
                }
                StringBuilder text = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !line.equals(".")) {
                    if (text.length() > 0) text.append('\n');
                    text.append(line);
                }
                if (line == null) break;
                out.println(StringServices.process(service, text.toString()));
                out.println("END_RESULT");
            }
        } catch (IOException e) { System.out.println("Client ngat: " + e.getMessage()); }
    }
}
