package client;

public class ClientMain {
    public static Client client;
    private static Thread clientThread;
    public static void main(String[] args) {

        try {
            GUI.startGUI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void startClient() {
        client = new Client();
        clientThread = new Thread(client);
        clientThread.start();
    }

}
