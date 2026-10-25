package TCP;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class TCPClient {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", 7000);
             Scanner keyboard = new Scanner(System.in, StandardCharsets.UTF_8.name());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            String line;
            while ((line = in.readLine()) != null && !line.equals("END_MENU")) System.out.println(line);
            while (true) {
                System.out.print("Chon 1-4 hoac EXIT: ");
                String choice = keyboard.nextLine().trim();
                out.println(choice);
                if (choice.equalsIgnoreCase("EXIT")) { System.out.println(in.readLine()); break; }
                if (choice.matches("[1-4]")) {
                    System.out.println("Nhap nhieu dong, go . tren mot dong rieng de gui:");
                    while (true) {
                        String data = keyboard.nextLine();
                        out.println(data);
                        if (data.equals(".")) break;
                    }
                }
                System.out.println("--- KET QUA ---");
                while ((line = in.readLine()) != null && !line.equals("END_RESULT")) System.out.println(line);
                if (line == null) break;
            }
        }
    }
}
