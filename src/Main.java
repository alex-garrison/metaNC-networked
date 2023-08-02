import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws GameException {
        Scanner scan = new Scanner(System.in);

        Board board = new Board();
        System.out.println(board);

        board.setStarter("X");
        AiAgent ai = new AiAgent(board);
        int[] move = ai.getMove();
        System.out.println("AI moving at " + Arrays.toString(move));
        board.turn("X", move); board.turn = "X";

        move = ai.getMove();
        System.out.println("AI moving at " + Arrays.toString(move));
        board.turn("X", move); board.turn = "X";

        move = ai.getMove();
        System.out.println("AI moving at " + Arrays.toString(move));
        board.turn("X", move); board.turn = "X";

        System.out.println(board);

//
//        System.out.println(board);
//
//        AiAgent ai = new AiAgent(board);
//        System.out.println(Arrays.toString(ai.getMove()));

//        board.turn("X", new int[]{1,1}); board.turn = "X";
//        board.turn("X", new int[]{1,2}); board.turn = "X";
//        board.turn("X", new int[]{1,3}); board.turn = "X";
////
////        System.out.println(board);
//
//        System.out.println(board.isWin().isWin());
//
//        Game game = new Game(scan);
//
//        game.setMode("ai");
//
//        try {
//            game.play();
//        } catch (GameException e) {
//            System.out.println(e.getMessage());
//        }

    }
}
