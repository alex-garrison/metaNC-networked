package server;

import client.NetworkedBoard;
import client.GameException;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class Server implements Runnable {
    private static final int PORT = 8000;
    private final int TIMEOUT_MILLIS = 2000;

    public static Server server;
    public static int serverID;

    private ServerSocket serverSocket;
    private static NetworkedBoard serverNetworkedBoard;
    private static ArrayList<ServerClient> serverClients;

    private static boolean isHeadless;
    private static boolean serverRunning;
    private static boolean gameRunning;
    private static boolean isNewGame;

    public Server() {
        serverClients = new ArrayList<>();
        serverNetworkedBoard = new NetworkedBoard();
        serverRunning = true;
        gameRunning = false;
        isNewGame = false;
        isHeadless = ServerMain.isHeadless();

        server = this;
        serverID = this.hashCode();
    }

    @Override
    public void run() {
        println("Started server");

        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(TIMEOUT_MILLIS);
        } catch (IOException e) {
            println("Error initialising server : " + e);
        }

        if (!isHeadless) setNetworkLabel();

        if (awaitServerClients()) println("Got serverClients : " + serverClients);

        awaitNewGame();

        serverGameLoop();

        broadcast("DISCONNECT");
        if (serverClients.size() > 0) {
            println("Disconnecting from clients");
        }
        closeClients();

        try {
            serverSocket.close();
        } catch (IOException e) {
            println("Error closing serverSocket : " + e.getMessage());
        }

        println("Server stopped");
        if (!isHeadless) ServerMain.serverStopped();
    }

    public static void turn(int[] location, int clientID) {
        if (serverRunning && gameRunning) {
            try {
                serverNetworkedBoard.turn(location, clientID);

                broadcast("BOARD:"+ serverNetworkedBoard.serialiseBoard());

                if (!serverNetworkedBoard.isWin()) {
                    send(getServerClientFromClientID(serverNetworkedBoard.getCurrentClientID()), "AWAITTURN");
                }
            } catch (GameException e) {
                ServerClient serverClient = getServerClientFromClientID(clientID);
                if (serverClient != null) {
                    send(serverClient, "ERROR:"+e.getMessage());
                } else {
                    println("Error with turn : " + e.getMessage());
                }

                if (e.getMessage().equals("Move not valid")) {
                    send(serverClient, "AWAITTURN");
                }
            }
        }
    }

    private static void broadcast(String message) {
        for (ServerClient serverClient : serverClients) {
            send(serverClient, message);
        }
    }

    private static void send(ServerClient serverClient, String message) {
        if (serverClient != null) {
            try {
                serverClient.getServerClientHandler().send(message);
            } catch(Exception e){
                println("Error sending message : " + message + " : " + e);
            }
        }
    }

    public static void println(String text) {
        if (isHeadless) {
            System.out.println(text);
        } else {
            ServerGUI.println(text);
        }
    }

    public static void setNetworkLabel() {
        try {
            ServerGUI.setNetworkLabel(InetAddress.getLocalHost(), PORT);
        } catch (UnknownHostException e) {
            println("Error setting network label : " + e);
        }
    }

    private void closeClients() {
        for (ServerClient serverClient : serverClients) {
            serverClient.stopClient();
        }

        while (serverClients.size() != 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void serverClientDisconnected(ServerClient serverClient) {
        serverClients.remove(serverClient);
        println("serverClient " + serverClient.getClientID() + " disconnected");
        serverRunning = false; gameRunning = false;
    }

    private boolean awaitServerClients() {
        while (serverClients.size() < 2) {
            if (!serverRunning) {
                return false;
            }

            try {
                Socket serverClientSocket = serverSocket.accept();
                ServerClientHandler serverClientHandler = new ServerClientHandler(serverClientSocket);
                Thread serverClientHandlerThread = new Thread(serverClientHandler);
                serverClientHandlerThread.start();

                ServerClient serverClient = new ServerClient(serverClientSocket, serverClientHandler, serverClientHandlerThread);

                serverClientHandler.setClientID(serverClient.getClientID());
                serverClients.add(serverClient);
            } catch (SocketTimeoutException e) {} catch (IOException e) {
                println("Error accepting serverClient : " + e);
            }
        }

        return true;
    }

    public static void newGame(int clientID) {
        isNewGame = true;
    }

    private void awaitNewGame() {
        isNewGame = false;

        while (serverRunning) {
            if (!isNewGame) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                break;
            }
        }
        isNewGame = false;
    }

    private void serverGameLoop() {
        serverLoop: while (serverRunning) {
            gameRunning = false;

            println("Starting new game");
            boolean clientsAssigned = setupClients();

            if (clientsAssigned) {
                serverNetworkedBoard.resetBoard();
                serverNetworkedBoard.setStarter("X");

                broadcast("BOARD:"+ serverNetworkedBoard.serialiseBoard());

                gameRunning = true;

                send(getServerClientFromClientID(serverNetworkedBoard.getCurrentClientID()), "AWAITTURN");

                while (gameRunning && serverRunning) {
                    if (serverNetworkedBoard.isWon) {
                        broadcast("BOARDWON:" + serverNetworkedBoard.winner);

                        awaitNewGame();
                        broadcast("NEWGAME");
                        continue serverLoop;
                    } else if (isNewGame) {
                        broadcast("NEWGAME");
                        isNewGame = false;
                        continue serverLoop;
                    } else {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    public boolean setupClients() {
        boolean clientsAssigned = false;

        try {
            serverNetworkedBoard.clearPlayers();
            for (ServerClient serverClient : serverClients) {
                try {
                    String player = serverNetworkedBoard.addPlayer(serverClient.getClientID());
                    println(serverClient.getClientID() + " assigned player : " + player);
                    send(serverClient, "ASSIGNPLAYER:" + player);
                    clientsAssigned = true;
                } catch (GameException e) {
                    println("Error assigning player : " + e.getMessage());
                    clientsAssigned = false;
                    break;
                }
            }
        } catch (ConcurrentModificationException e) {
            println("Error assigning players");
            clientsAssigned = false;
        }


        return clientsAssigned;
    }

    public static ServerClient getServerClientFromClientID(int clientID) {
        for (ServerClient serverClient: serverClients) {
            if (serverClient.getClientID() == clientID) {
                return serverClient;
            }
        }
        return null;
    }

    public static void stopRunning() {
        serverRunning = false; gameRunning = false;
    }
}
