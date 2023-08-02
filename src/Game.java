import java.util.Scanner;

public class Game {
    private Scanner scan;
    private Board board;
    private String mode;

    public Game(Scanner newScan) {
        scan = newScan;
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
        boolean isWon = false;
        boolean isHuman = !(mode.equals("AIVAI"));
        boolean isAIVAI = !isHuman;
        int moveLocation;
        AiAgent ai = new AiAgent(board);
        board.emptyBoard();

        if (isAIVAI) {board.setStarter(ai.getStarter());}
        else {board.setStarter(PlayerInput.getStarter(scan));}

        while (true) {
            isWon = checkWin();
            if (isWon) {break;}

            if (isHuman) { moveLocation = PlayerInput.getMove(scan, board.whoseTurn());}
            else { moveLocation = ai.getMove(); }

            try {
                board.turn(board.whoseTurn(), moveLocation);
            } catch (GameException e) {
                System.out.println(e.getMessage());
            }

            if (!isHuman && !isAIVAI) {
                System.out.println("Player " + board.invertPlayer(board.whoseTurn()) + " moved at " + moveLocation + "\n");
            }

            if (mode.equals("AI")) {isHuman = !isHuman;}

            int[][] lastMoveArr = new int[][]{board.getLastMoveArr()};
            if (!isAIVAI) System.out.println( board.toStringColour(lastMoveArr, ConsoleColours.PURPLE) );
        }

    }

    public void setMode(String newMode) {
        mode = newMode;
    }

    private boolean checkWin() {
        Win win = board.isWin();
        if (win.isWin()) {
            if (win.getWinType().equals("draw")) {
                System.out.println("Draw");
            } else {
                System.out.println("Player " + win.getWinner() + " wins");
                System.out.println(board.toStringColour(win.getWinTiles(), ConsoleColours.RED));
            }
            return true;
        } else {
            return false;
        }
    }
}
