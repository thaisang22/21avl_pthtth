package bai3;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class PrimeClient {
    public static final String HOST = "localhost";
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "PrimeService";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            PrimeService service = (PrimeService) registry.lookup(SERVICE_NAME);
            System.out.println("=== KẾT NỐI PRIME CHECK RMI SERVER THÀNH CÔNG ===");

            while (true) {
                System.out.print("\nNhập số cần kiểm tra nguyên tố (hoặc gõ 'exit' để thoát): ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                int n;
                try {
                    n = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Vui lòng nhập một số nguyên hợp lệ!");
                    continue;
                }

                boolean isPrime = service.isPrime(n);
                if (isPrime) {
                    System.out.printf("=> Số %d LÀ số nguyên tố.\n", n);
                } else {
                    System.out.printf("=> Số %d KHÔNG PHẢI là số nguyên tố.\n", n);
                }
            }
            System.out.println("Đã kết thúc chương trình.");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
