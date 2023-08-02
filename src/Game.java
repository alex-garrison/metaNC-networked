import java.util.Scanner;

public class Game {
    private final Scanner scan;
    private final Board board;
    private String mode;

    public Game(Scanner newScan) {
        scan = newScan;
        board = new Board();
    }

    public void play() throws GameException, InterruptedException {
        if (mode.isEmpty()) {
            throw new GameException("Mode not set.");
        } else {
            gameLoop();
        }
    }

    private void gameLoop() throws GameException {
        boolean isWon;
        boolean isHuman = !(mode.equals("AIVAI"));
        boolean isAIVAI = !isHuman;
        int[] moveLocation = new int[]{0,0};
        AiAgent ai = new AiAgent(board);
        board.emptyBoard();

        if (isAIVAI) {board.setStarter(ai.getStarter());}
        else {board.setStarter(PlayerInput.getStarter(scan));}

        while (true) {
            isWon = checkWin();
            if (isWon) {break; }

            if (isHuman) { moveLocation = PlayerInput.getMove(scan, board.whoseTurn());}
            else { moveLocation = ai.getMove(); }

            try {
                board.turn(board.whoseTurn(), moveLocation);
            } catch (GameException e) {
                System.out.println(e.getMessage());
            }

            if (!isHuman && !isAIVAI) {
                System.out.println("Player " + board.invertPlayer(board.whoseTurn()) + " moved at " + moveLocation[0]+"/"+moveLocation[1] + "\n");
            }

            if (mode.equals("AI")) {isHuman = !isHuman;}

            if (!isAIVAI) System.out.println(board.toString(moveLocation));
        }

        if (isAIVAI) System.out.println( board.toString(moveLocation) );
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
            }
            return true;
        } else {
            return false;
        }
    }
}
