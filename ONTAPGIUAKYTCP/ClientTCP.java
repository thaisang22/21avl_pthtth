import java.io.*;
import java.net.*;

// Lop ClientTCP: Ket noi den Server qua IP va Port, gui yeu cau va nhan ket qua
public class ClientTCP {
    // Cau 2: Client gui yeu cau va ket noi toi server qua IP va port cho truoc
    public static final String SERVER_HOST = "localhost"; // Dia chi IP hoac ten mien cua server
    public static final int SERVER_PORT = 7000;          // Port 7000 theo dung de bai

    public static void main(String[] args) {
        System.out.println("Dang ket noi den Server " + SERVER_HOST + ":" + SERVER_PORT + "...");

        try {
            // Cau 2: Tao socket ket noi toi server qua IP va port
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("-> Ket noi thanh cong den Server!\n");

            // Luong doc du lieu nguoi dung nhap tu ban phim (console)
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            // Luong doc du lieu server gui ve
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            // Luong gui du lieu len server (autoflush = true)
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // Vong lap duy tri ket noi voi server den khi gui "EXIT." (Cau 5)
            while (true) {
                // Buoc 1: Nhan va hien thi danh sach cac dich vu tu Server gui ve (Cau 3)
                String line;
                while ((line = in.readLine()) != null) {
                    // Khi server gui tin hieu END_MENU thi dung doc menu
                    if (line.equals("END_MENU")) {
                        break;
                    }
                    System.out.println(line);
                }

                // Neu server da dong ket noi
                if (line == null) {
                    System.out.println("Server da dong ket noi!");
                    break;
                }

                // Buoc 2: Nguoi dung nhap lua chon dich vu hoac chuoi "EXIT."
                System.out.print("\n-> Moi ban nhap lua chon (1, 2, 3, 4 hoac EXIT.): ");
                String choice = console.readLine();
                if (choice == null) {
                    break;
                }

                choice = choice.trim();
                // Gui lua chon len Server
                out.println(choice);

                // Cau 5: Neu nguoi dung chon dong ket noi bang chuoi "EXIT."
                if (choice.equalsIgnoreCase("EXIT.")) {
                    // Doc thong bao tam biet tu Server
                    while ((line = in.readLine()) != null) {
                        if (line.equals("END_RESPONSE")) {
                            break;
                        }
                        System.out.println(line);
                    }
                    System.out.println("Da ngat ket noi toi server. Chuong trinh ket thuc!");
                    break; // Thoat chuong trinh
                }

                // Buoc 3: Doc phan hoi ban dau cua server ve lua chon
                String serverStatus = in.readLine();
                if (serverStatus == null) {
                    break;
                }

                // Neu nguoi dung nhap lua chon khong hop le
                if (serverStatus.startsWith("ERR:")) {
                    System.out.println(serverStatus);
                    // Doc tiep cho den END_RESPONSE
                    while ((line = in.readLine()) != null) {
                        if (line.equals("END_RESPONSE")) {
                            break;
                        }
                    }
                    System.out.println();
                    continue; // Quay lai hien thi menu
                }

                // Neu Server san sang (READY), bat dau nhap cac dong chuoi
                if (serverStatus.equals("READY")) {
                    System.out.println("\n--- NHAP DU LIEU ---");
                    System.out.println("Nhap cac dong chuoi gui den server.");
                    System.out.println("(Luu y: Nhap '.' tren mot dong rieng de hoan thanh viec gui):");

                    // Cau 3: Qua trinh nhap lieu va gui den server dung lai khi server nhan duoc dau "."
                    while (true) {
                        String inputLine = console.readLine();
                        if (inputLine == null) {
                            break;
                        }
                        // Gui dong vua nhap len server
                        out.println(inputLine);

                        // Neu nguoi dung go dau "." tren dong rieng thi dung nhap
                        if (inputLine.trim().equals(".")) {
                            break;
                        }
                        // Neu nguoi dung go EXIT. thi cung dung
                        if (inputLine.trim().equalsIgnoreCase("EXIT.")) {
                            break;
                        }
                    }

                    // Buoc 4: Nhan ket qua xu ly tu server va in ra man hinh (Cau 4)
                    System.out.println("\n================= PHAN HOI TU SERVER =================");
                    while ((line = in.readLine()) != null) {
                        // Dung lai khi nhan tin hieu ket thuc phan hoi END_RESPONSE
                        if (line.equals("END_RESPONSE")) {
                            break;
                        }
                        System.out.println(line);
                    }
                    System.out.println("======================================================\n");
                }
            }

            // Dong ket noi socket
            socket.close();

        } catch (IOException e) {
            System.out.println("Loi ket noi: " + e.getMessage());
        }
    }
}
