import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public String name;
    public String currentRoom;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // gui tin nhan ve client
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // nhap ten
            out.println("Nhap ten: ");
            while (true) {
                name = in.readLine();
                if (name == null) {
                    return;
                }
                name = name.trim();
                if (name.equalsIgnoreCase("/exit")) {
                    return;
                }
                if (name.isEmpty()) {
                    out.println("Nhap ten: ");
                    continue;
                }

                // kiem tra trung ten
                boolean trung = false;
                for (ClientHandler c : ChatServer.clients) {
                    if (c != this && c.name != null && c.name.equalsIgnoreCase(name)) {
                        trung = true;
                        break;
                    }
                }
                if (trung) {
                    out.println("Ten da duoc su dung, nhap lai: ");
                    continue;
                }
                break;
            }

            System.out.println(name + " da ket noi");
            out.println("Xin chao " + name + "!\n");

            // menu phong va chat
            while (true) {
                out.println("Danh sach phong:");
                for (int i = 0; i < ChatServer.rooms.size(); i++) {
                    String r = ChatServer.rooms.get(i);
                    int count = ChatServer.countMembers(r);
                    out.println((i + 1) + ". " + r + " (" + count + " nguoi)");
                }
                out.println();
                out.println("Go /quit de thoat phong || go /eixt de thoat khoi chuong trinh");
                out.println("Chon phong: ");

                String line = in.readLine();
                if (line == null || line.trim().equalsIgnoreCase("/exit")) {
                    break;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // chon phong
                String selectedRoom = null;
                try {
                    int index = Integer.parseInt(line);
                    if (index >= 1 && index <= ChatServer.rooms.size()) {
                        selectedRoom = ChatServer.rooms.get(index - 1);
                    }
                } catch (Exception e) {
                }

                if (selectedRoom == null) {
                    for (String r : ChatServer.rooms) {
                        if (r.equalsIgnoreCase(line)) {
                            selectedRoom = r;
                            break;
                        }
                    }
                }

                // de xuat phong moi
                if (selectedRoom == null) {
                    selectedRoom = line;
                    ChatServer.rooms.add(selectedRoom);
                    System.out.println("Da tao phong: " + selectedRoom);
                }

                currentRoom = selectedRoom;
                System.out.println(name + " vao phong " + currentRoom);
                out.println("Ban da vao phong " + currentRoom + ".");
                ChatServer.broadcast(currentRoom, name + " da tham gia phong.");

                // chat trong phong
                boolean quit = false;
                String msg;
                while ((msg = in.readLine()) != null) {
                    msg = msg.trim();
                    if (msg.equalsIgnoreCase("/quit")) {
                        System.out.println(name + " da roi phong " + currentRoom);
                        ChatServer.broadcast(currentRoom, name + " da roi phong " + currentRoom + ".", this);
                        out.println("Ban da roi phong " + currentRoom + ".\n");
                        currentRoom = null;
                        quit = true;
                        break;
                    }
                    if (msg.equalsIgnoreCase("/exit")) {
                        System.out.println(name + " da roi phong " + currentRoom);
                        ChatServer.broadcast(currentRoom, name + " da roi phong " + currentRoom + ".", this);
                        currentRoom = null;
                        break;
                    }
                    if (!msg.isEmpty()) {
                        ChatServer.broadcast(currentRoom, "[" + name + "]: " + msg);
                    }
                }

                if (!quit) {
                    break;
                }
            }

        } catch (Exception e) {
        } finally {
            try {
                if (currentRoom != null) {
                    System.out.println(name + " da roi phong " + currentRoom);
                    ChatServer.broadcast(currentRoom, name + " da roi phong " + currentRoom + ".", this);
                    currentRoom = null;
                }
                ChatServer.clients.remove(this);
                socket.close();
                if (name != null) {
                    System.out.println(name + " da ngat ket noi");
                }
            } catch (Exception e) {
            }
        }
    }
}
