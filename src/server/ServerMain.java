package server;

import java.util.concurrent.locks.ReentrantLock;

public class ServerMain {
    public static ServerMain serverMain;
    public static Server server;
    private static Thread serverThread;

    private static boolean isHeadless;
    private final ReentrantLock lock = new ReentrantLock();

    private ServerMain() {}

    public static void main(String[] args) {
        serverMain = new ServerMain();
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
            serverMain.startServer();
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void startServer() {
        lock.lock();

        try {
            if (serverThread == null || !serverThread.isAlive()) {
                if (!isHeadless) {
                    ServerGUI.clear();
                    ServerGUI.clearNetworkLabel();
                }

                server = new Server();
                serverThread = new Thread(server);
                serverThread.start();
            }
        } finally {
            lock.unlock();
        }
    }

    public void stopServer() {
        lock.lock();

        try {
            if (server != null && serverThread != null) {
                Server.stopRunning();

                try {
                    do {
                        serverThread.join(1000);

                        if (serverThread.isAlive()) {
                            serverThread.interrupt();
                        }
                    } while (serverThread.isAlive());

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static void serverStopped() {
        ServerGUI.setServerButtonFunction(true);
    }

    public static boolean isHeadless() {
        return isHeadless;
    }
}
