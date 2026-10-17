import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    public static final int PORT = 5000;
    public static ArrayList<String> rooms = new ArrayList<>();
    public static ArrayList<ClientHandler> clients = new ArrayList<>();

    // khoi tao phong mac dinh
    public static void initRooms() {
        rooms.add("Study");
        rooms.add("Game");
        rooms.add("TraiNghiem");
    }

    // dem so nguoi trong phong
    public static int countMembers(String room) {
        int count = 0;
        for (ClientHandler c : clients) {
            if (room.equalsIgnoreCase(c.currentRoom)) {
                count++;
            }
        }
        return count;
    }

    // gui tin nhan cho tat ca nguoi trong phong
    public static void broadcast(String room, String msg) {
        for (ClientHandler c : clients) {
            if (room.equalsIgnoreCase(c.currentRoom)) {
                c.sendMessage(msg);
            }
        }
    }

    // gui tin nhan cho nhung nguoi khac trong phong
    public static void broadcast(String room, String msg, ClientHandler except) {
        for (ClientHandler c : clients) {
            if (c != except && room.equalsIgnoreCase(c.currentRoom)) {
                c.sendMessage(msg);
            }
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Server dang chay tai cong " + PORT);

            initRooms();
            for (String r : rooms) {
                System.out.println("Da tao phong: " + r);
            }
            System.out.println("Dang cho client ket noi...\n");

            while (true) {
                Socket socket = server.accept();
                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            System.out.println("Loi server: " + e.getMessage());
        }
    }
}
