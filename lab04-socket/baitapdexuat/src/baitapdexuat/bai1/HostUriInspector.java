package baitapdexuat.bai1;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public final class HostUriInspector {
    private HostUriInspector() {
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java baitapdexuat.bai1.HostUriInspector <hostname> <uri>");
            System.exit(1);
        }

        final URI uri;
        try {
            uri = new URI(args[1]);
        } catch (URISyntaxException e) {
            System.err.println("ERR INVALID_URI: " + e.getMessage());
            System.exit(2);
            return;
        }

        try {
            printHost(args[0]);
        } catch (UnknownHostException e) {
            System.err.println("ERR UNKNOWN_HOST: " + args[0]);
            System.exit(3);
        }
        printUri(uri);
    }

    private static void printHost(String hostname) throws UnknownHostException {
        System.out.println("Hostname: " + hostname);
        for (InetAddress address : InetAddress.getAllByName(hostname)) {
            System.out.println("IP: " + address.getHostAddress());
            System.out.println("  Type: " + ipType(address));
            System.out.println("  Loopback: " + address.isLoopbackAddress());
            System.out.println("  Site local: " + address.isSiteLocalAddress());
        }
    }

    private static String ipType(InetAddress address) {
        if (address instanceof Inet4Address) {
            return "IPv4";
        }
        if (address instanceof Inet6Address) {
            return "IPv6";
        }
        return "Unknown";
    }

    private static void printUri(URI uri) {
        System.out.println("URI: " + uri);
        System.out.println("  Scheme: " + uri.getScheme());
        System.out.println("  Host: " + uri.getHost());
        System.out.println("  Port: " + uri.getPort());
        System.out.println("  Path: " + uri.getPath());
        System.out.println("  Query: " + uri.getQuery());
        System.out.println("  Fragment: " + uri.getFragment());
    }
}
