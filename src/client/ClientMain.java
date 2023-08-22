package client;

public class ClientMain {

    public static Client client;
    public static void main(String[] args) {
        client = new Client();

        try {
            GUI.startGUI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        GUI.frame.setClientIDLabel(client.clientID);
//
//        GUI.frame.waitForNewGame();
//
//        Thread gameLoopThread;
//
//        while (!Thread.currentThread().isInterrupted()) {
//            gameLoopThread = new Thread(new GameLoop());
//            gameLoopThread.start();
//            new NewGameLoop(gameLoopThread);
//            GUI.frame.resetBoard();
//        }
    }

    public static void startClient() {
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}
