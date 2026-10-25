package bai5;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatService extends Remote {
    /**
     * Gửi tin nhắn từ client đến server (theo đúng yêu cầu đề bài)
     */
    void sendMessage(String message) throws RemoteException;

    /**
     * Lấy phản hồi mới nhất từ server
     */
    String getLastReply() throws RemoteException;
}
