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

        System.out.println("Accepted serverClient " + clientID);
    }

    public void stopClient() {
        try {
            serverClientHandler.stopRunning();
            serverClientHandlerThread.join();
            serverClientSocket.close();
            System.out.println("closed serverclient " + clientID);
        } catch (IOException | InterruptedException e) {
            System.out.println("Error stopping client : " + e);
        }
    }

    public int getClientID() {
        return clientID;
    }

    public ServerClientHandler getServerClientHandler() {
        return serverClientHandler;
    }

}
