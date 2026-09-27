package baitapdexuat.bai5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class TcpChatClient {
    private TcpChatClient() { }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5300;
        try (Socket socket = new Socket(host, port);
             BufferedReader server = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            System.out.print("Nickname: ");
            out.println(console.readLine());
            String greeting = server.readLine();
            if (greeting == null || !greeting.startsWith("OK ")) {
                System.out.println(greeting == null ? "Server disconnected" : greeting);
                return;
            }
            System.out.println(greeting);

            Thread receiver = new Thread(() -> {
                try {
                    for (String message; (message = server.readLine()) != null;) System.out.println(message);
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }, "chat-receiver");
            receiver.setDaemon(true);
            receiver.start();
            System.out.println("Commands: USERS, MSG text, QUIT");
            for (String command; (command = console.readLine()) != null;) {
                out.println(command);
                if (command.equalsIgnoreCase("QUIT")) break;
            }
        } catch (IOException e) {
            System.err.println("Chat connection failed: " + e.getMessage());
        }
    }
}
