package bai1;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HelloClient {
    public static final String HOST = "localhost";
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "HelloService";

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            HelloService service = (HelloService) registry.lookup(SERVICE_NAME);

            System.out.println("Connected to HelloServer.");
            String response = service.sayHello();
            System.out.println("Result from server: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


    
}
