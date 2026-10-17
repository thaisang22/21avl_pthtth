import java.io.*;
import java.net.*;

// Lop ServerTCP: Khoi tao server socket tai port 7000 va lang nghe ket noi tu client
public class ServerTCP {
    // Port 7000 theo dung yeu cau Cau 1 cua de bai
    public static final int PORT = 7000;

    public static void main(String[] args) {
        try {
            // Cau 1: Server tao ServerSocket tai port 7000
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("====================================================");
            System.out.println("Server TCP dang chay va lang nghe tai port: " + PORT);
            System.out.println("Cho yeu cau ket noi tu client...");
            System.out.println("====================================================");

            // Vong lap vo han de server luon san sang chap nhan nhieu client ket noi den
            while (true) {
                // Chap nhan ket noi tu client (lenh nay se block cho den khi co client ket noi)
                Socket socket = serverSocket.accept();
                System.out.println("-> Co client moi ket noi tu: " + socket.getRemoteSocketAddress());

                // Cau 1: Server su dung luong (thread) de quan ly ket noi va cac yeu cau tu client(s)
                // Moi client se duoc phuc vu boi 1 ClientHandler chay tren 1 luong rieng biet
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start(); // Khoi chay luong
            }
        } catch (IOException e) {
            // In loi neu khong the khoi tao server hoac gap su co mang
            System.out.println("Loi server: " + e.getMessage());
        }
    }
}
