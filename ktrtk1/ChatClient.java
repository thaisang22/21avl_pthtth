import java.io.*;
import java.net.*;

public class ChatClient {
    public static String HOST = "localhost";
    public static int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Dang ket noi den " + HOST + ":" + PORT + "...");

        try {
            Socket socket = new Socket(HOST, PORT);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            System.out.println("Ket noi thanh cong!\n");

            // luong nhan tin nhan tu server
            Thread readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String msg;
                        while ((msg = in.readLine()) != null) {
                            if (msg.endsWith(": ")) {
                                System.out.print(msg);
                            } else {
                                System.out.println(msg);
                            }
                        }
                    } catch (IOException e) {
                    }
                }
            });
            readThread.start();

            // doc tin nhan tu ban phim va gui
            String input;
            while ((input = console.readLine()) != null) {
                out.println(input);
                if (input.equalsIgnoreCase("/exit")) {
                    Thread.sleep(200);
                    break;
                }
            }

            socket.close();

        } catch (Exception e) {
            System.out.println("Loi ket noi: " + e.getMessage());
        }
    }
}
