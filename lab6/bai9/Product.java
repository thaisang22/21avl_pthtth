package bai9;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private double startingPrice;
    private double currentHighestBid;
    private String highestBidder;
    private String seller;

    public Product(String id, String name, double startingPrice, String seller) {
        this.id = id;
        this.name = name;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.seller = seller;
        this.highestBidder = "(Chưa có ai đặt thầu)";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(double currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    public String getSeller() {
        return seller;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Khởi điểm: %,.2f VND | Giá cao nhất: %,.2f VND | Đang dẫn đầu: %s | Người bán: %s",
                id, name, startingPrice, currentHighestBid, highestBidder, seller);
    }
}
