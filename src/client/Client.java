package client;

import java.io.*;
import java.net.*;

public class Client implements Runnable {
    private final InetAddress host;
    private final int port;

    private final int TIMEOUT_MILLIS = 500;

    private int clientID;
    private String player;
    private boolean isClientTurn;
    private boolean boardWon;

    private Socket clientSocket;
    private boolean isConnected;
    private ClientWriter writer;
    private ClientReader reader;

    public Client(InetAddress host, int port) {
        this.host = host;
        this.port = port;

        clientID = -1;
        isConnected = false;
        boardWon = false;
    }

    public void run() {
        ClientGUI.frame.resetBoardPanels();
        ClientGUI.frame.clearBottomLabel();
        ClientGUI.frame.clearNetworkLabel();
        ClientGUI.frame.clearPlayerLabel();

        if (connectToServer()) {
            ClientGUI.frame.setNetworkLabel("Connected", false);
            System.out.println("Connected to server : " + clientSocket);
            isConnected = true;
            ClientGUI.frame.setNetworked(true);
            ClientGUI.frame.setNetworkButtonFunction(false);
            ClientGUI.frame.setNetworkMode(true);

            reader = new ClientReader();
            Thread readerThread = new Thread(reader);
            readerThread.start();

            writer = new ClientWriter();

            try {
                readerThread.join();
            } catch (InterruptedException e) {
                System.out.println("Error waiting for readerThread to stop");
            }
            writer.close();

            isConnected = false;
            ClientGUI.frame.setNetworkLabel("Disconnected" , true);
            ClientGUI.frame.clearBottomLabel();
            ClientGUI.frame.clearPlayerLabel();
            ClientGUI.frame.setNetworkMode(false);
            ClientGUI.frame.setNetworked(false);
            ClientGUI.frame.setNetworkButtonFunction(true);
            clientID = 0;

        } else {
            ClientGUI.frame.setNetworkLabel("Error connecting" , true);
        }

        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket");
            }
        }

        System.out.println("Client stopped");

        ClientMain.restartGameloop(true);
    }

    public void turn(int[] location) {
        if (isClientTurn && !boardWon) {
            if (location.length == 3) {
                writer.send("TURN:" + location[0] + location[1] + location[2]);
                isClientTurn = false;
            }
        }
    }

    private boolean connectToServer() {
        int connectionFailCounter = 0;
        while (clientSocket == null) {
            if (connectionFailCounter >= 5) {
                break;
            }
            try {
                clientSocket = new Socket(host.getHostName(), port);
                clientSocket.setSoTimeout(TIMEOUT_MILLIS);
                return true;
            } catch (IOException e) {
                System.out.println("Error connecting to server : " + e);
                connectionFailCounter++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return false;
    }

    public void disconnect() {
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                writer.send("DISCONNECT");
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket");
            }
        }
    }

    public void sendNewGame() {
        if (writer != null) {
            writer.send("NEWGAME");
        }
    }

    private void setClientID(int clientID) {
        this.clientID = clientID;
        ClientGUI.frame.setNetworkLabel(ClientMain.client.getClientID());
        System.out.println("Set clientID : " + clientID);
    }

    public void setPlayer(String player) {
        this.player = player;
        ClientGUI.frame.setPlayerLabel(this.player, false);
    }

    public void setClientTurn(boolean isClientTurn) {
        ClientGUI.frame.setPlayerLabel(ClientGUI.frame.getPlayerLabel(), isClientTurn);
        this.isClientTurn = true;
    }

    public void boardWon(String winner) {
        if (winner.equals(player)) {
            ClientGUI.frame.setBottomLabel("You won!", false, true);
        } else if (winner.equals(Board.invertPlayer(player))) {
            ClientGUI.frame.setBottomLabel("You lost", true, false);
        } else if (winner.equals("D")) {
            ClientGUI.frame.setBottomLabel("Draw", false, false);
        }
        boardWon = true;
    }

    public int getClientID() {
        return clientID;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void updateBoard(String serialisedBoard) {
        try {
            NetworkedBoard newNetworkedBoard = new NetworkedBoard();
            try {
                newNetworkedBoard.deserializeBoard(serialisedBoard);
            } catch (NumberFormatException e) {
                System.out.println(serialisedBoard);
            }

            if (newNetworkedBoard.isEmptyBoard()) {
                ClientGUI.frame.resetBoardPanels();
                ClientGUI.frame.clearBottomLabel();
            }

            newNetworkedBoard.isWin();
            ClientGUI.frame.updateBoard(newNetworkedBoard);
            ClientGUI.frame.setBoardColours(newNetworkedBoard, player);
            ClientGUI.frame.clearBottomLabel();
        } catch (GameException e) {
            System.out.println("Board error : " + e);
        }
    }

    private class ClientReader implements Runnable {
        private BufferedReader reader;
        private boolean keepRunning;

        private int nullDataCounter = 0;

        public ClientReader() {
            keepRunning = true;
        }

        public void run() {
            while (keepRunning) {
                if (reader == null) {
                    try {
                        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    } catch (IOException e) {
                        System.out.println("Error initialising reader : " + e);
                    }
                } else {
                    try {
                        if (nullDataCounter >= 5) {
                            System.out.println("Error with server.");
                            keepRunning = false; continue;
                        }

                        String receivedData = reader.readLine();

                        if (receivedData == null) {
                            nullDataCounter++;
                        } else if (!receivedData.isEmpty()){
                            String[] args = receivedData.split(":");
                            switch (args[0]) {
                                case "CLIENTID":
                                    try {
                                        setClientID(Integer.parseInt(args[1]));
                                    } catch (IndexOutOfBoundsException e) {
                                        System.out.println("Error with CLIENTID command");
                                    } catch (Exception e) {
                                        System.out.println("Error with setting clientID");
                                    }
                                    break;
                                case "BOARD":
                                    try {
                                        updateBoard(args[1]);
                                        setClientTurn(false);
                                    } catch (IndexOutOfBoundsException e) {
                                        System.out.println("Error with BOARD command");
                                    }
                                    break;
                                case "NEWGAME":
                                    ClientGUI.frame.clearPlayerLabel();
                                    boardWon = false;
                                    break;
                                case "ASSIGNPLAYER":
                                    try {
                                        setPlayer(args[1]);
                                    } catch (IndexOutOfBoundsException e) {
                                        System.out.println("Error with ASSIGNPLAYER command");
                                    }
                                    break;
                                case "AWAITTURN":
                                    setClientTurn(true);
                                    break;
                                case "BOARDWON":
                                    try {
                                        boardWon(args[1]);
                                    } catch (IndexOutOfBoundsException e) {
                                        System.out.println("Error with BOARDWON command");
                                    }
                                    break;
                                case "ERROR":
                                    try {
                                        ClientGUI.frame.setBottomLabel(args[1], true, false);
                                    } catch (IndexOutOfBoundsException e) {
                                        System.out.println("Error with ERROR command");
                                    } catch (Exception e) {
                                        System.out.println("Error displaying error message : " + e);
                                    }
                                    break;
                                case "DISCONNECT":
                                    keepRunning = false; break;
                                case default:
                                    System.out.println("Server sent : " + receivedData);
                            }
                        }
                    }  catch (SocketTimeoutException e) {} catch (IOException e) {
                        System.out.println("Error reading data : " + e);
                        keepRunning = false;
                    }
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Error closing reader" + e);
                }
            }
        }

        public void stopRunning() {
            keepRunning = false;
        }
    }

    private class ClientWriter {
        private BufferedWriter writer;

        public ClientWriter() {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (IOException e) {
                System.out.println("Error initialising writer : " + e);
            }
        }

        public void send(String message) {
            boolean messageSent = false;
            int errorCount = 0;

            while (!messageSent) {
                try {
                    writer.write(message.strip());
                    writer.newLine();
                    writer.flush();
                    messageSent = true;
                } catch (SocketTimeoutException e) {} catch (IOException e) {
                    if (errorCount >= 5) {
                        return;
                    } else {
                        System.out.println("Error sending message : " + e);
                        errorCount++;
                    }
                }
            }
        }

        public void close() {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Error closing writer" + e);
                }
            }
        }
    }
}
