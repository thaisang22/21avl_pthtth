package bai9;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AuctionService extends Remote {
    // Dành cho chủ sở hữu (Seller)
    Product addProduct(String name, double startingPrice, String seller) throws RemoteException;

    // Dành cho người đấu giá (Bidder)
    List<Product> getProducts() throws RemoteException;
    Product getProduct(String productId) throws RemoteException;
    boolean placeBid(String productId, double bidAmount, String bidderName) throws RemoteException;

    // Đăng ký nhận thông báo thời gian thực khi có giá thầu mới
    void registerListener(AuctionListener listener) throws RemoteException;
    void unregisterListener(AuctionListener listener) throws RemoteException;
}
