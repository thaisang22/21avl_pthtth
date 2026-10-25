package bai4;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ContactClient {
    public static final String HOST = "localhost";
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "ContactService";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            ContactService service = (ContactService) registry.lookup(SERVICE_NAME);
            System.out.println("=== KẾT NỐI QUẢN LÝ DANH BẠ RMI SERVER THÀNH CÔNG ===");

            while (true) {
                System.out.println("\n-------------------------------------------");
                System.out.println("1. Thêm / cập nhật liên hệ");
                System.out.println("2. Tìm kiếm liên hệ theo tên");
                System.out.println("3. Xóa liên hệ");
                System.out.println("0. Thoát");
                System.out.print("Chọn chức năng: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        System.out.print("Nhập tên người liên hệ: ");
                        String addName = scanner.nextLine().trim();
                        System.out.print("Nhập số điện thoại: ");
                        String phone = scanner.nextLine().trim();
                        if (addName.isEmpty() || phone.isEmpty()) {
                            System.out.println("Tên và số điện thoại không được để trống!");
                        } else {
                            service.addContact(addName, phone);
                            System.out.println("=> Đã lưu liên hệ thành công!");
                        }
                        break;
                    case "2":
                        System.out.print("Nhập tên người cần tìm: ");
                        String searchName = scanner.nextLine().trim();
                        String foundPhone = service.findContact(searchName);
                        if (foundPhone != null) {
                            System.out.printf("=> Tìm thấy: %s - Số điện thoại: %s\n", searchName, foundPhone);
                        } else {
                            System.out.printf("=> Không tìm thấy liên hệ nào có tên '%s'.\n", searchName);
                        }
                        break;
                    case "3":
                        System.out.print("Nhập tên liên hệ cần xóa: ");
                        String delName = scanner.nextLine().trim();
                        boolean deleted = service.deleteContact(delName);
                        if (deleted) {
                            System.out.printf("=> Đã xóa thành công liên hệ '%s'.\n", delName);
                        } else {
                            System.out.printf("=> Không tìm thấy liên hệ '%s' để xóa.\n", delName);
                        }
                        break;
                    case "0":
                        System.out.println("Tạm biệt!");
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
