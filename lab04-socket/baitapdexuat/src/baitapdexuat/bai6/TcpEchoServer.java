package baitapdexuat.bai6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class TcpEchoServer {
    private TcpEchoServer() { }
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 5400 : Integer.parseInt(args[0]);
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("TCP echo server listening on port " + port);
            while (true) {
                try (Socket client = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {
                    for (String message; (message = in.readLine()) != null;) out.println(message);
                }
            }
        }
    }
}
