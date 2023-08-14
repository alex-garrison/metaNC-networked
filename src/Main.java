public class Main {
    public static void main(String[] args) {

        Board board = new Board();
        board.emptyBoard();

        try {
            GUI.startGUI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        GUI.frame.showBoard(board);

        GUI.frame.waitForNewGame();

        GUI.frame.setBottomLabel(GUI.frame.getMode());
    }
}
