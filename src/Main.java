import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        Game game = new Game(scan);

//        Board board = new Board();
//        board.emptyBoard();
//        board.setStarter("X");
//        board.turn("X", new int[]{1,1}); board.turn = "X";
//        board.turn("X", new int[]{1,4}); board.turn = "X";
//        board.turn("X", new int[]{1,7}); board.turn = "X";
//
//        board.turn("X", new int[]{2,4}); board.turn = "X";
//        board.turn("X", new int[]{2,5}); board.turn = "X";
//        board.turn("X", new int[]{2,6}); board.turn = "X";
//
//        board.turn("X", new int[]{3,3}); board.turn = "X";
//        board.turn("X", new int[]{3,4}); board.turn = "X";
//        board.turn("X", new int[]{3,7}); board.turn = "X";
//
//        System.out.println(board);
//
//        Win[] localBoardWins = board.getWins();
//
//        for (int i = 0; i < 9; i=i+3) {
//            if (localBoardWins[i].isWin() && localBoardWins[i+1].isWin() && localBoardWins[i+2].isWin() && true) {
//                System.out.println("horizonal win in row "+ (i/3));
//            }
//
//        }
//        for (int j = 0; j < 3; j++) {
//            if (localBoardWins[j].isWin() && localBoardWins[j+3].isWin() && localBoardWins[j+6].isWin() && true) {
//                System.out.println("vertical win in column "+j);
//            }
//        }
//

        game.setMode("AI");

        try {
            game.play();
        } catch (GameException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
