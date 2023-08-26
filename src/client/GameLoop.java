//package client;
//
//class GameLoop implements Runnable {
//    private String mode;
//
//    public volatile boolean gameLoopExecuting;
//
//    public GameLoop() {
//        gameLoopExecuting = true;
//    }
//
//    public void run() {
//        mode = GUI.frame.getMode();
//
//        gameLoop();
//    }
//
//    private void gameLoop() {
//        Board board = new Board();
//        board.emptyBoard();
//        board.setStarter("X");
//
//        long AI_MOVE_DELAY = 75;
//
//        boolean isPVP = false;
//        boolean isPVAI = false;
//        boolean isAIVAI = false;
//
//        switch (mode) {
//            case "PvP" -> isPVP = true;
//            case "PvAI" -> isPVAI = true;
//            case "AIvAI" -> isAIVAI = true;
//        }
//
//        boolean errorOccurred = false;
//        int[] moveLocation = new int[3];
//        AiAgent ai = new AiAgent(board);
//
//        while (gameLoopExecuting) {
//            if (board.isWin()) {
//                if (board.winner.equals("Draw")) {
//                    GUI.frame.setBottomLabel("Draw", false);
//                } else {
//                    GUI.frame.setBottomLabel("Player " + board.winner + " wins.", false);
//                }
//                GUI.frame.updateBoard(board);
//                GUI.frame.setBoardColours(board);
//
//                break;
//            }
//
//            try {
//                GUI.frame.updateBoard(board);
//                GUI.frame.setBoardColours(board);
//            } catch (RuntimeException e) {
//                gameLoopExecuting = false; continue;
//            }
//
//            if (isPVP) {
//                moveLocation = GUI.frame.waitForMove(this);
//            } else if (isPVAI) {
//                if (board.whoseTurn().equals("X")) {
//                    moveLocation = GUI.frame.waitForMove(this);
//                } else {
//                    try {
//                        moveLocation = ai.getMove();
//                        Thread.sleep(AI_MOVE_DELAY);
//                    } catch (InterruptedException e) {
//                        break;
//                    } catch (GameException e) {
//                        System.out.println(e.getMessage());
//                    }
//                }
//            } else if (isAIVAI) {
//                try {
//                    moveLocation = ai.getMove();
//                    Thread.sleep(AI_MOVE_DELAY);
//                } catch (InterruptedException e) {
//                    break;
//                } catch (GameException e) {
//                    System.out.println(e.getMessage());
//                }
//
//            }
//
//            try {
//                board.turn(board.whoseTurn(), moveLocation);
//            } catch (GameException e) {
//                GUI.frame.setBottomLabel(e.getMessage(), true);
//                errorOccurred = true;
//            } catch (NullPointerException e) {
//                continue;
//            }
//
//            if (!errorOccurred) {
//                GUI.frame.clearBottomLabel();
//            } else {
//                errorOccurred = false;
//            }
//        }
//    }
//}
