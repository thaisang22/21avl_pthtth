package baitapdexuat.bai3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public final class DateTimeUdpServer {
    private static final int DEFAULT_PORT = 5201;
    private DateTimeUdpServer() { }
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Date-time UDP server listening on port " + port);
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                String command = new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.UTF_8);
                byte[] answer = DateTimeService.process(command).getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(answer, answer.length, request.getAddress(), request.getPort()));
            }
        }
    }
}
