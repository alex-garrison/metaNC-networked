package server;

import java.io.IOException;
import java.net.Socket;

public class ServerClient {
    private final Socket serverClientSocket;
    private final ServerClientHandler serverClientHandler;
    private final Thread serverClientHandlerThread;
    private final int clientID;

    public ServerClient(Socket serverClientSocket, ServerClientHandler serverClientHandler, Thread serverClientHandlerThread) {
        this.serverClientSocket = serverClientSocket;
        this.serverClientHandler = serverClientHandler;
        this.serverClientHandlerThread = serverClientHandlerThread;
        this.clientID = this.hashCode();

        Server.println("Accepted serverClient " + clientID);

        new Thread(new monitor(this)).start();
    }

    public void stopClient() {
        serverClientHandler.stopRunning();
    }

    public int getClientID() {
        return clientID;
    }

    public ServerClientHandler getServerClientHandler() {
        return serverClientHandler;
    }

    public String toString() {
        return "cID:" + clientID;
    }

    private class monitor implements Runnable {
        ServerClient serverClient;
        public monitor(ServerClient serverClient) {
            this.serverClient = serverClient;
        }
        public void run() {
            try {
                serverClientHandlerThread.join();
            } catch (InterruptedException e) {
                Server.println("Error waiting for serverClientHandlerThread to stop");
            } finally {
                if (serverClientSocket != null) {
                    try {
                        serverClientSocket.close();
                    } catch (IOException e) {
                        Server.println("Error closing serverClientSocket : " + e);
                    }
                }
                Server.serverClientDisconnected(serverClient);
            }
        }
    }

}
