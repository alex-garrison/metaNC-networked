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
        int[] moveLocation = new int[2];
        AiAgent ai = new AiAgent(board);
        board.emptyBoard();

        if (isAIVAI) {board.setStarter(ai.getStarter());}
        else {board.setStarter(PlayerInput.getStarter(scan));}

        gameLoop: while (true) {
            isWon = checkWin();
            if (isWon) {break; }

            if (isHuman) { moveLocation = PlayerInput.getMove(scan, board.whoseTurn());}
            else { moveLocation = ai.getMove(); }

            try {
                board.turn(board.whoseTurn(), moveLocation);
            } catch (GameException e) {
                System.out.println(e.getMessage());
                continue gameLoop;
            }

            if (!isHuman && !isAIVAI) {
                System.out.println("Player " + board.invertPlayer(board.whoseTurn()) + " moved at " + moveLocation[0]+"/"+moveLocation[1]);
            }

            if (mode.equals("AI")) {isHuman = !isHuman;}

            printWins();
            System.out.println(board.toString(moveLocation));
        }

        printWins();
        if (isAIVAI) System.out.println( board.toString(moveLocation) );
    }

    public void setMode(String newMode) {
        mode = newMode;
    }

    private void printWins() {
        Win[] localBoardWins = board.getWins();
        for (Win localBoard: localBoardWins) {
            if (localBoard.isWin()) {
                if (localBoard.getWinType().equals("draw")) {
                    System.out.println("Draw in board " + (localBoard.getLocalBoard()+1));
                } else {
                    System.out.println("Player " + localBoard.getWinner() + " wins in board " + (localBoard.getLocalBoard()+1));
                }
            }
        }
    }
    private boolean checkWin() {
        int winsCount = 0;
        Win[] localBoardWins = board.getWins();
        for (Win localBoard: localBoardWins) {
            if (localBoard.isWin()) {
                winsCount++;
            }
        }

        for (int i = 0; i < 9; i=i+3) {
            if (localBoardWins[i].isWin() && localBoardWins[i + 1].isWin() && localBoardWins[i + 2].isWin()) {
                System.out.println("horizonal win in row "+ ((i/3)+1));
                return true;
            }
        }
        for (int j = 0; j < 3; j++) {
            if (localBoardWins[j].isWin() && localBoardWins[j + 3].isWin() && localBoardWins[j + 6].isWin()) {
                System.out.println("vertical win in column "+j);
                return true;
            }
        }
        if ((localBoardWins[0].isWin() && localBoardWins[4].isWin() && localBoardWins[8].isWin()) || (localBoardWins[2].isWin() && localBoardWins[4].isWin() && localBoardWins[6].isWin())) {
            System.out.println("diagonal win");
            return true;
        }

        return false;
    }
}
