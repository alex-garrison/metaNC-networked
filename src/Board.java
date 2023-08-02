import java.util.Arrays;

public class Board {
    private String[][] board;
    private String turn;
    private int lastMove;

    public Board() {
        board = new String[3][3];
        this.emptyBoard();
    }

    public String invertPlayer(String player) {
        if (player.equals("X")) {return "O";} else {return "X";}
    }

    public void setStarter(String starter) {
        turn = starter;
    }

    public void turn(String player, int location) throws GameException {
        if (location < 1 || location > 9) {
            throw new GameException("Location not in range");
        } else {
            if (player.equals(turn)) {
                int[] loc = resolveLocation(location);


                if (isValidMove(location)) {
                    board[loc[0]][loc[1]] = player;
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
    }

    public boolean isValidMove(int location) {
        int[] loc = new int[0];
        try {
            loc = resolveLocation(location);
        } catch (GameException e) {
            System.out.println(e.getMessage());
        }
        return board[loc[0]][loc[1]].equals(".");
    }

    public int getNumberOfValidMoves() {
        int numberOfValidMoves = 0;
        for (int i = 1; i < 10; i++) {
            if (isValidMove(i)) {numberOfValidMoves++;}
        }
        return numberOfValidMoves;
    }

    public int[] getValidMoves() {
        int numberOfValidMoves = getNumberOfValidMoves();

        int[] validMoves = new int[numberOfValidMoves];
        int counter = 0;
        for (int i = 1; i < 10; i++) {
            if (isValidMove(i)) {validMoves[counter] = i; counter++;}
        }

        return validMoves;
    }

    public int getLastMove() {
        return lastMove;
    }

    public int[] getLastMoveArr() {
        int[] lastMoveArr = new int[2];
        try {
            lastMoveArr = resolveLocation(lastMove);
        } catch (GameException e) {
            System.out.println(e.getMessage());
        }
        return lastMoveArr;
    }

    public Win isWin() {
        Win win = new Win(this);
        win.setWin(false);
        for (String player : new String[]{"X","O"}) {
            for (int i = 0; i < board.length; i++) {
                if (board[i][0].equals(player) && board[i][1].equals(player) && board[i][2].equals(player)) {
                    win.setWin(true);
                    win.setWinType("win");
                    win.setWinTiles(new int[][] {{i, 0}, {i, 1}, {i, 2}});
                    win.setWinner(player);
                    return win;
                } else if (board[0][i].equals(player) && board[1][i].equals(player) && board[2][i].equals(player)){
                    win.setWin(true);
                    win.setWinType("win");
                    win.setWinTiles(new int[][] {{0, i}, {1, i}, {2, i}});
                    win.setWinner(player);
                    return win;
                }
            }
            if ((board[0][0].equals(player) && board[1][1].equals(player) && board[2][2].equals(player))) {
                win.setWin(true);
                win.setWinType("win");
                win.setWinTiles(new int[][] {{0, 0}, {1, 1}, {2, 2}});
                win.setWinner(player);
                return win;
            } else if ((board[0][2].equals(player) && board[1][1].equals(player) && board[2][0].equals(player))) {
                win.setWin(true);
                win.setWinType("win");
                win.setWinTiles(new int[][] {{0, 2}, {1, 1}, {2, 0}});
                win.setWinner(player);
                return win;
            }
        }
        if (getNumberOfValidMoves() == 0) {
            win.setWin(true);
            win.setWinType("draw");
            return win;
        }
        return win;
    }

    public void emptyBoard() {
        for (int i = 0; i < board.length; i++) {
            board[i] = new String[] {".", ".", "."};
        }
        lastMove = -1;
    }

    public String whoseTurn() {
        return turn;
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        for (String[] strings : board) {
            for (String string : strings) {
                output.append(string).append(" ");
            }
            output.append("\n");
        }
        return output.toString();
    }

    public String toStringColour(int[][] locations, String colour) {
        StringBuilder output = new StringBuilder();
        int[] locArr;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                locArr = new int[]{i, j};
                for (int[] location : locations) {
                    if (Arrays.equals(location, locArr)) {
                        output.append(colour);
                    }
                }
                output.append(board[i][j]).append(ConsoleColours.RESET).append(" ");
            }
            output.append("\n");
        }
        return output.toString();
    }

    private int[] resolveLocation(int location) throws GameException {
        int row;
        if (location > 0 && location <= 3) {
            row = 0;
        } else if (location > 3 && location <= 6) {
            row = 1;
        } else if (location > 6 && location <= 9) {
            row = 2;
        } else {
            throw new GameException("Location not in range.");
        }
        int col = (location-1) % 3;
        return new int[] {row, col};
    }
}

