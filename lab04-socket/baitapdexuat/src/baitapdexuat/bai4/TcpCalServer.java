package baitapdexuat.bai4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;

public class TcpCalServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("TCP server listening on port" + PORT);

            while (true) {
                try (Socket socket = server.accept()) {
                    serve(socket);
                } catch (IOException e) {
                    System.err.println("Lỗi phiên client" + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Không mở được server " + e.getMessage());
        }
    }

    static void serve(Socket socket) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            String request;
            while ((request = in.readLine()) != null) {
                String response = process(request);
                out.println(response);
                if (request.equalsIgnoreCase("QUIT"))
                    break;
            }
        } catch (Exception e) {
        }
    }

    static String process(String request) {
        String trimmed = request.trim();
        if (trimmed.equalsIgnoreCase("PING"))
            return "OK PONG";
        if (trimmed.equalsIgnoreCase("TIME"))
            return "OK" + LocalDateTime.now();
        if (trimmed.equalsIgnoreCase("QUIT"))
            return "OK BYE";
        if (trimmed.equalsIgnoreCase("CALC") || trimmed.regionMatches(true, 0, "CALC ", 0, 5)) {
            return calculate(trimmed);
        }
        if (trimmed.regionMatches(true, 0, "UPPER ", 0, 6)) {
            return "OK " + trimmed.substring(6).toUpperCase(Locale.ROOT);
        }
        return "ERR UNKNOWN_CMD";

    }

    private static String calculate(String request) {
        String[] parts = request.split("\\s+");
        if (parts.length != 4) {
            return "ERR INVALID_FORMAT";
        }

        BigDecimal left;
        BigDecimal right;
        try {
            left = new BigDecimal(parts[2]);
            right = new BigDecimal(parts[3]);
        } catch (NumberFormatException e) {
            return "ERR INVALID_NUMBER";
        }

        BigDecimal result;
        switch (parts[1]) {
            case "+":
                result = left.add(right);
                break;
            case "-":
                result = left.subtract(right);
                break;
            case "*":
                result = left.multiply(right);
                break;
            case "/":
                if (right.compareTo(BigDecimal.ZERO) == 0) {
                    return "ERR DIVIDE_BY_ZERO";
                }
                result = left.divide(right, MathContext.DECIMAL128);
                break;
            default:
                return "ERR UNSUPPORTED_OPERATOR";
        }
        return "OK " + result.stripTrailingZeros().toPlainString();
    }

}
