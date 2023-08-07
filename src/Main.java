import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        Board board = new Board();
        board.emptyBoard();

        for (int i = 0; i < 100; i++) {
            Game game = new Game(scan);

            game.setMode("AIVAI");

            try {
                game.play();
            } catch (GameException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
