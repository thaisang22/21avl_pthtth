package baitapdexuat.bai6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public final class TransportBenchmark {
    private static final int COUNT = 1000;
    private static final int ROUNDS = 5;
    private static final int MESSAGE_BYTES = 128;
    private static final int TIMEOUT_MS = 1000;

    private TransportBenchmark() { }

    public static void main(String[] args) throws Exception {
        String host = args.length == 0 ? "127.0.0.1" : args[0];
        int tcpPort = args.length < 2 ? 5400 : Integer.parseInt(args[1]);
        int udpPort = args.length < 3 ? 5401 : Integer.parseInt(args[2]);
        System.out.println("Environment: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + "; Java " + System.getProperty("java.version") + "; target " + host + " (localhost)");
        System.out.println("Method: " + ROUNDS + " rounds, " + COUNT + " sequential request/reply messages per round;"
                + " fixed payload=" + MESSAGE_BYTES + " bytes; per-read timeout=" + TIMEOUT_MS + " ms.");
        System.out.println("Elapsed time uses System.nanoTime() from first send through final receive/timeout.");

        for (int round = 1; round <= ROUNDS; round++) {
            report("TCP", round, tcpRound(host, tcpPort));
            report("UDP", round, udpRound(host, udpPort));
        }
        System.out.println("Compare all rounds and response counts; one run does not show that UDP is always faster.");
    }

    private static Result tcpRound(String host, int port) {
        long start = System.nanoTime();
        int received = 0;
        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(TIMEOUT_MS);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            String payload = payload();
            for (int i = 0; i < COUNT; i++) {
                out.println(payload);
                if (out.checkError()) break;
                try {
                    if (payload.equals(in.readLine())) received++;
                } catch (SocketTimeoutException e) { break; }
            }
        } catch (Exception e) {
            return new Result(received, elapsed(start), "connection error: " + e.getMessage());
        }
        return new Result(received, elapsed(start), "");
    }

    private static Result udpRound(String host, int port) {
        long start = System.nanoTime();
        int received = 0;
        byte[] bytes = payload().getBytes(StandardCharsets.UTF_8);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress address = InetAddress.getByName(host);
            byte[] buffer = new byte[2048];
            for (int i = 0; i < COUNT; i++) {
                socket.send(new DatagramPacket(bytes, bytes.length, address, port));
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(response);
                    if (response.getLength() == bytes.length) {
                        boolean equal = true;
                        for (int j = 0; j < bytes.length; j++) {
                            if (bytes[j] != response.getData()[response.getOffset() + j]) { equal = false; break; }
                        }
                        if (equal) received++;
                    }
                } catch (SocketTimeoutException e) {
                    // Keep sending the remaining messages so each round attempts COUNT datagrams.
                }
            }
        } catch (Exception e) {
            return new Result(received, elapsed(start), "connection error: " + e.getMessage());
        }
        return new Result(received, elapsed(start), "");
    }

    private static String payload() {
        return "x".repeat(MESSAGE_BYTES);
    }

    private static long elapsed(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static void report(String protocol, int round, Result result) {
        System.out.printf("%s round %d: %d/%d replies, %d ms%s%n", protocol, round, result.received, COUNT,
                result.elapsedMs, result.error.isEmpty() ? "" : " (" + result.error + ")");
    }

    private static final class Result {
        final int received;
        final long elapsedMs;
        final String error;
        Result(int received, long elapsedMs, String error) {
            this.received = received;
            this.elapsedMs = elapsedMs;
            this.error = error;
        }
    }
}
