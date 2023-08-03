import java.util.Scanner;

public class PlayerInput {
    public static String getStarter(Scanner scan) {
        boolean starterSelected = false;
        String starter = "";
        while (!starterSelected) {
            System.out.print("Enter starting player : ");
            starter = scan.next().toUpperCase();
            if (!(starter.equals("X") || starter.equals("O"))) {
                System.out.println("Please enter either X or O");
            } else {
                starterSelected = true;
            }
        }
        return starter;
    }

    public static String getMode(Scanner scan) {
        boolean modeSelected = false;
        String mode = "";
        while (!modeSelected) {
            System.out.print("Enter mode (PvP / AI / AIvAI): ");
            mode = scan.next().toUpperCase();
            if (!(mode.equals("PVP") || mode.equals("AI") || mode.equals("AIVAI"))) {
                System.out.println("Please enter either PvP or AI or AIvAI");
            } else {
                modeSelected = true;
            }
        }
        return mode;
    }

    public static int[] getMove(Scanner scan, String player) {
        int[] moveLocation = new int[2];

        outerloop: while (true) {
            while (true) {
                try {
                    System.out.print("Player " + player + " enter move : ");
                    String moveLocationString = scan.next();
                    String[] moveLocationChars = moveLocationString.split("/");
                    for (int i = 0; i < moveLocation.length; i++) {
                        moveLocation[i] = Integer.parseInt(moveLocationChars[i]);
                    }
                    break;
                } catch (Exception e)  {
                    System.out.println("Move must be in the format (1-9)/(1-9) ");
                }
            }
            for (int moveLoc: moveLocation) {
                if (moveLoc < 1 || moveLoc > 9) {
                    System.out.println("Move must be between 1 and 9");
                    continue outerloop;
                }
            }
            break;

        }

        return moveLocation;
    }
}
