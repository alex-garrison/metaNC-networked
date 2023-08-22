package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server implements Runnable {
    private final int PORT = 8000;
    private final int TIMEOUT_MILLIS = 2000;

    private ServerSocket serverSocket;
    private final ArrayList<ServerClient> serverClients;

    public Server() {
        serverClients = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(TIMEOUT_MILLIS);
        } catch (IOException e) {
            System.out.println("Error initialising server : " + e);
        }

        awaitServerClients();

        System.out.println("Got serverClients");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (ServerClient serverClient : serverClients) {
            serverClient.stopClient();
        }
    }

    private void awaitServerClients() {
        while (serverClients.size() < 2) {
            try {
                Socket serverClientSocket = serverSocket.accept();
                ServerClientHandler serverClientHandler = new ServerClientHandler(serverClientSocket);
                Thread serverClientHandlerThread = new Thread(serverClientHandler);
                serverClientHandlerThread.start();

                ServerClient serverClient = new ServerClient(serverClientSocket, serverClientHandler, serverClientHandlerThread);

                serverClientHandler.setClientID(serverClient.getClientID());
                serverClients.add(serverClient);
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                System.out.println("Error accepting serverClient : " + e);
            }
        }
    }
}
