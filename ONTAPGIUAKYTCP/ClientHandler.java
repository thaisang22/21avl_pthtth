import java.io.*;
import java.net.*;
import java.util.*;

// Lop ClientHandler ke thua Thread de quan ly rieng mot ket noi client
public class ClientHandler extends Thread {
    // Socket ket noi truc tiep toi client
    private Socket socket;
    // Luong doc du lieu tu client gui den
    private BufferedReader in;
    // Luong ghi du lieu de gui ve client
    private PrintWriter out;

    // Ham khoi tao nhan socket tu ServerTCP truyen sang
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // Phuong thuc run() duoc goi khi luong duoc start()
    @Override
    public void run() { 
        try {
            // Khoi tao luong doc du lieu tu socket (chuyen byte stream sang char stream voi bo ma UTF-8)
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            // Khoi tao luong ghi du lieu qua socket (autoflush = true de du lieu gui di ngay lap tuc)
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // Vong lap duy tri ket noi phuc vu client cho den khi nhan chuoi "EXIT." (Cau 5)
            while (true) {
                // Cau 3: Sau khi chap nhan ket noi, server gui danh sach cac dich vu cung cap toi client
                out.println("----- DANH SACH DICH VU -----");
                out.println("1. Dao nguoc toan bo chuoi va in hoa ky tu dau cua tung tu");
                out.println("2. Dao nguoc tu va in hoa ky tu dau cua tung tu, giu nguyen vi tri");
                out.println("3. Dem so luong tu cua tat ca cac dong gui den");
                out.println("4. Dem so luong tu tung dong");
                out.println("Nhap 'EXIT.' de dong ket noi");
                out.println("END_MENU"); // Tin hieu danh dau da gui het noi dung menu

                // Doc lua chon dich vu do client gui len
                String choice = in.readLine();
                
                // Neu client ngat ket noi dot ngot hoac dong luong
                if (choice == null) {
                    break;
                }

                choice = choice.trim();

                // Cau 5: Client dong ket noi bang cach gui chuoi "EXIT." den server
                if (choice.equalsIgnoreCase("EXIT.")) {
                    System.out.println("Client " + socket.getRemoteSocketAddress() + " yeu cau EXIT.");
                    out.println("Tam biet! Server da dong ket noi.");
                    out.println("END_RESPONSE");
                    break; // Thoat vong lap de dong ket noi
                }

                // Kiem tra lua chon hop le (tu 1 den 4)
                if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3") && !choice.equals("4")) {
                    out.println("ERR: Lua chon khong hop le! Vui long nhap tu 1 den 4 hoac EXIT.");
                    out.println("END_RESPONSE");
                    continue; // Quay lai gui menu cho client chon lai
                }

                // Gui tin hieu READY bao cho client biet server da san sang nhan cac dong chuoi
                out.println("READY");

                // Danh sach luu cac dong chuoi do client gui den
                ArrayList<String> lines = new ArrayList<>();
                String line;

                // Cau 3: Qua trinh nhap lieu va gui den server dung lai khi server nhan duoc dau "."
                while ((line = in.readLine()) != null) {
                    // Neu client gui dong la "." (hoac nguoi dung go EXIT.) thi dung nhan
                    if (line.trim().equals(".")) {
                        break;
                    }
                    if (line.trim().equalsIgnoreCase("EXIT.")) {
                        // Truong hop client muon thoat ngay trong luc nhap
                        break;
                    }
                    // Them dong vua nhan vao danh sach
                    lines.add(line);
                }

                // Cau 4: Server xu ly yeu cau va gui tra ket qua thanh cong den client
                switch (choice) {
                    case "1":
                        // Dich vu 1: Dao nguoc toan bo chuoi va in hoa ky tu dau cua tung tu
                        out.println("--- KET QUA DICH VU 1 (Dao nguoc chuoi, in hoa chu dau tung tu) ---");
                        for (String l : lines) {
                            out.println(dichVu1(l));
                        }
                        break;

                    case "2":
                        // Dich vu 2: Dao nguoc tu va in hoa ky tu dau cua tung tu, giu nguyen vi tri
                        out.println("--- KET QUA DICH VU 2 (Dao nguoc tu giu nguyen vi tri, in hoa chu dau) ---");
                        for (String l : lines) {
                            out.println(dichVu2(l));
                        }
                        break;

                    case "3":
                        // Dich vu 3: Dem so luong tu cua tat ca cac dong gui den
                        out.println("--- KET QUA DICH VU 3 (Dem so luong tu tat ca cac dong) ---");
                        int tongSoTu = dichVu3(lines);
                        out.println("Tong so luong tu cua tat ca cac dong gui den la: " + tongSoTu + " tu");
                        break;

                    case "4":
                        // Dich vu 4: Dem so luong tu tung dong
                        out.println("--- KET QUA DICH VU 4 (Dem so luong tu tung dong) ---");
                        for (int i = 0; i < lines.size(); i++) {
                            int soTu = demTuTrongDong(lines.get(i));
                            out.println("Dong " + (i + 1) + ": " + soTu + " tu (Noi dung: \"" + lines.get(i) + "\")");
                        }
                        break;
                }

                // Gui tin hieu ket thuc phan hoi ket qua
                out.println("END_RESPONSE");
            }

        } catch (IOException e) {
            System.out.println("Loi xu ly voi client " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        } finally {
            // Dong socket va giai phong tai nguyen
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                System.out.println("-> Da dong ket noi voi: " + socket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.out.println("Loi khi dong socket: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // CAC HAM XU LY DICH VU THEO YEU CAU DE BAI
    // =========================================================================

    // Dich vu 1: Dao nguoc toan bo chuoi va in hoa ky tu dau cua tung tu
    // Vi du: "PHAT TRIEN HE THONG TICH HOP" -> "Poh Hcit Gnoht Eh Neirt Tahp"
    public static String dichVu1(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        // Buoc 1: Dao nguoc toan bo cac ky tu trong chuoi
        // Vi du: "PHAT TRIEN HE THONG TICH HOP" -> "POH HCIT GNOHT EH NEIRT TAHP"
        String reversed = new StringBuilder(input.trim()).reverse().toString();

        // Buoc 2: Tach chuoi da dao nguoc thanh tung tu theo khoang trang
        String[] words = reversed.split("\\s+");

        // Buoc 3: In hoa ky tu dau cua tung tu, cac ky tu con lai viet thuong
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (!w.isEmpty()) {
                // Ky tu dau in hoa, cac ky tu tiep theo viet thuong
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
        // Buoc 1: Tach chuoi thanh cac tu (giu nguyen vi tri ban dau trong cau)
        String[] words = input.trim().split("\\s+");

        // Buoc 2: Dao nguoc tung tu va in hoa ky tu dau cua tu do
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (!w.isEmpty()) {
                // Dao nguoc cac ky tu cua rieng tu do
                String reversedWord = new StringBuilder(w).reverse().toString();
                // In hoa ky tu dau, cac ky tu sau viet thuong
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

    // Ham phu dem so tu trong mot dong don le
    public static int demTuTrongDong(String line) {
        if (line == null) {
            return 0;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        // Tach theo 1 hoac nhieu khoang trang de lay so luong tu
        String[] words = trimmed.split("\\s+");
        return words.length;
    }
}
