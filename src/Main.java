public class Main {
    public static void main(String[] args) {
        try {
            GUI.startGUI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        GUI.frame.waitForNewGame();

        Thread gameLoopThread;

        while (!Thread.currentThread().isInterrupted()) {
            gameLoopThread = new Thread(new GameLoop());
            gameLoopThread.start();
            new NewGameLoop(gameLoopThread);
            GUI.frame.resetBoard();
        }
    }
}
