import java.net.*;
import java.util.Scanner;

/**
 * BÀI 4 (BẢN UDP): Máy tính từ xa UDP (Client)
 */
public class ClientUDP_Bai4 {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7004;
    public static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 4 UDP (MÁY TÍNH TỪ XA) GỬI ĐẾN " + SERVER_HOST + ":" + SERVER_PORT + " ===");
        System.out.println("Cú pháp: CALC <toán_tử> <số_1> <số_2> (Ví dụ: CALC + 100 200). Nhập EXIT để thoát.");

        try (
            DatagramSocket socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in)
        ) {
            socket.setSoTimeout(3000);
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            while (true) {
                System.out.print("Client UDP > ");
                if (!scanner.hasNextLine()) break;
                String command = scanner.nextLine().trim();

                if (command.equalsIgnoreCase("EXIT")) {
                    break;
                }

                byte[] sendData = command.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);

                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                try {
                    socket.receive(receivePacket);
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                    System.out.println("Server UDP < " + response);
                } catch (SocketTimeoutException e) {
                    System.err.println("Lỗi: Quá thời gian chờ phản hồi từ Server (Timeout).");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi Client UDP: " + e.getMessage());
        }
    }
}
