package server;

public class ServerMain {
    private static Server server;
    private static Thread serverThread;

    private static boolean isHeadless;

    public static void main(String[] args) {
        isHeadless = false;

        if (args.length > 0) {
            if (args[0].equals("-H") || args[0].equals("--headless")) {
                isHeadless = true;
            }
        }

        if (!isHeadless) {
            try {
                ServerGUI.startGUI();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            startServer();
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void startServer() {
        if (!isHeadless) {
            ServerGUI.clear();
            ServerGUI.clearNetworkLabel();
        }

        server = new Server();
        serverThread = new Thread(server);
        serverThread.start();
    }

    public static void stopServer() {
        if (server != null && serverThread != null) {
            Server.stopRunning();

            try {
                serverThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void serverStopped() {
        ServerGUI.setServerButtonFunction(true);
    }

    public static boolean isHeadless() {
        return isHeadless;
    }
}
