package bai8;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class FlightBookingServiceImpl extends UnicastRemoteObject implements FlightBookingService {
    // Map lưu mã chuyến bay -> số ghế trống hiện tại
    private final Map<String, Integer> flightSeats;
    // Map lưu tổng số ghế thiết kế tối đa
    private final Map<String, Integer> maxCapacity;

    public FlightBookingServiceImpl() throws RemoteException {
        super();
        this.flightSeats = new HashMap<>();
        this.maxCapacity = new HashMap<>();

        // Khởi tạo các chuyến bay mẫu
        initFlight("VN123", 50);
        initFlight("VJ456", 100);
        initFlight("QH789", 30);
    }

    private void initFlight(String flightNumber, int seats) {
        flightSeats.put(flightNumber.toUpperCase(), seats);
        maxCapacity.put(flightNumber.toUpperCase(), seats);
    }

    @Override
    public synchronized int availableSeats(String flightNumber) throws RemoteException {
        String key = flightNumber.toUpperCase().trim();
        Integer seats = flightSeats.get(key);
        if (seats == null) {
            System.out.println("Flight not found: " + key);
            return -1; // -1 biểu thị chuyến bay không tồn tại
        }
        System.out.printf("Checked seats for flight %s: %d available\n", key, seats);
        return seats;
    }

    @Override
    public synchronized boolean bookTicket(String flightNumber, int seats) throws RemoteException {
        if (seats <= 0) {
            return false;
        }
        String key = flightNumber.toUpperCase().trim();
        Integer current = flightSeats.get(key);
        if (current == null) {
            return false;
        }
        if (current >= seats) {
            flightSeats.put(key, current - seats);
            System.out.printf("Booked %d seats on flight %s. Remaining: %d\n", seats, key, current - seats);
            return true;
        } else {
            System.out.printf("Booking failed for flight %s: Requested %d, Available %d\n", key, seats, current);
            return false;
        }
    }

    @Override
    public synchronized boolean cancelBooking(String flightNumber, int seats) throws RemoteException {
        if (seats <= 0) {
            return false;
        }
        String key = flightNumber.toUpperCase().trim();
        Integer current = flightSeats.get(key);
        Integer max = maxCapacity.get(key);
        if (current == null || max == null) {
            return false;
        }
        // Không cho phép hủy vượt quá số ghế tối đa ban đầu
        if (current + seats <= max) {
            flightSeats.put(key, current + seats);
            System.out.printf("Cancelled %d seats on flight %s. New available: %d\n", seats, key, current + seats);
            return true;
        } else {
            System.out.printf("Cancel failed for flight %s: Exceeds original flight capacity\n", key);
            return false;
        }
    }
}
