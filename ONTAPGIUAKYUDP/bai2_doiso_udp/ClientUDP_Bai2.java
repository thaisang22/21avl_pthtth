import java.net.*;
import java.util.Scanner;

/**
 * BÀI 2 (UDP): Đổi chữ số thành chữ (Client)
 */
public class ClientUDP_Bai2 {
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7002;
    public static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) {
        System.out.println("=== CLIENT BÀI 2 UDP (ĐỔI CHỮ SỐ) GỬI ĐẾN " + SERVER_HOST + ":" + SERVER_PORT + " ===");
        System.out.println("Nhập 1 chữ số từ 0 đến 9 (Nhập EXIT để thoát):");

        try (
            DatagramSocket socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in)
        ) {
            socket.setSoTimeout(3000);
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            while (true) {
                System.out.print("Client UDP > ");
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("EXIT")) {
                    break;
                }

                byte[] sendData = input.getBytes("UTF-8");
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(receivePacket);
                    String res = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
                    System.out.println("Server UDP < " + res);
                } catch (SocketTimeoutException e) {
                    System.err.println("Timeout: Server không phản hồi.");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi Client UDP Bài 2: " + e.getMessage());
        }
    }
}
