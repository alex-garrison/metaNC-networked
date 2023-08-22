package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerClientHandler implements Runnable {
    private final Socket serverClientSocket;
    private ServerClientReader reader;
    private ServerClientWriter writer;
    private boolean keepRunning;

    private int clientID = -1;

    private final int TIMEOUT_MILLIS = 500;

    public ServerClientHandler(Socket serverClientSocket) {
        this.serverClientSocket = serverClientSocket;
        try {
            serverClientSocket.setSoTimeout(TIMEOUT_MILLIS);
        } catch (IOException e) {
            System.out.println("Error setting timeout");
        }

        keepRunning = true;
    }

    @Override
    public void run() {
        waitForClientID();

        reader = new ServerClientReader();
        Thread readerThread = new Thread(reader);
        readerThread.start();

        writer = new ServerClientWriter();

        writer.send("CLIENTID:" + clientID);

        while (keepRunning) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        writer.close();
        reader.stopRunning();
        try {
            readerThread.join();
        } catch (InterruptedException e) {
            System.out.println("Error waiting for readerThread to stop");
        }
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    private void waitForClientID() {
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

    public void stopRunning() {
        keepRunning = false;
    }

    private class ServerClientReader implements Runnable {
        private BufferedReader reader;
        private boolean keepRunning;

        public ServerClientReader() {
            keepRunning = true;
        }

        public void run() {
            while (keepRunning) {
                if (reader == null) {
                    try {
                        reader = new BufferedReader(new InputStreamReader(serverClientSocket.getInputStream()));
                    } catch (IOException e) {
                        System.out.println("Error initialising reader : " + e); continue;
                    }
                }

                if (!serverClientSocket.isClosed()) {
                    try {
                        String receivedData = reader.readLine();

                        if (receivedData == null) {
//                            System.out.println("Error with client."); keepRunning = false;
                            continue;
                        } else {
                            System.out.println("Client sent : " + receivedData);
                        }
                    }  catch (SocketTimeoutException e) {
                        continue;
                    } catch (IOException e) {
                        System.out.println("Error reading data : " + e);
                        keepRunning = false;
                    }
                } else {
                    keepRunning = false;
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

    private class ServerClientWriter {
        private BufferedWriter writer;

        public ServerClientWriter() {
            try {
                writer = new BufferedWriter(new OutputStreamWriter(serverClientSocket.getOutputStream()));
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
