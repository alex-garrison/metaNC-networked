package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server implements Runnable {
    private final int PORT = 8000;

    private ServerSocket serverSocket;
    private final ArrayList<Client> clients;

    public Server() {
        clients = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(200);
        } catch (IOException e) {
            System.out.println("Error initialising server : " + e);
        }

        awaitClients();

        System.out.println("Got clients");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Client client: clients) {
            client.stopClient();
        }

        System.out.println("Closed clients");
    }

    private void awaitClients() {
        while (clients.size() < 2) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientHandlerThread = new Thread(clientHandler);
                clientHandlerThread.start();

                Client client = new Client(clientSocket, clientHandler, clientHandlerThread);

                clients.add(client);
                System.out.println("Accepted client " + client.getClientID());
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                System.out.println("Error accepting client : " + e);
            }
        }
    }
}
