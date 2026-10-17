import java.io.*;
import java.net.*;
import java.util.*;

// Lop ServerUDP: Lang nghe tren cong 7000 qua giao thuc UDP va xu ly cac yeu cau tu client
public class ServerUDP {
    // Cau 1: Server tao socket tai port 7000
    public static final int PORT = 7000;

    public static void main(String[] args) {
        try {
            // Tao DatagramSocket de lang nghe tren cong 7000 (khac voi ServerSocket trong TCP)
            DatagramSocket socket = new DatagramSocket(PORT);
            System.out.println("====================================================");
            System.out.println("Server UDP dang chay va lang nghe tai port: " + PORT);
            System.out.println("Cho cac goi tin (DatagramPacket) tu client...");
            System.out.println("====================================================");

            // Vong lap vo han de server lien tuc nhan va xu ly cac goi tin gui den
            while (true) {
                // Tao mang byte buffer de chua du lieu nhan duoc
                byte[] buffer = new byte[8192];
                // Tao DatagramPacket de nhan du lieu vao buffer
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Nhan goi tin tu client (lenh nay se block cho den khi co goi tin den)
                socket.receive(packet);

                // Cau 1: Server su dung luong (thread) de quan ly ket noi va cac yeu cau tu client(s)
                // Moi khi nhan duoc goi tin, server tao 1 luong rieng (RequestHandlerUDP) de xu ly doc lap
                new RequestHandlerUDP(socket, packet).start();
            }
        } catch (IOException e) {
            System.out.println("Loi server UDP: " + e.getMessage());
        }
    }

    // =========================================================================
    // CAC HAM XU LY DICH VU
    // =========================================================================

    // Dich vu 1: Dao nguoc toan bo chuoi va in hoa ky tu dau cua tung tu
    // Vi du: "PHAT TRIEN HE THONG TICH HOP" -> "Poh Hcit Gnoht Eh Neirt Tahp"
    public static String dichVu1(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        // Buoc 1: Dao nguoc toan bo ky tu trong chuoi
        String reversed = new StringBuilder(input.trim()).reverse().toString();

        // Buoc 2: Tach chuoi dao nguoc thanh tung tu theo khoang trang
        String[] words = reversed.split("\\s+");

        // Buoc 3: In hoa ky tu dau cua tung tu, cac ky tu con lai viet thuong
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (!w.isEmpty()) {
                String formatted = Character.toUpperCase(w.charAt(0)) + 
                                  (w.length() > 1 ? w.substring(1).toLowerCase() : "");
                result.append(formatted);
                if (i < words.length - 1) {
                    result.append(" ");
                }
            }
        }
        return result.toString();
    }

    // Dich vu 2: Dao nguoc tu va in hoa ky tu dau cua tung tu, giu nguyen vi tri
    // Vi du: "PHAT TRIEN HE THONG TICH HOP" -> "Tahp Neirt Eh Gnoht Hcit Poh"
    public static String dichVu2(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        // Buoc 1: Tach chuoi thanh tung tu giu nguyen thu tu trong cau
        String[] words = input.trim().split("\\s+");

        // Buoc 2: Dao nguoc tung tu va in hoa ky tu dau cua tu do
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (!w.isEmpty()) {
                String reversedWord = new StringBuilder(w).reverse().toString();
                String formatted = Character.toUpperCase(reversedWord.charAt(0)) + 
                                  (reversedWord.length() > 1 ? reversedWord.substring(1).toLowerCase() : "");
                result.append(formatted);
                if (i < words.length - 1) {
                    result.append(" ");
                }
            }
        }
        return result.toString();
    }

    // Dich vu 3: Dem so luong tu cua tat ca cac dong gui den
    public static int dichVu3(List<String> lines) {
        int totalWords = 0;
        for (String line : lines) {
            totalWords += demTuTrongDong(line);
        }
        return totalWords;
    }

    // Ham phu dem so tu trong 1 dong
    public static int demTuTrongDong(String line) {
        if (line == null) {
            return 0;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        String[] words = trimmed.split("\\s+");
        return words.length;
    }
}

// Lop RequestHandlerUDP ke thua Thread de xu ly tung goi tin yeu cau trong mot luong rieng
class RequestHandlerUDP extends Thread {
    private DatagramSocket socket;
    private DatagramPacket packet;

    public RequestHandlerUDP(DatagramSocket socket, DatagramPacket packet) {
        this.socket = socket;
        this.packet = packet;
    }

