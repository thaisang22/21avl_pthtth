package UDP;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UDPClient {
    public static void main(String[] args) throws Exception {
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        try (DatagramSocket socket = new DatagramSocket();
             Scanner keyboard = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            socket.setSoTimeout(5000);
            System.out.println("=== MENU DICH VU UDP ===");
            System.out.println("1. Dao nguoc toan chuoi, viet hoa dau tu");
            System.out.println("2. Dao nguoc tung tu, viet hoa dau tu");
            System.out.println("3. Dem tong so tu");
            System.out.println("4. Dem so tu tung dong");
            while (true) {
                System.out.print("Chon 1-4 hoac EXIT: ");
                String choice = keyboard.nextLine().trim();
                String response = request(socket, ip, choice);
                if (choice.equalsIgnoreCase("EXIT")) { System.out.println(response); break; }
                if (!response.equals("OK_CHOICE")) { System.out.println(response); continue; }
                System.out.println("Nhap nhieu dong, go . tren mot dong rieng de gui:");
                while (true) {
                    String line = keyboard.nextLine();
                    response = request(socket, ip, line);
                    if (line.equals(".")) { System.out.println("--- KET QUA ---\n" + response); break; }
                }
            }
        }
    }
    private static String request(DatagramSocket socket, InetAddress ip, String message) throws Exception {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        if (data.length > 8000) throw new IllegalArgumentException("Dong qua dai cho UDP demo");
        socket.send(new DatagramPacket(data, data.length, ip, 7001));
        byte[] response = new byte[8192];
        DatagramPacket packet = new DatagramPacket(response, response.length);
        socket.receive(packet);
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }
}
