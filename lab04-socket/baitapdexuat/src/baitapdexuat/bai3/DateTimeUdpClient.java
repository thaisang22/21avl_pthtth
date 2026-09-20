package baitapdexuat.bai3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public final class DateTimeUdpClient {
    private static final int TIMEOUT_MS = 3000;
    private DateTimeUdpClient() { }
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5201;
        try (DatagramSocket socket = new DatagramSocket();
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress address = InetAddress.getByName(host);
            System.out.println("Enter DATE, TIME, DATETIME (UDP has no session QUIT):");
            for (String request; (request = console.readLine()) != null;) {
                byte[] data = request.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(data, data.length, address, port));
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    socket.receive(reply);
                    System.out.println(new String(reply.getData(), reply.getOffset(), reply.getLength(), StandardCharsets.UTF_8));
                } catch (SocketTimeoutException e) {
                    System.out.println("No UDP response within " + TIMEOUT_MS + " ms (server may have stopped)." );
                }
            }
        } catch (IOException e) { System.err.println("ERR UDP: " + e.getMessage()); }
    }
}
