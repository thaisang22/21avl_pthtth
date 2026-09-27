package baitapdexuat.bai6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public final class UdpEchoServer {
    private UdpEchoServer() { }
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 5401 : Integer.parseInt(args[0]);
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP echo server listening on port " + port);
            byte[] buffer = new byte[2048];
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                DatagramPacket response = new DatagramPacket(request.getData(), request.getOffset(), request.getLength(),
                        request.getAddress(), request.getPort());
                socket.send(response);
            }
        }
    }
}
