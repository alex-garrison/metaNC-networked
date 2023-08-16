class GameLoop implements Runnable {
    private final Board board;
    private String mode;

    public volatile boolean gameLoopExecuting;

    public GameLoop() {
        board = new Board();
        gameLoopExecuting = true;
    }

    public void run() {
        mode = GUI.frame.getMode();
        gameLoop();
    }

    private void gameLoop() {
        board.emptyBoard();
        board.setStarter("X");

        long MOVE_DELAY = 150;

        boolean isPVP = false;
        boolean isPVAI = false;
        boolean isAIVAI = false;

        switch (mode) {
            case "PvP" -> isPVP = true;
            case "PvAI" -> isPVAI = true;
            case "AIvAI" -> isAIVAI = true;
        }

        boolean errorOccured = false;
        int[] moveLocation = new int[3];
        AiAgent ai = new AiAgent(board);

        while (gameLoopExecuting) {
            if (board.isWin()) {
                if (board.winner.equals("Draw")) {
                    GUI.frame.setBottomLabel("Draw", false);
                } else {
                    GUI.frame.setBottomLabel("Player " + board.winner + " wins.", false);
                }
                break;
            }

            GUI.frame.updateBoard(board);
            GUI.frame.setBoardColours(board);



            if (isPVP) {
                moveLocation = GUI.frame.waitForMove(this);
            } else if (isPVAI) {
                if (board.whoseTurn().equals("X")) {
                    moveLocation = GUI.frame.waitForMove(this);
                } else {
                    try {
                        moveLocation = ai.getMove();
                        Thread.sleep(MOVE_DELAY);
                    } catch (InterruptedException e) {
                        break;
                    } catch (GameException e) {
                        System.out.println(e.getMessage());
                    }
                }
            } else if (isAIVAI) {
                try {
                    moveLocation = ai.getMove();
                    Thread.sleep(MOVE_DELAY);
                } catch (InterruptedException e) {
                    break;
                } catch (GameException e) {
                    System.out.println(e.getMessage());
                }

            }

            try {
                board.turn(board.whoseTurn(), moveLocation);
            } catch (GameException e) {
                GUI.frame.setBottomLabel(e.getMessage(), true);
                errorOccured = true;
            } catch (NullPointerException e) {
                continue;
            }

            if (!errorOccured) {
                GUI.frame.clearBottomLabel();
            } else {
                errorOccured = false;
            }
        }
        GUI.frame.updateBoard(board);
        GUI.frame.setBoardColours(board);
    }

    public void stopExecuting() {
        gameLoopExecuting = false;
    }
}
