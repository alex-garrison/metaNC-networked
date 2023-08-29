package server;

import client.AiAgent;
import client.NetworkedBoard;
import client.GameException;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class Server implements Runnable {
    private static final int PORT = 8000;
    private final int TIMEOUT_MILLIS = 2000;
    private final static int AI_MOVE_DELAY = 80;

    public static Server server;
    public static int serverID;

    private ServerSocket serverSocket;
    private static NetworkedBoard serverNetworkedBoard;
    private static ArrayList<ServerClient> serverClients;

    private static boolean isHeadless;
    private static boolean serverRunning;
    private static boolean gameRunning;
    private static boolean isNewGame;
    private static String gameMode;
    private static ServerClient currentClient;

    boolean isPVP = false;
    boolean isPVAI = false;
    boolean isAIVAI = false;

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


        if (awaitServerClients()) println("Got serverClients");

        awaitGamemode();
        awaitNewGame(true);

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
                if (server.isPVP) {
                    broadcast("BOARD:"+ serverNetworkedBoard.serialiseBoard());
                } else if (server.isPVAI || server.isAIVAI) {
                    send(currentClient, "BOARD:"+ serverNetworkedBoard.serialiseBoard());
                }

                if (!serverNetworkedBoard.isWin()) {
                    if (((server.isPVP) || ((server.isPVAI && !isAiClientID(serverNetworkedBoard.getCurrentClientID()))))) {
                        send(getServerClientFromClientID(serverNetworkedBoard.getCurrentClientID()), "AWAITTURN");
                    } else if (server.isPVAI || server.isAIVAI) {
                        aiTurn(serverNetworkedBoard.getCurrentClientID());
                    }
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

    public static void aiTurn(int clientID) {
        if (isAiClientID(clientID) && serverRunning && gameRunning) {
            AiAgent ai = new AiAgent(serverNetworkedBoard);
            try {
                Thread.sleep(AI_MOVE_DELAY);
                turn(ai.getMove(), serverNetworkedBoard.getCurrentClientID());
            } catch (GameException e) {
                println("Error with AI turn : " + e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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

    public void setGameMode(String gameMode) {
        if (gameMode.equals("PvP") || gameMode.equals("PvAI") || gameMode.equals("AIvAI")) {
            Server.gameMode = gameMode;
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
        currentClient = getServerClientFromClientID(clientID);
        isNewGame = true;
    }

    private void awaitNewGame(boolean start) {
        isNewGame = start;

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

    private void awaitGamemode() {
        gameMode = null;

        while (serverRunning) {
            if ((gameMode == null) || (gameMode.isEmpty())) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            } else {
                break;
            }
        }
    }

    private void serverGameLoop() {
        serverLoop: while (serverRunning) {
            switch (gameMode) {
                case "PvP" -> {
                    isPVP = true;
                    isPVAI = false;
                    isAIVAI = false;
                }
                case "PvAI" -> {
                    isPVAI = true;
                    isPVP = false;
                    isAIVAI = false;
                }
                case "AIvAI" -> {
                    isAIVAI = true;
                    isPVAI = false;
                    isPVP = false;
                }
            }

            gameRunning = false;

            boolean clientsAssigned = setupClients();

            if (clientsAssigned) {
                serverNetworkedBoard.resetBoard();
                serverNetworkedBoard.setStarter("X");

                broadcast("BOARD:"+ serverNetworkedBoard.serialiseBoard());

                gameRunning = true;

                if (isPVP || isPVAI) {
                    if (!isAiClientID(serverNetworkedBoard.getCurrentClientID())) {
                        send(getServerClientFromClientID(serverNetworkedBoard.getCurrentClientID()), "AWAITTURN");
                    } else if (isPVAI) {
                        aiTurn(serverNetworkedBoard.getCurrentClientID());
                    }
                } else if (isAIVAI) {
                    aiTurn(serverNetworkedBoard.getCurrentClientID());
                }


                while (gameRunning && serverRunning) {
                    if (serverNetworkedBoard.isWon) {
                        if (isPVP) {
                            broadcast("BOARDWON");
                        } else if (isPVAI || isAIVAI) {
                            send(currentClient, "BOARDWON");
                        }
                        awaitNewGame(false);
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

        println("Mode is : " + gameMode);

        if (isPVP) {
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
        } else if (isPVAI) {
            try {
                serverNetworkedBoard.clearPlayers();
                String player = serverNetworkedBoard.addPlayer(currentClient.getClientID());
                println(currentClient.getClientID() + " assigned player : " + player);
                send(currentClient, "ASSIGNPLAYER:" + player);
                String aiPlayer = serverNetworkedBoard.addPlayer();
                println("AI assigned player : " + aiPlayer);
                clientsAssigned = true;
            } catch (GameException e) {
                println("Error assigning player : " + e.getMessage());
                clientsAssigned = false;
            }
        } else if (isAIVAI) {
            try {
                serverNetworkedBoard.clearPlayers();
                for (int i = 0; i < 2; i++) {
                    String aiPlayer = serverNetworkedBoard.addPlayer(i);
                    println("AI " + i + " assigned player : " + aiPlayer);
                }
                clientsAssigned = true;
            } catch (GameException e) {
                println("Error assigning player : " + e.getMessage());
                clientsAssigned = false;
            }
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

    public static boolean isAiClientID(int clientID) {
        return (clientID >= 0 && clientID < 10);
    }

    public static void stopRunning() {
        serverRunning = false; gameRunning = false;
    }
}
