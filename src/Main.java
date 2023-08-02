import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws GameException {
        Scanner scan = new Scanner(System.in);

        Board board = new Board();

//        System.out.println(board.toString());

        System.out.println(Arrays.toString(board.resolveLocation(PlayerInput.getMove(scan, "X"))));

//        int[][] validMoves = board.getValidMoves();
//        for (int i = 0; i < validMoves.length; i++) {
//            System.out.println(Arrays.toString(validMoves[i]));
//        }


//        for (int i = 0; i < board.board.length; i++) {
//            for (int j = 0; j < board.board[i].length; j++) {
//                System.out.println(Arrays.toString(board.board[i][j]));
//            }
//            System.out.println("---------");
//        }

//        Game game = new Game(scan);

//        game.setMode(PlayerInput.getMode(scan));
//
//        try {
//            game.play();
//        } catch (GameException e) {
//            System.out.println(e.getMessage());
//        }

    }
}
