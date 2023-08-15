public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.emptyBoard();

        try {
            GUI.startGUI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        GUI.frame.showBoard(board);

        GUI.frame.waitForNewGame();
        Game game = new Game();
        game.setMode(GUI.frame.getMode());

        try {
            game.play();
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
    }
}
