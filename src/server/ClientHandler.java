package server;

import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private boolean keepRunning;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        keepRunning = true;
    }

    @Override
    public void run() {
        System.out.println("ClientHandler started");
        while (keepRunning) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopRunning() {
        keepRunning = false;
    }
}
