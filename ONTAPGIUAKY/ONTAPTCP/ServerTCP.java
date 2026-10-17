package ONTAPTCP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTCP {
    public static final int PORT = 7000;

    public static void main(String[] args) {
        try {
            ServerSocket serverSoket = new ServerSocket(PORT);
            System.out.println("====================================================");
            System.out.println("Server TCP dang chay va lang nghe tai port: " + PORT);
            System.out.println("Cho yeu cau ket noi tu client...");
            System.out.println("====================================================");

            while (true) {
                Socket socket = serverSoket.accept();
                System.out.println("Co ket noi moi tu" + socket.getRemoteSocketAddress());
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            } catch (IOException e) {
                System.err.println("LOI server" + e.getMessage());
            }
        }
    }
}
