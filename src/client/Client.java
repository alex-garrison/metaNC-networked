package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client implements Runnable {
    private final int PORT = 8000;
    private final int TIMEOUT_MILLIS = 500;

    private InetAddress host;
    public int clientID;

    private Socket clientSocket;
    private ClientWriter writer;
    private ClientReader reader;

    public Client() {
        clientID = -1;
    }

    public void run() {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Error initialising host");
        }

        if (connectToServer()) {
            System.out.println("Connected to server : " + clientSocket);

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
        }

        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket");
            }
        }

        System.out.println("Client stoppped");
    }

    public void turn(int[] location) {
        if (location.length == 3) {
            writer.send("TURN:" + location[0] + location[1] + location[2]);
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

    private void setClientID(int clientID) {
        this.clientID = clientID;
        System.out.println("Set clientID : " + clientID);
    }

    private void updateBoard(String serialisedBoard) {
        try {
            Board newBoard = new Board();
            newBoard.deserializeBoard(serialisedBoard);
            newBoard.isWin();
            GUI.frame.updateBoard(newBoard);
            GUI.frame.setBoardColours(newBoard);
            GUI.frame.clearBottomLabel();
        } catch (GameException e) {
            System.out.println("Board error : " + e);
        }
    }

    public void waitForClientID() {
        while (true) {
            if (clientID == -1) {
                try {
                    Thread.sleep(200);
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
                        String receivedData = reader.readLine();

                        if (receivedData == null) {
                            System.out.println("Error with server."); keepRunning = false;
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
                                    } catch (IndexOutOfBoundsException e) {
                                        System.out.println("Error with BOARD command");
                                    }
                                    break;
                                case "ERROR":
                                    try {
                                        GUI.frame.setBottomLabel(args[1], true);
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
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (IOException e) {
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
