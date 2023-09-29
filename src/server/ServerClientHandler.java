package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

public class ServerClientHandler implements Runnable {
    private final Socket serverClientSocket;
    private ServerClient serverClient;
    private Lobby lobby;

    private ServerClientReader reader;
    private ServerClientWriter writer;
    private boolean keepRunning;

    private String serverKey;
    private int clientID = 0;

    private final int TIMEOUT_MILLIS = 500;

    public ServerClientHandler(Socket serverClientSocket) {
        this.serverClientSocket = serverClientSocket;
        try {
            serverClientSocket.setSoTimeout(TIMEOUT_MILLIS);
        } catch (IOException e) {
            output("Error setting timeout");
        }

        serverKey = getServerKey();

        keepRunning = true;
    }

    @Override
    public void run() {
        reader = new ServerClientReader();
        Thread readerThread = new Thread(reader);
        readerThread.start();

        writer = new ServerClientWriter();

        while (keepRunning) {
            if (!reader.keepRunning) {
                keepRunning = false; continue;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        reader.stopRunning();
        try {
            readerThread.join();
        } catch (InterruptedException e) {
            output("Error waiting for readerThread to stop");
        }

        writer.close();
    }

    public void send(String message) {
        while (true) {
            if (writer != null) {
                writer.send(message);
                break;
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setServerClient(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
        send("LOBBYID:"+lobby.lobbyID);
    }

    private void output(String text) {
        Server.print("C" + clientID + ": " + text);
    }

    public void stopRunning() {
        keepRunning = false;
    }

    private String getServerKey() {
        String urlStr = "https://alex-garrison.github.io/server-key";

        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            StringBuilder content = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();

            return content.toString().strip();
        } catch (IOException e) {
            System.out.println("Error retrieving server IP");
        }
        return null;
    }

    private class ServerClientReader implements Runnable {
        private BufferedReader reader;
        private boolean keepRunning;

        private int nullDataCounter = 0;

        public ServerClientReader() {
            keepRunning = true;
        }

        public void run() {
            while (keepRunning) {
                if (reader == null) {
                    try {
                        reader = new BufferedReader(new InputStreamReader(serverClientSocket.getInputStream()));
                    } catch (IOException e) {
                        output("Error initialising reader : " + e); keepRunning = false; continue;
                    }
                }

                if (!serverClientSocket.isClosed()) {
                    try {
                        if (nullDataCounter >= 10) {
                            keepRunning = false;
                            continue;
                        }

                        String receivedData = reader.readLine();

                        if (receivedData == null) {
                            nullDataCounter++;
                            Thread.sleep(100);
                        } else if (!receivedData.isEmpty()) {
                            if (!serverClient.isAuthorised()) {
                                if (receivedData.equals("AUTH:" + serverKey)) {
                                    serverClient.authoriseClient();
                                    writer.send("CLIENTID:" + clientID);
                                }
                            } else {
                                String[] args = receivedData.split(":");
                                if (args[0].equals("TURN")) {
                                    try {
                                        String[] locationString = args[1].split("");
                                        int[] location = new int[3];
                                        for (int i = 0; i < location.length; i++) {
                                            location[i] = Integer.parseInt(locationString[i]);
                                        }
                                        lobby.turn(location, clientID);
                                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                                        output("Error with TURN command");
                                    }
                                } else if (args[0].equals("DISCONNECT")) {
                                    stopRunning();
                                } else if (args[0].equals("NEWGAME")) {
                                    if (lobby != null) {
                                        lobby.newGame();
                                    }
                                } else {
                                    output("Client sent : " + receivedData);
                                }
                            }
                        }
                    }  catch (SocketTimeoutException e) {} catch (IOException e) {
                        output("Error reading data : " + e);
                        keepRunning = false;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    output("Sockets closed");
                    keepRunning = false;
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    output("Error closing reader" + e);
                }
            }

            this.stopRunning();
        }

        public void stopRunning() {
            keepRunning = false;
        }
    }

    private class ServerClientWriter {
        private BufferedWriter writer;

        public ServerClientWriter() {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(serverClientSocket.getOutputStream()));
            } catch (IOException e) {
                output("Error initialising writer : " + e);
            }
        }

        public void send(String message) {
            boolean messageSent = false;
            int errorCount = 0;

            while (!messageSent && writer != null) {
                try {
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                    messageSent = true;
                } catch (SocketTimeoutException e) {} catch (IOException e) {
                    if (errorCount >= 5) {
                        close();
                    } else {
                        output("Error sending message : " + e);
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
                    output("Error closing writer" + e);
                }
            }
        }
    }
}
