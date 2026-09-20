package baitapdexuat.bai2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class DigitWordTcpServer {
    private static final int DEFAULT_PORT = 5100;

    private DigitWordTcpServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Digit-word TCP server listening on port " + port);
            while (true) {
                try (Socket client = server.accept()) {
                    serve(client);
                } catch (IOException e) {
                    System.err.println("Client session error: " + e.getMessage());
                }
            }
        }
    }

    private static void serve(Socket client) throws IOException {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {
            String request;
            while ((request = in.readLine()) != null) {
                out.println(DigitWordService.process(request));
                if ("QUIT".equalsIgnoreCase(request)) {
                    return;
                }
            }
        }
    }
}
