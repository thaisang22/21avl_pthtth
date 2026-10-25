package bai8;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class FlightBookingServer {
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "FlightBookingService";

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

            FlightBookingService service = new FlightBookingServiceImpl();
            registry.rebind(SERVICE_NAME, service);

            System.out.println("===========================================");
            System.out.println("FlightBookingServer is running!");
            System.out.println("Các chuyến bay có sẵn ban đầu: VN123 (50 ghế), VJ456 (100 ghế), QH789 (30 ghế).");
            System.out.println("Registered as '" + SERVICE_NAME + "'.");
            System.out.println("===========================================");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
