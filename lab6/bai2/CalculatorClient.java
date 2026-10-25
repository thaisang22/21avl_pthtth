package bai2;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CalculatorClient {
    public static final String HOST = "localhost";
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "CalculatorService";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            CalculatorService service = (CalculatorService) registry.lookup(SERVICE_NAME);
            System.out.println("=== KẾT NỐI CALCULATOR RMI SERVER THÀNH CÔNG ===");

            while (true) {
                System.out.print("\nNhập số nguyên a (hoặc gõ 'exit' để thoát): ");
                String inputA = scanner.nextLine().trim();
                if (inputA.equalsIgnoreCase("exit")) {
                    break;
                }

                int a;
                try {
                    a = Integer.parseInt(inputA);
                } catch (NumberFormatException e) {
                    System.out.println("Vui lòng nhập số nguyên hợp lệ!");
                    continue;
                }

                System.out.print("Nhập số nguyên b: ");
                String inputB = scanner.nextLine().trim();
                int b;
                try {
                    b = Integer.parseInt(inputB);
                } catch (NumberFormatException e) {
                    System.out.println("Vui lòng nhập số nguyên hợp lệ!");
                    continue;
                }

                int result = service.add(a, b);
                System.out.printf("Kết quả từ server: %d + %d = %d\n", a, b, result);
            }
            System.out.println("Đã kết thúc chương trình.");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
