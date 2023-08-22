package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client {
    private final int PORT = 8000;
    private final int TIMEOUT_MILLIS = 500;

    private InetAddress host;
    private int clientID;

    private Socket clientSocket;

    public Client() {
        startClient();
    }

    public void startClient() {
        try {
            host = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Error initialising host");
        }

        connectToServer();
        System.out.println("Connected to server : " + clientSocket);

        ClientReader reader = new ClientReader();
        Thread readerThread = new Thread(reader);
        readerThread.start();

        ClientWriter writer = new ClientWriter();

        try {
            readerThread.join();
        } catch (InterruptedException e) {
            System.out.println("Error waiting for readerThread to stop");
        }
        writer.close();

        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket");
        }

        System.out.println("Client stoppped");
    }

    private void connectToServer() {
        while (clientSocket == null) {
            try {
                clientSocket = new Socket(host.getHostName(), PORT);
                clientSocket.setSoTimeout(TIMEOUT_MILLIS);
            } catch (IOException e) {
                System.out.println("Error connecting to server : " + e);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private void setClientID(int clientID) {
        this.clientID = clientID;
        System.out.println("Set clientID : " + clientID);
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
                                    } catch (Exception e) {
                                        System.out.println("Error with setting clientID");
                                    }
                                    break;
                                case default:
                                    System.out.println("Server sent : " + receivedData);
                            }
                        }
                    }  catch (SocketTimeoutException e) {
                        continue;
                    } catch (IOException e) {
                        System.out.println("Error reading data : " + e);
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
