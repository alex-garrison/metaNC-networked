package client;

import java.net.InetAddress;

public class ClientMain {
    public static Client client;
    private static Thread clientThread;

    private static InetAddress host;
    private static int port;
    private static boolean hostSet;
    public static void main(String[] args) {
        try {
            ClientGUI.startGUI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static boolean isHostSet() {
        return hostSet;
    }

    public static void setHostAndPort(InetAddress newHost, int newPort) {
        host = newHost;
        port = newPort;
        hostSet = true;
    }

    public static InetAddress getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    public static void startClient() {
        if (hostSet) {
            client = new Client(host, port);
            clientThread = new Thread(client);
            clientThread.start();
        } else {
            ClientGUI.frame.setNetworkLabel("Host not set", true);
        }
    }

}
