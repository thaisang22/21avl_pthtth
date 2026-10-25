package bai6;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankServer {
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "BankService";
    public static final double INITIAL_BALANCE = 5000000.0; // 5,000,000 VND

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

            BankService service = new BankServiceImpl(INITIAL_BALANCE);
            registry.rebind(SERVICE_NAME, service);

            System.out.printf("BankServer is running. Initial balance: %,.2f VND. Registered as '%s'.\n",
                    INITIAL_BALANCE, SERVICE_NAME);
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
