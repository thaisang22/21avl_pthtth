package bai6;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class BankClient {
    public static final String HOST = "localhost";
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "BankService";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            BankService service = (BankService) registry.lookup(SERVICE_NAME);
            System.out.println("=== KẾT NỐI HỆ THỐNG NGÂN HÀNG RMI THÀNH CÔNG ===");

            while (true) {
                System.out.println("\n-------------------------------------------");
                System.out.println("1. Kiểm tra số dư tài khoản");
                System.out.println("2. Gửi tiền (Deposit)");
                System.out.println("3. Rút tiền (Withdraw)");
                System.out.println("0. Thoát");
                System.out.print("Chọn chức năng: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        double balance = service.getBalance();
                        System.out.printf("=> Số dư hiện tại của bạn: %,.2f VND\n", balance);
                        break;
                    case "2":
                        System.out.print("Nhập số tiền muốn gửi: ");
                        try {
                            double depositAmount = Double.parseDouble(scanner.nextLine().trim());
                            service.deposit(depositAmount);
                            System.out.printf("=> Nạp thành công %,.2f VND. Số dư mới: %,.2f VND\n",
                                    depositAmount, service.getBalance());
                        } catch (NumberFormatException e) {
                            System.out.println("Vui lòng nhập số tiền hợp lệ!");
                        } catch (Exception e) {
                            System.out.println("Lỗi giao dịch: " + e.getMessage());
                        }
                        break;
                    case "3":
                        System.out.print("Nhập số tiền muốn rút: ");
                        try {
                            double withdrawAmount = Double.parseDouble(scanner.nextLine().trim());
                            boolean success = service.withdraw(withdrawAmount);
                            if (success) {
                                System.out.printf("=> Rút thành công %,.2f VND. Số dư còn lại: %,.2f VND\n",
                                        withdrawAmount, service.getBalance());
                            } else {
                                System.out.printf("=> Rút tiền thất bại! Số dư không đủ (Hiện có: %,.2f VND)\n",
                                        service.getBalance());
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Vui lòng nhập số tiền hợp lệ!");
                        } catch (Exception e) {
                            System.out.println("Lỗi giao dịch: " + e.getMessage());
                        }
                        break;
                    case "0":
                        System.out.println("Cảm ơn bạn đã sử dụng dịch vụ ngân hàng!");
                        return;
                    default:
                        System.out.println("Lựa chọn không hợp lệ, vui lòng chọn lại!");
                }
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
