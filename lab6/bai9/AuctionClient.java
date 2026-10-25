package bai9;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

public class AuctionClient {
    public static final String HOST = "localhost";
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "AuctionService";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuctionListener listener = null;
        AuctionService service = null;

        try {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            service = (AuctionService) registry.lookup(SERVICE_NAME);

            System.out.println("=== KẾT NỐI HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN THÀNH CÔNG ===");
            System.out.print("Nhập tên hiển thị của bạn (Ví dụ: Nguyễn Văn A): ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                username = "User_" + (int)(Math.random() * 1000);
            }

            // Tạo callback listener để nhận thông báo real-time
            final String currentUserName = username;
            listener = new AuctionListener() {
                @Override
                public void onNewBidPlaced(String productId, String productName, double newHighestBid, String bidderName) throws RemoteException {
                    System.out.println("\n------------------------------------------------------------");
                    System.out.printf("[THÔNG BÁO REAL-TIME] Sản phẩm [%s] %s có GIÁ THẦU MỚI!\n", productId, productName);
                    System.out.printf(">> Giá cao nhất hiện tại: %,.2f VND (bởi %s%s)\n",
                            newHighestBid, bidderName, bidderName.equals(currentUserName) ? " - BẠN" : "");
                    System.out.println("------------------------------------------------------------");
                    System.out.print("Chọn chức năng: ");
                }

                @Override
                public void onProductAdded(Product product) throws RemoteException {
                    System.out.println("\n------------------------------------------------------------");
                    System.out.printf("[THÔNG BÁO REAL-TIME] SẢN PHẨM MỚI LÊN SÀN: [%s] %s\n", product.getId(), product.getName());
                    System.out.printf(">> Người bán: %s | Giá khởi điểm: %,.2f VND\n", product.getSeller(), product.getStartingPrice());
                    System.out.println("------------------------------------------------------------");
                    System.out.print("Chọn chức năng: ");
                }
            };

            // Export listener để server có thể gọi callback
            UnicastRemoteObject.exportObject(listener, 0);
            service.registerListener(listener);

            System.out.printf("\nChào mừng %s tham gia sàn đấu giá trực tuyến!\n", username);

            while (true) {
                System.out.println("\n===========================================");
                System.out.println("1. [Bidder] Xem danh sách sản phẩm đang đấu giá");
                System.out.println("2. [Bidder] Đặt giá thầu (Bid)");
                System.out.println("3. [Seller] Đăng sản phẩm mới lên sàn");
                System.out.println("0. Thoát");
                System.out.print("Chọn chức năng: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1": {
                        List<Product> products = service.getProducts();
                        System.out.println("\n--- DANH SÁCH SẢN PHẨM ĐANG ĐẤU GIÁ ---");
                        if (products.isEmpty()) {
                            System.out.println("(Chưa có sản phẩm nào trên sàn)");
                        } else {
                            for (Product p : products) {
                                System.out.println(p);
                            }
                        }
                        break;
                    }
                    case "2": {
                        System.out.print("Nhập mã sản phẩm muốn đấu giá (VD: SP001): ");
                        String pId = scanner.nextLine().trim();
                        Product product = service.getProduct(pId);
                        if (product == null) {
                            System.out.println("=> Không tìm thấy sản phẩm có mã: " + pId);
                            break;
                        }

                        System.out.printf("Sản phẩm: %s | Giá cao nhất hiện tại: %,.2f VND\n",
                                product.getName(), product.getCurrentHighestBid());
                        System.out.print("Nhập giá thầu của bạn (phải cao hơn giá hiện tại): ");
                        try {
                            double bid = Double.parseDouble(scanner.nextLine().trim());
                            boolean ok = service.placeBid(product.getId(), bid, username);
                            if (ok) {
                                System.out.println("=> BẠN ĐÃ ĐẶT THẦU THÀNH CÔNG VỚI GIÁ CAO NHẤT!");
                            } else {
                                System.out.println("=> ĐẶT THẦU THẤT BẠI! Giá thầu phải cao hơn giá cao nhất hiện tại.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Vui lòng nhập số tiền hợp lệ!");
                        }
                        break;
                    }
                    case "3": {
                        System.out.print("Nhập tên sản phẩm: ");
                        String name = scanner.nextLine().trim();
                        if (name.isEmpty()) {
                            System.out.println("Tên sản phẩm không được rỗng!");
                            break;
                        }
                        System.out.print("Nhập giá khởi điểm: ");
                        try {
                            double startPrice = Double.parseDouble(scanner.nextLine().trim());
                            if (startPrice <= 0) {
                                System.out.println("Giá khởi điểm phải lớn hơn 0!");
                                break;
                            }
                            Product added = service.addProduct(name, startPrice, username);
                            System.out.printf("=> ĐÃ ĐĂNG SẢN PHẨM THÀNH CÔNG! Mã sản phẩm: %s\n", added.getId());
                        } catch (NumberFormatException e) {
                            System.out.println("Vui lòng nhập giá khởi điểm hợp lệ!");
                        }
                        break;
                    }
                    case "0":
                        System.out.println("Đang thoát chương trình...");
                        try {
                            service.unregisterListener(listener);
                            UnicastRemoteObject.unexportObject(listener, true);
                        } catch (Exception ignored) {}
                        System.out.println("Tạm biệt!");
                        return;
                    default:
                        System.out.println("Lựa chọn không hợp lệ, vui lòng chọn lại!");
                }
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (listener != null) {
                try {
                    if (service != null) service.unregisterListener(listener);
                    UnicastRemoteObject.unexportObject(listener, true);
                } catch (Exception ignored) {}
            }
            scanner.close();
        }
    }
}
