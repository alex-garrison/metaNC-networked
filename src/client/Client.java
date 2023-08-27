package client;

import java.io.*;
import java.net.*;

public class Client implements Runnable {
    private final int PORT = 8000;
    private final int TIMEOUT_MILLIS = 500;

    private InetAddress host;
    private int clientID;
    private String player;
    private boolean isClientTurn;

    private Socket clientSocket;
    private boolean isConnected;
    private ClientWriter writer;
    private ClientReader reader;

    public Client() {
        clientID = -1;
        isConnected = false;
    }

    public void run() {
        try {
//            host = InetAddress.getByName("192.168.86.202");
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Error initialising host");
        }

        ClientGUI.frame.resetBoardPanels();
        ClientGUI.frame.clearBottomLabel();
        ClientGUI.frame.clearNetworkLabel();
        ClientGUI.frame.clearPlayerLabel();

        if (connectToServer()) {
            System.out.println("Connected to server : " + clientSocket);
            isConnected = true;
            ClientGUI.frame.setNetworkButtonFunction(false);

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
            ClientGUI.frame.setNetworkLabel("Server disconnected" , true);
            ClientGUI.frame.clearPlayerLabel();

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

        ClientGUI.frame.setNetworkButtonFunction(true);
    }

    public void turn(int[] location) {
        if (isClientTurn) {
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
                clientSocket = new Socket(host.getHostName(), PORT);
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
            writer.send("SETMODE:" + ClientGUI.frame.getMode());
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

    public void boardWon() {
        ClientGUI.frame.setBottomLabel("Board won", false);
    }

    public int getClientID() {
        return clientID;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void updateBoard(String serialisedBoard) {
        try {
            Board newBoard = new Board();
            try {
                newBoard.deserializeBoard(serialisedBoard);
            } catch (NumberFormatException e) {
                System.out.println(serialisedBoard);
            }

            if (newBoard.isEmptyBoard()) {
                ClientGUI.frame.resetBoardPanels();
                ClientGUI.frame.clearBottomLabel();
            }

            newBoard.isWin();
            ClientGUI.frame.updateBoard(newBoard);
            ClientGUI.frame.setBoardColours(newBoard, player);
            ClientGUI.frame.clearBottomLabel();
        } catch (GameException e) {
            System.out.println("Board error : " + e);
        }
    }

    public void waitForClientID() throws RuntimeException {
        int timeoutCount = 0;
        while (true) {
            if (timeoutCount >= 5) {
                throw new RuntimeException("Waiting timed out");
            } else if (clientID == -1) {
                try {
                    Thread.sleep(200);
                    timeoutCount++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                break;
            }
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
                                    boardWon();
                                    break;
                                case "ERROR":
                                    try {
                                        ClientGUI.frame.setBottomLabel(args[1], true);
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
                    }  catch (SocketTimeoutException e) {
                        continue;
                    } catch (IOException e) {
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
