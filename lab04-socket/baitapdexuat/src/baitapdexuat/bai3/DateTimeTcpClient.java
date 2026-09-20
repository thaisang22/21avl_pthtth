package baitapdexuat.bai3;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class DateTimeTcpClient {
    private DateTimeTcpClient() { }
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5200;
        try (Socket socket = new Socket(host, port);
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            System.out.println("Enter DATE, TIME, DATETIME, or QUIT:");
            for (String request; (request = console.readLine()) != null;) {
                out.println(request);
                String response = in.readLine();
                if (response == null) { System.out.println("Server stopped/closed the TCP connection."); return; }
                System.out.println(response);
                if ("QUIT".equalsIgnoreCase(request)) return;
            }
        } catch (IOException e) { System.err.println("ERR CONNECTION: " + e.getMessage()); }
    }
}
