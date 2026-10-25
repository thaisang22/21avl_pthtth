package bai7;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class GeometryClient {
    public static final String HOST = "localhost";
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "GeometryService";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            GeometryService service = (GeometryService) registry.lookup(SERVICE_NAME);
            System.out.println("=== KẾT NỐI HÌNH HỌC GEOMETRY RMI THÀNH CÔNG ===");

            while (true) {
                System.out.println("\n-------------------------------------------");
                System.out.println("1. Tính diện tích Hình Chữ Nhật");
                System.out.println("2. Tính diện tích Hình Tròn");
                System.out.println("3. Tính diện tích Hình Tam Giác");
                System.out.println("0. Thoát");
                System.out.print("Chọn chức năng: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        System.out.print("Nhập chiều dài: ");
                        double width = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Nhập chiều rộng: ");
                        double height = Double.parseDouble(scanner.nextLine().trim());
                        double rectArea = service.rectangleArea(width, height);
                        System.out.printf("=> Diện tích hình chữ nhật: %.4f\n", rectArea);
                        break;
                    case "2":
                        System.out.print("Nhập bán kính hình tròn: ");
                        double radius = Double.parseDouble(scanner.nextLine().trim());
                        double circArea = service.circleArea(radius);
                        System.out.printf("=> Diện tích hình tròn: %.4f\n", circArea);
                        break;
                    case "3":
                        System.out.print("Nhập cạnh đáy tam giác: ");
                        double base = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Nhập chiều cao tam giác: ");
                        double triHeight = Double.parseDouble(scanner.nextLine().trim());
                        double triArea = service.triangleArea(base, triHeight);
                        System.out.printf("=> Diện tích hình tam giác: %.4f\n", triArea);
                        break;
                    case "0":
                        System.out.println("Tạm biệt!");
                        return;
                    default:
                        System.out.println("Lựa chọn không hợp lệ, vui lòng chọn lại!");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Dữ liệu nhập vào phải là số thực!");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
