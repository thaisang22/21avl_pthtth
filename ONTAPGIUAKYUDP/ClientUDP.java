import java.io.*;
import java.net.*;
import java.util.*;

// Lop ClientUDP: Gui yeu cau va nhan ket qua tu Server thong qua giao thuc UDP
public class ClientUDP {
    // Cau 2: Thong tin dia chi IP va Port cua Server
    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 7000;

    public static void main(String[] args) {
        System.out.println("Khoi tao Client UDP ket noi toi Server " + SERVER_HOST + ":" + SERVER_PORT + "...");

        try {
            // Tao DatagramSocket cho client (he dieu hanh tu dong gan 1 port ngau nhien con trong)
            DatagramSocket socket = new DatagramSocket();
            // Cai dat thoi gian cho toi da la 5 giay (5000ms), tranh truong hop bi treo neu rot goi tin
            socket.setSoTimeout(5000);

            // Lay dia chi IP cua Server tu hostname
            InetAddress serverAddress = InetAddress.getByName(SERVER_HOST);

            // Luong doc du lieu nguoi dung go tu ban phim
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

            // Buoc 1: Gui goi tin "GET_MENU" de yeu cau Server gui danh sach dich vu (Cau 3)
            guiGoiTin(socket, "GET_MENU", serverAddress, SERVER_PORT);

            // Nhan phan hoi danh sach dich vu tu Server va in ra man hinh
            String menu = nhanGoiTin(socket);
            System.out.println("\n" + menu + "\n");

            // Vong lap duy tri phien lam viec cho den khi nguoi dung nhap "EXIT." (Cau 5)
            while (true) {
                // Nhap lua chon dich vu
                System.out.print("-> Moi ban nhap lua chon (1, 2, 3, 4 hoac EXIT.): ");
                String choice = console.readLine();
                if (choice == null) {
                    break;
                }

                choice = choice.trim();

                // Cau 5: Client dong ket noi bang cach gui chuoi "EXIT." den server
                if (choice.equalsIgnoreCase("EXIT.")) {
                    // Gui goi tin "EXIT." sang server
                    guiGoiTin(socket, "EXIT.", serverAddress, SERVER_PORT);
                    try {
                        // Nhan thong bao xac nhan tu server
                        String phanHoiExit = nhanGoiTin(socket);
                        System.out.println("Server: " + phanHoiExit);
                    } catch (SocketTimeoutException e) {
                        System.out.println("Server da ghi nhan thoat.");
                    }
                    System.out.println("Da dong phien UDP. Chuong trinh ket thuc!");
                    break; // Thoat chuong trinh
                }

                // Kiem tra lua chon hop le
                if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3") && !choice.equals("4")) {
                    System.out.println("Lua chon khong hop le! Vui long nhap tu 1 den 4 hoac EXIT.\n");
                    continue;
                }

                // Buoc 2: Nhap cac dong chuoi gui den server (Cau 3)
                System.out.println("\n--- NHAP DU LIEU ---");
                System.out.println("Nhap cac dong chuoi gui den server.");
                System.out.println("(Luu y: Nhap '.' tren mot dong rieng de ket thuc qua trinh nhap lieu):");

                // Tao chuoi payload chua: LuaChon\nDong1\nDong2\n.\n
                StringBuilder payload = new StringBuilder();
                payload.append(choice).append("\n");

                while (true) {
                    String inputLine = console.readLine();
                    if (inputLine == null) {
                        break;
                    }
                    payload.append(inputLine).append("\n");

                    // Cau 3: Dung lai khi nhap dau "."
                    if (inputLine.trim().equals(".")) {
                        break;
                    }
                    if (inputLine.trim().equalsIgnoreCase("EXIT.")) {
                        break;
                    }
                }

                // Buoc 3: Dong goi va gui du lieu sang Server qua DatagramPacket
                guiGoiTin(socket, payload.toString(), serverAddress, SERVER_PORT);

                // Buoc 4: Cho nhan goi tin ket qua tu server ve (Cau 4)
                try {
                    String ketQua = nhanGoiTin(socket);
                    System.out.println("\n================= PHAN HOI TU SERVER UDP =================");
                    System.out.println(ketQua);
                    System.out.println("==========================================================\n");
                } catch (SocketTimeoutException e) {
                    System.out.println("Qua 5 giay khong nhan duoc phan hoi tu server (co the do mat goi tin UDP)!\n");
                }
            }

            // Dong socket sau khi da ket thuc
            socket.close();

        } catch (IOException e) {
            System.out.println("Loi UDP Client: " + e.getMessage());
        }
    }

    // Ham tien ich: Gui mot chuoi van ban sang server duoi dang DatagramPacket
    private static void guiGoiTin(DatagramSocket socket, String message, InetAddress address, int port) throws IOException {
        // Chuyen chuoi sang mang byte UTF-8
        byte[] data = message.getBytes("UTF-8");
        // Dong goi vao DatagramPacket kem dia chi IP va Port dich
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        // Gui goi tin di
        socket.send(packet);
    }

    // Ham tien ich: Nhan mot DatagramPacket tu server va chuyen doi thanh chuoi van ban
    private static String nhanGoiTin(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[8192];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        // Nhan goi tin (block cho den khi nhan duoc hoac het timeout)
        socket.receive(packet);
        // Chuyen mang byte nhan duoc thanh chuoi String
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8");
    }
}
