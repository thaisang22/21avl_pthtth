package bai5;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ChatClient {
    public static final String HOST = "localhost";
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "ChatService";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            ChatService service = (ChatService) registry.lookup(SERVICE_NAME);
            System.out.println("=== KẾT NỐI CHAT RMI SERVER THÀNH CÔNG ===");
            System.out.println("Gõ tin nhắn và nhấn Enter để gửi đến Server (Gõ 'exit' để thoát).");

            while (true) {
                System.out.print("\n[Bạn (Client)]: ");
                String message = scanner.nextLine().trim();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Đã ngắt kết nối chat.");
                    break;
                }
                if (message.isEmpty()) {
                    continue;
                }

                // Gửi tin nhắn đến server theo đúng phương thức interface
                service.sendMessage(message);

                // Nhận phản hồi từ server
                String reply = service.getLastReply();
                System.out.println("[Server phản hồi]: " + reply);
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
