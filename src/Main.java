import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        Game game = new Game(scan);

        game.setMode("AIVAI");

        try {
            game.play();
        } catch (GameException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
