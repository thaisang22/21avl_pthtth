package bai5;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {
    private final Scanner serverScanner;
    private volatile String lastReply;

    public ChatServiceImpl() throws RemoteException {
        super();
        this.serverScanner = new Scanner(System.in);
        this.lastReply = "";
    }

    @Override
    public synchronized void sendMessage(String message) throws RemoteException {
        System.out.println("\n-------------------------------------------");
        System.out.println("[Client gửi]: " + message);
        System.out.print("[Server nhập phản hồi cho Client] (nhấn Enter để gửi phản hồi mặc định): ");
        
        String reply = "";
        if (serverScanner.hasNextLine()) {
            reply = serverScanner.nextLine().trim();
        }
        if (reply.isEmpty()) {
            reply = "Server đã nhận được tin nhắn: \"" + message + "\"";
        }
        this.lastReply = reply;
        System.out.println("[Server đã gửi]: " + this.lastReply);
    }

    @Override
    public synchronized String getLastReply() throws RemoteException {
        return this.lastReply;
    }
}
