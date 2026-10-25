package UDP;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UDPServer {
    // UDP khong co ket noi. Luu lua chon va du lieu theo tung dia chi client.
    private static class Session {
        int service = 0;
        List<String> lines = new ArrayList<>();
    }
    public static void main(String[] args) throws Exception {
        Map<String, Session> sessions = new HashMap<>();
        try (DatagramSocket server = new DatagramSocket(7001)) {
            System.out.println("UDP Server dang cho tai cong 7001...");
            byte[] buffer = new byte[8192];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                server.receive(packet);
                String msg = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                String key = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                Session session = sessions.computeIfAbsent(key, k -> new Session());
                String response;
                if (msg.equalsIgnoreCase("EXIT")) {
                    sessions.remove(key);
                    response = "Tam biet!";
                } else if (session.service == 0) {
                    try { session.service = Integer.parseInt(msg); }
                    catch (NumberFormatException e) { session.service = 0; }
                    if (session.service < 1 || session.service > 4) {
                        session.service = 0; response = "Lua chon khong hop le";
                    } else { session.lines.clear(); response = "OK_CHOICE"; }
                } else if (msg.equals(".")) {
                    response = StringServices.process(session.service, String.join("\n", session.lines));
                    session.service = 0;
                    session.lines.clear();
                } else {
                    session.lines.add(msg);
                    response = "OK_LINE";
                }
                byte[] data = response.getBytes(StandardCharsets.UTF_8);
                server.send(new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort()));
            }
        }
    }
}
