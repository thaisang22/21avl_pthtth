package bai5;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ChatServer {
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "ChatService";

    public static void main(String[] args) {
        try {
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(PORT);
                System.out.println("RMI Registry created on port " + PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Using existing RMI Registry on port " + PORT);
            }

            ChatService service = new ChatServiceImpl();
            registry.rebind(SERVICE_NAME, service);

            System.out.println("===========================================");
            System.out.println("ChatServer RMI đã sẵn sàng!");
            System.out.println("Đang chờ client kết nối và gửi tin nhắn...");
            System.out.println("===========================================");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
