package baitapdexuat.bai5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TcpChatServer {
    private static final int DEFAULT_PORT = 5300;
    private static final int THREADS = 50;
    private static final ConcurrentHashMap<String, ChatPeer> CLIENTS = new ConcurrentHashMap<>();

    private TcpChatServer() { }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("TCP chat server listening on port " + port);
            while (true) {
                Socket socket = server.accept();
                pool.execute(() -> handle(socket));
            }
        } finally {
            pool.shutdownNow();
        }
    }

    private static void handle(Socket socket) {
        ChatPeer peer = null;
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            String nickname = in.readLine();
            if (nickname == null || !nickname.matches("[\\p{L}\\p{N}_-]{1,20}")) {
                out.println("ERR INVALID_NICKNAME");
                return;
            }
            peer = new ChatPeer(nickname, out);
            if (CLIENTS.putIfAbsent(nickname, peer) != null) {
                out.println("ERR NICKNAME_TAKEN");
                return;
            }
            out.println("OK Welcome " + nickname + ". Commands: USERS, MSG text, QUIT");
            broadcast("* " + nickname + " joined the chat", peer);

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("USERS")) {
                    Set<String> names = new TreeSet<>(CLIENTS.keySet());
                    peer.send("USERS " + String.join(", ", names));
                } else if (line.equalsIgnoreCase("QUIT")) {
                    peer.send("OK BYE");
                    break;
                } else if (line.regionMatches(true, 0, "MSG ", 0, 4) && !line.substring(4).trim().isEmpty()) {
                    broadcast("[" + nickname + "] " + line.substring(4), peer);
                    peer.send("OK SENT");
                } else {
                    peer.send("ERR USE_USERS_MSG_OR_QUIT");
                }
            }
        } catch (IOException ignored) {
            // A disconnected client is removed in finally; the server keeps serving others.
        } finally {
            if (peer != null && CLIENTS.remove(peer.nickname, peer)) {
                broadcast("* " + peer.nickname + " left the chat", peer);
            }
        }
    }

    private static void broadcast(String message, ChatPeer sender) {
        for (ChatPeer recipient : new ArrayList<>(CLIENTS.values())) {
            if (recipient != sender && !recipient.send(message)) {
                CLIENTS.remove(recipient.nickname, recipient);
                recipient.close();
            }
        }
    }

    private static final class ChatPeer {
        final String nickname;
        private final PrintWriter out;

        ChatPeer(String nickname, PrintWriter out) {
            this.nickname = nickname;
            this.out = out;
        }

        synchronized boolean send(String message) {
            out.println(message);
            return !out.checkError();
        }

        void close() {
            synchronized (this) { out.close(); }
        }
    }
}
