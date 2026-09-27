package tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiClientTcpServer {
    private static final int PORT = 5000; // port server
    private static final int MAX_CLIENT = 20; // litmit client khi kết nối

    public static void main(String[] args) {
        // tạo một ThreadPool để khi 1 client kết nối sẽ được giao cho một thread 
        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENT); 

        // tạo một server socket
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Multi-client server on port" + PORT);
            while (true) { 
                Socket socket = server.accept();                pool.submit(() -> {
                    String client = String.valueOf(socket.getRemoteSocketAddress());
                    System.out.println("Connected:" + client);
                    try (socket) {
                        TcpCommandServer.serve(socket);
                    } catch (IOException e) {
                        System.err.println("Client" + client + "failed" + e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("server err" + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }
}