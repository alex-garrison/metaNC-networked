import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class GUI extends JFrame {
    public static GUI frame;

    private JPanel mainPanel;
    private JPanel[] boardPanels;
    private JButton[][][] cells;

    public GUI() {
        initGUI();
    }

    private void initGUI() {
        setTitle("Ultimate Noughts and Crosses");
        setMinimumSize(new Dimension(450, 450));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3,3));

        boardPanels = new JPanel[9];
        cells = new JButton[9][3][3];

        for (int boardIndex = 0; boardIndex < 9; boardIndex++) {
            JPanel boardPanel = createBoardPanel(boardIndex);
            boardPanels[boardIndex] = boardPanel;
            mainPanel.add(boardPanel);
        }

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createBoardPanel(int boardIndex) {
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3,3));
        boardPanel.setBackground(Color.WHITE);
        boardPanel.setBorder(new LineBorder(Color.BLACK, 2));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton cell = new JButton();
                cell.setPreferredSize(new Dimension(50,50));
                cell.setBorder(new LineBorder(Color.BLACK,1));
                cell.setOpaque(false);
                cell.setContentAreaFilled(false);
                cell.setForeground(Color.BLACK);
                cell.setFont(new Font("monospaced", Font.PLAIN, 40));
                boardPanel.add(cell);
                cells[boardIndex][row][col] = cell;
            }
        }
        return boardPanel;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            frame = new GUI();
            frame.setVisible(true);
        });

    }
}
