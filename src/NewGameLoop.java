public class NewGameLoop {
    public NewGameLoop(Thread gameLoopThread) {
        GUI.frame.waitForNewGame();
        gameLoopThread.interrupt();
    }
}
