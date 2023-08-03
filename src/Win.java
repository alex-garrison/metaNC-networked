public class Win {
    private boolean isWin;
    private String winner;
    private String winType;

    public Win() {
        this.isWin = false;
        this.winner = "No winner";
        this.winType = "No win";
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
