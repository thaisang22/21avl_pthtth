package bai9;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AuctionServiceImpl extends UnicastRemoteObject implements AuctionService {
    private final Map<String, Product> productMap;
    private final AtomicInteger idCounter;
    private final List<AuctionListener> listeners;

    public AuctionServiceImpl() throws RemoteException {
        super();
        this.productMap = Collections.synchronizedMap(new LinkedHashMap<>());
        this.idCounter = new AtomicInteger(1);
        this.listeners = new CopyOnWriteArrayList<>();

        // Sản phẩm mẫu
        addProductInternal("iPhone 15 Pro Max 256GB", 20000000.0, "AppleStore_VN");
        addProductInternal("MacBook Pro M3 14 inch", 35000000.0, "TechWorld");
        addProductInternal("Bức tranh sơn dầu cổ điển", 5000000.0, "GalerieArt");
    }

    private Product addProductInternal(String name, double startingPrice, String seller) {
        String id = "SP" + String.format("%03d", idCounter.getAndIncrement());
        Product product = new Product(id, name, startingPrice, seller);
        productMap.put(id, product);
        return product;
    }

    @Override
    public synchronized Product addProduct(String name, double startingPrice, String seller) throws RemoteException {
        Product product = addProductInternal(name, startingPrice, seller);
        System.out.printf("New product registered: [%s] %s by %s with base price %,.2f VND\n",
                product.getId(), product.getName(), seller, startingPrice);

        // Phát thông báo đến các client đang theo dõi
        notifyProductAdded(product);
        return product;
    }

    @Override
    public synchronized List<Product> getProducts() throws RemoteException {
        return new ArrayList<>(productMap.values());
    }

    @Override
    public synchronized Product getProduct(String productId) throws RemoteException {
        return productMap.get(productId.toUpperCase().trim());
    }

    @Override
    public synchronized boolean placeBid(String productId, double bidAmount, String bidderName) throws RemoteException {
        Product product = productMap.get(productId.toUpperCase().trim());
        if (product == null) {
            System.out.println("Place bid failed: Product not found " + productId);
            return false;
        }

        // Giá thầu phải lớn hơn giá cao nhất hiện tại
        if (bidAmount > product.getCurrentHighestBid()) {
            product.setCurrentHighestBid(bidAmount);
            product.setHighestBidder(bidderName);

            System.out.printf("NEW HIGHEST BID! Product [%s] %s: %,.2f VND by %s\n",
                    product.getId(), product.getName(), bidAmount, bidderName);

            // Báo cho các client theo thời gian thực
            notifyNewBid(product.getId(), product.getName(), bidAmount, bidderName);
            return true;
        } else {
            System.out.printf("Place bid rejected! Bid %,.2f <= Current %,.2f for [%s]\n",
                    bidAmount, product.getCurrentHighestBid(), product.getId());
            return false;
        }
    }

    @Override
    public void registerListener(AuctionListener listener) throws RemoteException {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            System.out.println("Registered new listener. Total listeners: " + listeners.size());
        }
    }

    @Override
    public void unregisterListener(AuctionListener listener) throws RemoteException {
        if (listener != null) {
            listeners.remove(listener);
            System.out.println("Unregistered listener. Total listeners: " + listeners.size());
        }
    }

    private void notifyNewBid(String productId, String productName, double newHighestBid, String bidderName) {
        for (AuctionListener listener : listeners) {
            try {
                listener.onNewBidPlaced(productId, productName, newHighestBid, bidderName);
            } catch (Exception e) {
                // Client có thể đã ngắt kết nối
                listeners.remove(listener);
            }
        }
    }

    private void notifyProductAdded(Product product) {
        for (AuctionListener listener : listeners) {
            try {
                listener.onProductAdded(product);
            } catch (Exception e) {
                listeners.remove(listener);
            }
        }
    }
}
