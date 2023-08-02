import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        Board board = new Board();

        Game game = new Game(scan);

        game.setMode("AI");

        try {
            game.play();
        } catch (GameException | InterruptedException e) {
            System.out.println(e.getMessage());
        }




    }
}
