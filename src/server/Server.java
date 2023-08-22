package server;

import client.AiAgent;
import client.Board;
import client.GameException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server implements Runnable {
    private final int PORT = 8000;
    private final int TIMEOUT_MILLIS = 2000;

    private ServerSocket serverSocket;
    private static Board serverBoard;
    private static ArrayList<ServerClient> serverClients;

    public Server() {
        serverClients = new ArrayList<>();
        serverBoard = new Board();
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

        serverBoard.emptyBoard();
        serverBoard.setStarter("X");
        AiAgent ai = new AiAgent(serverBoard);

        while (!serverBoard.isWin()){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        broadcast("DISCONNECT");
        System.out.println("Disconnecting from clients");

        for (ServerClient serverClient : serverClients) {
            serverClient.stopClient();
        }
    }

    public static void turn(int[] location, int clientID) {
        try {
            serverBoard.turn(serverBoard.whoseTurn(), location);
            broadcast("BOARD:"+serverBoard.serialiseBoard());
        } catch (GameException e) {
            ServerClient serverClient = getServerClientFromClientID(clientID);
            if (serverClient != null) {
                send(serverClient, "ERROR:"+e.getMessage());
            } else {
                System.out.println("Error with clientID ");
            }
        }
    }

    private static void broadcast(String message) {
        for (ServerClient serverClient : serverClients) {
            send(serverClient, message);
        }
    }

    private static void send(ServerClient serverClient, String message) {
        serverClient.getServerClientHandler().send(message);
    }

    private void awaitServerClients() {
        while (serverClients.size() < 1) {
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

    private static ServerClient getServerClientFromClientID(int clientID) {
        for (ServerClient serverClient: serverClients) {
            if (serverClient.getClientID() == clientID) {
                return serverClient;
            }
        }
        return null;
    }
}
