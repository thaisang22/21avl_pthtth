package ONTAPTCP;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ClientHandler {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // khoi tao luong nhan tu client
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            // khoi tao luong ra cho client
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            while (true) {
                out.println("----- DANH SACH DICH VU -----");
                out.println("1. Dao nguoc toan bo chuoi va in hoa ky tu dau cua tung tu");
                out.println("2. Dao nguoc tu va in hoa ky tu dau cua tung tu, giu nguyen vi tri");
                out.println("3. Dem so luong tu cua tat ca cac dong gui den");
                out.println("4. Dem so luong tu tung dong");
                out.println("Nhap 'EXIT.' de dong ket noi");
                out.println("END_MENU"); // Tin hieu danh dau da gui het noi dung menu

                // read choice from client
                String choice = in.readLine();

                if (choice == null) {
                    break;
                }

                choice = choice.trim();

                if (choice.equalsIgnoreCase("EXIT.")) {
                    System.out.println("Cilent" + socket.getRemoteSocketAddress() + "Yeu cau exit");
                    break;
                }
                // Kiem tra lua chon hop le (tu 1 den 4)
                if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3") && !choice.equals("4")) {
                    out.println("ERR: Lua chon khong hop le! Vui long nhap tu 1 den 4 hoac EXIT.");
                    out.println("END_RESPONSE");
                    continue; // Quay lai gui menu cho client chon lai
                }

                out.println("SAN SAN NHAN CHUOI");

                // Danh sach luu cac dong chuoi do client gui den
                ArrayList<String> lines = new ArrayList<>();
                String line;

            }
        } catch (IOException e) {
            System.err.println("loi :" + e.getMessage());
        }
    }

}
