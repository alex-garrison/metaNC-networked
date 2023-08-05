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
        int[] moveLocation;
        AiAgent ai = new AiAgent(board);
        board.emptyBoard();

        if (isAIVAI) {
            board.setStarter(ai.getStarter());
        } else {
            board.setStarter(PlayerInput.getStarter(scan));
        }

        while (true) {
            isWon = checkWin();
            if (isWon) {
                break;
            }

            if (isHuman) {
                moveLocation = PlayerInput.getMove(scan, board.whoseTurn());
            } else {
                try {
                    moveLocation = ai.getMove();
                } catch (GameException e) {
                    System.out.println(e.getMessage());
                    return;
                }
            }

            try {
                board.turn(board.whoseTurn(), moveLocation);
            } catch (GameException e) {
                System.out.println(e.getMessage());
                continue;
            }

            if (!isHuman && !isAIVAI) {
                System.out.println("Player " + board.invertPlayer(board.whoseTurn()) + " moved at " + moveLocation[0] + "/" + moveLocation[1]);
            }

            if (mode.equals("AI")) {
                isHuman = !isHuman;
            }

            System.out.println(board.toString(moveLocation));
        }

//         printWins();
        System.out.println(board);
    }

    public void setMode(String newMode) {
        mode = newMode;
    }

    private void printWins() {
        Win[] localBoardWins = board.getWins();
        for (Win localBoard : localBoardWins) {
            if (localBoard.isWin()) {
                if (localBoard.getWinType().equals("draw")) {
                    System.out.println("Draw in board " + (localBoard.getLocalBoard() + 1));
                } else {
                    System.out.println("Player " + localBoard.getWinner() + " wins in board " + (localBoard.getLocalBoard() + 1));
                }
            }
        }
    }

    private boolean checkWin() {
        Win[] localBoardWins = board.getWins();

        for (String player : new String[]{"X", "O"}) {
            for (int i = 0; i < 9; i = i + 3) {
                if (localBoardWins[i].getWinner().equals(player) && localBoardWins[i+1].getWinner().equals(player) && localBoardWins[i+2].getWinner().equals(player)) {
                    System.out.println("Player " + player + " horizonal win in row " + ((i / 3) + 1));
                    return true;
                }
            }
            for (int j = 0; j < 3; j++) {
                if (localBoardWins[j].getWinner().equals(player) && localBoardWins[j+3].getWinner().equals(player) && localBoardWins[j+6].getWinner().equals(player)) {
                    System.out.println("Player " + player + " vertical win in column " + (j+1));
                    return true;
                }
            }
            if (    (localBoardWins[0].getWinner().equals(player) && localBoardWins[4].getWinner().equals(player) && localBoardWins[8].getWinner().equals(player))
                    || (localBoardWins[2].getWinner().equals(player) && localBoardWins[4].getWinner().equals(player) && localBoardWins[6].getWinner().equals(player))) {
                System.out.println("Player " + player + " diagonal win");
                return true;
            }
        }
        if (board.getNumberOfValidMovesAI() < 1) {
            System.out.println("No more valid moves. Draw."); return true;
        }
        return false;

    }
}