    @Override
    public void run() {
        try {
            // Chuyen doi byte array nhan duoc thanh chuoi String (su dung bang ma UTF-8)
            String request = new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8").trim();

            // Lay dia chi IP va Port cua client da gui goi tin de ti nua gui phan hoi ve
            InetAddress clientAddress = packet.getAddress();
            int clientPort = packet.getPort();

            System.out.println("-> Nhan yeu cau tu " + clientAddress + ":" + clientPort);

            String response = "";

            // Truong hop 1: Client gui "GET_MENU" de lay danh sach cac dich vu (Cau 3)
            if (request.equalsIgnoreCase("GET_MENU")) {
                StringBuilder menu = new StringBuilder();
                menu.append("----- DANH SACH DICH VU -----\n");
                menu.append("1. Dao nguoc toan bo chuoi va in hoa ky tu dau cua tung tu\n");
                menu.append("2. Dao nguoc tu va in hoa ky tu dau cua tung tu, giu nguyen vi tri\n");
                menu.append("3. Dem so luong tu cua tat ca cac dong gui den\n");
                menu.append("4. Dem so luong tu tung dong\n");
                menu.append("Nhap 'EXIT.' de dong ket noi");
                response = menu.toString();
            }
            // Truong hop 2: Client gui chuoi "EXIT." de dong ket noi (Cau 5)
            else if (request.equalsIgnoreCase("EXIT.")) {
                System.out.println("Client " + clientAddress + ":" + clientPort + " yeu cau EXIT.");
                response = "Tam biet! Server UDP da ghi nhan dong phien lam viec.";
            }
            // Truong hop 3: Client gui du lieu yeu cau xu ly dich vu (dinh dang: DichVu\nDong1\nDong2\n...)
            else {
                // Tach yeu cau thanh cac dong
                String[] parts = request.split("\r?\n");
                if (parts.length > 0) {
                    String choice = parts[0].trim();
                    // Trich xuat cac dong chuoi do client nhap vao
                    List<String> lines = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        String l = parts[i];
                        // Dung lai neu gap dau "." (Cau 3)
                        if (l.trim().equals(".")) {
                            break;
                        }
                        lines.add(l);
                    }

                    // Xu ly theo dich vu duoc chon (Cau 4)
                    StringBuilder sb = new StringBuilder();
                    switch (choice) {
                        case "1":
                            sb.append("--- KET QUA DICH VU 1 (Dao nguoc chuoi, in hoa chu dau tung tu) ---\n");
                            for (String l : lines) {
                                sb.append(ServerUDP.dichVu1(l)).append("\n");
                            }
                            break;

                        case "2":
                            sb.append("--- KET QUA DICH VU 2 (Dao nguoc tu giu nguyen vi tri, in hoa chu dau) ---\n");
                            for (String l : lines) {
                                sb.append(ServerUDP.dichVu2(l)).append("\n");
                            }
                            break;

                        case "3":
                            sb.append("--- KET QUA DICH VU 3 (Dem so luong tu tat ca cac dong) ---\n");
                            int total = ServerUDP.dichVu3(lines);
                            sb.append("Tong so luong tu cua tat ca cac dong gui den la: ").append(total).append(" tu\n");
                            break;

                        case "4":
                            sb.append("--- KET QUA DICH VU 4 (Dem so luong tu tung dong) ---\n");
                            for (int i = 0; i < lines.size(); i++) {
                                int count = ServerUDP.demTuTrongDong(lines.get(i));
                                sb.append("Dong ").append(i + 1).append(": ").append(count)
                                  .append(" tu (Noi dung: \"").append(lines.get(i)).append("\")\n");
                            }
                            break;

                        default:
                            sb.append("ERR: Lua chon khong hop le! Vui long chon tu 1 den 4 hoac EXIT.\n");
                            break;
                    }
                    response = sb.toString();
                }
            }

            // Chuyen chuoi ket qua phan hoi thanh mang byte de dong goi gui di
            byte[] responseData = response.getBytes("UTF-8");
            // Tao DatagramPacket chua ket qua, kem dia chi IP va port cua client
            DatagramPacket responsePacket = new DatagramPacket(
                    responseData, responseData.length, clientAddress, clientPort);
            // Gui goi tin phan hoi ve cho client qua socket
            socket.send(responsePacket);

        } catch (IOException e) {
            System.out.println("Loi xu ly goi tin UDP: " + e.getMessage());
        }
    }
}
