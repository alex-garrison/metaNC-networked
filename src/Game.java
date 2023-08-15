public class Game {
    private final Board board;
    private String mode;

    public Game() {
        board = new Board();
    }

    public void play() throws GameException {
        if (mode.isEmpty()) {
            throw new GameException("Mode not set.");
        } else {
            gameLoop();
        }
    }

    private void gameLoop() {
        boolean errorOccured = false;
        int[] moveLocation;
        board.emptyBoard();

        board.setStarter("X");

        while (true) {
            if (board.isWin()) {
                if (board.winner.equals("Draw")) {
                    GUI.frame.setBottomLabel("Draw", false);
                } else {
                    GUI.frame.setBottomLabel("Player " + board.winner + " wins.", false);
                }
                break;
            }

            GUI.frame.showBoard(board);
            GUI.frame.setBoardColours(board);

            moveLocation = GUI.frame.waitForMove();

            try {
                board.turn(board.whoseTurn(), moveLocation);
            } catch (GameException e) {
                GUI.frame.setBottomLabel(e.getMessage(), true);
                errorOccured = true;
            }

            if (!errorOccured) {
                GUI.frame.clearBottomLabel();
            }
        }
        GUI.frame.showBoard(board);
        GUI.frame.setBoardColours(board);
    }

    public void setMode(String newMode) {
        mode = newMode;
    }
}
