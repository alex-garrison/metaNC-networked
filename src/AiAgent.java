//import java.util.Random;
//
//public class AiAgent {
//    Board board;
//    Random rand;
//
//    public AiAgent(Board newBoard) {
//        board = newBoard;
//        rand = new Random();
//    }
//
//    public int[] getMove() {
//        return getRandomMove();
//    }
//
//    public String getStarter() {
//        return new String[]{"X", "O"}[rand.nextInt(1)];
//    }
//
//    private int[] getRandomMove() {
//        int[] validMoves = board.getValidMoves();
//        return validMoves[rand.nextInt(validMoves.length)];
//    }
//}
