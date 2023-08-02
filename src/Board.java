import java.util.Arrays;

public class Board {
    public String[][][] board;
    public String turn;
    private int[] lastMove;

    public Board() {
        board = new String[9][3][3];
        this.emptyBoard();
    }

    public String invertPlayer(String player) {
        if (player.equals("X")) {return "O";} else {return "X";}
    }

    public void setStarter(String starter) {
        turn = starter;
    }

    public void turn(String player, int[] location) throws GameException {

            if (player.equals(turn)) {
                int[] loc = resolveLocation(location);

                if (isValidMove(location)) {
                    board[loc[0]][loc[1]][loc[2]] = player;
                    lastMove = location;
                } else {
                    throw new GameException("Move not valid.");
                }

                turn = invertPlayer(turn);

            } else if (turn.isEmpty()) {
                throw new GameException("Starter player has not been set.");
            } else {
                throw new GameException("It is not player " + player + "'s turn.");
            }

    }

    public boolean isValidMove(int[] location) {
        int[] loc = new int[3];
        try {
            loc = resolveLocation(location);
        } catch (GameException e) {
            System.out.println(e.getMessage());
        }
        return board[loc[0]][loc[1]][loc[2]].equals(".");
    }

    public int getNumberOfValidMoves() {
        int numberOfValidMoves = 0;
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                if (isValidMove(new int[]{i, j})) {numberOfValidMoves++;}
            }
        }
        return numberOfValidMoves;
    }

    public int[][] getValidMoves() {
        int numberOfValidMoves = getNumberOfValidMoves();

        int[][] validMoves = new int[numberOfValidMoves][2];
        int counter = 0;

        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                if (isValidMove(new int[]{i, j})) {
                    validMoves[counter] = new int[]{i,j};
                    counter++;
                }
            }
        }
        return validMoves;
    }

    public int[] getLastMove() {
        return lastMove;
    }

    public int[] getLastMoveArr() {
        int[] lastMoveArr = new int[3];
        try {
            lastMoveArr = resolveLocation(lastMove);
        } catch (GameException e) {
            System.out.println(e.getMessage());
        }
        return lastMoveArr;
    }

    public Win isWin() {
        Win win = new Win(this);
        win.setWinConditions(false);
        for (String player : new String[]{"X","O"}) {
            for (int i = 0; i < board.length; i++) {
                System.out.println("Checking board " + i + " for player " + player);
                for (int j = 0; j < board[i].length; j++) {
                    System.out.println("    Checking row/col " + j);
                    if (board[i][j][0].equals(player) && board[i][j][1].equals(player) && board[i][j][2].equals(player)) {
                        win.setWinConditions(true, "win", player);
                        return win;
                    } else if (board[i][0][j].equals(player) && board[i][1][j].equals(player) && board[i][2][j].equals(player)){
                        win.setWinConditions(true, "win", player);
                        return win;
                    }
                }
                System.out.println("    Checking diagonals ");
                if ((board[i][0][0].equals(player) && board[i][1][1].equals(player) && board[i][2][2].equals(player))) {
                    win.setWinConditions(true, "win", player);
                    return win;
                } else if ((board[i][0][2].equals(player) && board[i][1][1].equals(player) && board[i][2][0].equals(player))) {
                    win.setWinConditions(true, "win", player);
                    return win;
                }
            }
        }
        if (getNumberOfValidMoves() == 0) {
            win.setWinConditions(true, "draw");
            return win;
        }
        return win;
    }

    public void emptyBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = new String[] {".", ".", "."};
            }
        }
        lastMove = new int[]{-1,-1};
    }

    public String whoseTurn() {
        return turn;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        int rowCounter = 1;
        int colCounter = 1;

        for (String[][] localBoards : board) {
            for (String[] rows : localBoards) {
                for (String cols: rows) {
                    output.append(cols).append(" ");
                    if (((colCounter % 3) == 0) && (colCounter != 9)) {
                        output.append("| ");
                    }
                    colCounter++;
                }
            }
            output.append("\n");

            if (((rowCounter % 3) == 0) && (rowCounter != 9)) {
                output.append("---------------------\n");
            }
            rowCounter++;

            colCounter = 1;

        }
        return output.toString();
    }

    public int[] resolveLocation(int[] location) throws GameException {
        int row;
        if (location[1] > 0 && location[1] <= 3) {
            row = 0;
        } else if (location[1] > 3 && location[1] <= 6) {
            row = 1;
        } else if (location[1] > 6 && location[1] <= 9) {
            row = 2;
        } else {
            throw new GameException("Location not in range.");
        }
        int col = (location[1]-1) % 3;
        return new int[] {location[0]-1, row, col};
    }
}

