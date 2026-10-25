package bai4;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ContactServer {
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "ContactService";

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

            ContactService service = new ContactServiceImpl();
            registry.rebind(SERVICE_NAME, service);

            System.out.println("ContactServer is running. Registered as '" + SERVICE_NAME + "'.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
