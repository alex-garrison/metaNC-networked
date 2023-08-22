package server;

import java.io.IOException;
import java.net.Socket;

public class Client {
    private final Socket clientSocket;
    private final ClientHandler clientHandler;
    private final Thread clientHandlerThread;
    private final int clientID;

    public Client(Socket clientSocket, ClientHandler clientHandler, Thread clientHandlerThread) {
        this.clientSocket = clientSocket;
        this.clientHandler = clientHandler;
        this.clientHandlerThread = clientHandlerThread;
        this.clientID = this.hashCode();
    }

    public void stopClient() {
        try {
            clientHandler.stopRunning();
            clientHandlerThread.join();
            clientSocket.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("Error stopping client : " + e);
        }
    }

    public int getClientID() {
        return clientID;
    }
}
