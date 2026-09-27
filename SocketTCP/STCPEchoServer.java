import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class STCPEchoServer {
    public static final int SERVER_PORT = 9999;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server đã được tạo");


            // thêm thông tin client nào đã kết nối 
            while (true) {
                try (Socket socket = serverSocket.accept();
                     OutputStream os = socket.getOutputStream();
                     InputStream is = socket.getInputStream()) {
                    int ch;
                    while ((ch = is.read()) != -1) {
                        System.out.println((char) ch);
                        os.write(ch);
                    }
                } catch (IOException e) {
                    System.out.println("Connection Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Server Creation Error: " + e.getMessage());
        }
    }
}