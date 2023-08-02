public class Win {
    private final Board board;
    private boolean isWin = false;
    private String winner = "No winner";
    private String winType = "No win";

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
    public void setWinConditions(Boolean isWin) {
        this.isWin = isWin;
    }

    public void setWinConditions(Boolean isWin, String winType) {
        this.isWin = isWin;
        this.winType = winType;
    }

    public void setWinConditions(Boolean isWin, String winType, String winner) {
        this.winner = winner;
        this.isWin = isWin;
        this.winType = winType;
    }
}
