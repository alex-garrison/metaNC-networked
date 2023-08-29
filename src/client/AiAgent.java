package client;

import java.util.Random;
public class AiAgent {
    NetworkedBoard networkedBoard;
    Random rand;

    public AiAgent(NetworkedBoard newNetworkedBoard) {
        networkedBoard = newNetworkedBoard;
        rand = new Random();
    }

    public int[] getMove() throws GameException {
        return getRandomMove();
    }

    private int[] getRandomMove() throws GameException {
        int[][] validMoves = networkedBoard.getValidMovesAI();
        if (validMoves.length < 1) {
            throw new GameException("No more valid moves. Draw.");
        }
        return validMoves[rand.nextInt(validMoves.length)];
    }
}
