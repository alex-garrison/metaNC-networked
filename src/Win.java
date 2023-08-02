public class Win {
    private final Board board;
    private boolean isWin = false;
    private String winner = "No winner";
    private String winType = "No win";
    private int[][] winTiles;

    public Win(Board newBoard) {
        board = newBoard;
    }

    public boolean isWin() {
        return isWin;
    }

    public String getWinner() {
        return winner;
    }

    public String getWinType() {
        return winType;
    }

    public int[][] getWinTiles() {
        return winTiles;
    }

    public void setWinTiles(int[][] winTiles) {
        this.winTiles = winTiles;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void setWin(Boolean isWin) {
        this.isWin = isWin;
    }

    public void setWinType(String winType) {
        this.winType = winType;
    }
}
