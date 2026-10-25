package bai7;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GeometryServiceImpl extends UnicastRemoteObject implements GeometryService {
    public GeometryServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public double rectangleArea(double width, double height) throws RemoteException {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Kích thước không được âm!");
        }
        double area = width * height;
        System.out.printf("Calculated rectangle area (%.2f x %.2f) = %.2f\n", width, height, area);
        return area;
    }

    @Override
    public double circleArea(double radius) throws RemoteException {
        if (radius < 0) {
            throw new IllegalArgumentException("Bán kính không được âm!");
        }
        double area = Math.PI * radius * radius;
        System.out.printf("Calculated circle area (r = %.2f) = %.2f\n", radius, area);
        return area;
    }

    @Override
    public double triangleArea(double base, double height) throws RemoteException {
        if (base < 0 || height < 0) {
            throw new IllegalArgumentException("Kích thước không được âm!");
        }
        double area = 0.5 * base * height;
        System.out.printf("Calculated triangle area (base = %.2f, height = %.2f) = %.2f\n", base, height, area);
        return area;
    }
}
