package server;

public class ServerMain {
    private static Server server;
    private static Thread serverThread;

    public static void main(String[] args) {
        try {
            ServerGUI.startGUI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void startServer() {
        if (server != null && serverThread != null) {
            Server.stopRunning();
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        ServerGUI.clear();
        ServerGUI.clearNetworkLabel();
        server = new Server();
        serverThread = new Thread(server);
        serverThread.start();
    }
}
