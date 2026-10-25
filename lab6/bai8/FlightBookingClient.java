package bai8;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class FlightBookingClient {
    public static final String HOST = "localhost";
    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "FlightBookingService";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            FlightBookingService service = (FlightBookingService) registry.lookup(SERVICE_NAME);
            System.out.println("=== KẾT NỐI HỆ THỐNG ĐẶT VÉ MÁY BAY RMI THÀNH CÔNG ===");

            while (true) {
                System.out.println("\n-------------------------------------------");
                System.out.println("1. Kiểm tra số ghế trống (availableSeats)");
                System.out.println("2. Đặt vé máy bay (bookTicket)");
                System.out.println("3. Hủy vé máy bay (cancelBooking)");
                System.out.println("0. Thoát");
                System.out.print("Chọn chức năng: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1": {
                        System.out.print("Nhập mã chuyến bay (ví dụ: VN123, VJ456, QH789): ");
                        String flightNo = scanner.nextLine().trim();
                        int seats = service.availableSeats(flightNo);
                        if (seats >= 0) {
                            System.out.printf("=> Chuyến bay %s hiện còn: %d ghế trống.\n", flightNo.toUpperCase(), seats);
                        } else {
                            System.out.printf("=> Không tìm thấy chuyến bay %s trong hệ thống.\n", flightNo);
                        }
                        break;
                    }
                    case "2": {
                        System.out.print("Nhập mã chuyến bay muốn đặt vé: ");
                        String flightNo = scanner.nextLine().trim();
                        System.out.print("Nhập số lượng ghế muốn đặt: ");
                        try {
                            int seats = Integer.parseInt(scanner.nextLine().trim());
                            boolean success = service.bookTicket(flightNo, seats);
                            if (success) {
                                System.out.printf("=> ĐẶT VÉ THÀNH CÔNG! Đã đặt %d vé cho chuyến bay %s.\n",
                                        seats, flightNo.toUpperCase());
                                System.out.printf("   Số ghế còn lại: %d ghế.\n", service.availableSeats(flightNo));
                            } else {
                                System.out.printf("=> ĐẶT VÉ THẤT BẠI! Chuyến bay không tồn tại hoặc không đủ ghế trống.\n");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Số lượng ghế phải là số nguyên dương!");
                        }
                        break;
                    }
                    case "3": {
                        System.out.print("Nhập mã chuyến bay muốn hủy vé: ");
                        String flightNo = scanner.nextLine().trim();
                        System.out.print("Nhập số lượng ghế muốn hủy: ");
                        try {
                            int seats = Integer.parseInt(scanner.nextLine().trim());
                            boolean success = service.cancelBooking(flightNo, seats);
                            if (success) {
                                System.out.printf("=> HỦY VÉ THÀNH CÔNG! Đã trả lại %d vé cho chuyến bay %s.\n",
                                        seats, flightNo.toUpperCase());
                                System.out.printf("   Số ghế còn lại: %d ghế.\n", service.availableSeats(flightNo));
                            } else {
                                System.out.printf("=> HỦY VÉ THẤT BẠI! Vui lòng kiểm tra lại mã chuyến bay và số lượng hủy.\n");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Số lượng ghế phải là số nguyên dương!");
                        }
                        break;
                    }
                    case "0":
                        System.out.println("Cảm ơn bạn đã sử dụng dịch vụ!");
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
