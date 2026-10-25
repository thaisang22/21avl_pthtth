package bai9;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuctionListener extends Remote {
    void onNewBidPlaced(String productId, String productName, double newHighestBid, String bidderName) throws RemoteException;
    void onProductAdded(Product product) throws RemoteException;
}
