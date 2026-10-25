package bai9;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AuctionServer {
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "AuctionService";

    public static void main(String[] args) {
        try {
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(PORT);
                System.out.println("RMI Registry created on port " + PORT);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(PORT);
                System.out.println("Using existing RMI Registry on port " + PORT);
            }

            AuctionService service = new AuctionServiceImpl();
            registry.rebind(SERVICE_NAME, service);

            System.out.println("=================================================");
            System.out.println("HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN (AUCTION SERVER) SẴN SÀNG!");
            System.out.println("Dịch vụ đăng ký: '" + SERVICE_NAME + "' trên cổng " + PORT);
            System.out.println("Đang theo dõi các sản phẩm và cập nhật thời gian thực...");
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
