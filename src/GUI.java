import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

public class GUI extends JFrame {
    public static GUI frame;

    private JPanel mainPanel;
    private JPanel optionPanel;
    private JPanel[] boardPanels;
    private JButton newGameButton;
    private JComboBox<String> selectMode;
    private JLabel bottomLabel;
    private JButton[][][] cells;

    private int[] currentMove;

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

        optionPanel = createOptionPanel();

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(optionPanel, BorderLayout.SOUTH);
    }

    private JPanel createBoardPanel(int boardIndex) {
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3,3));
        boardPanel.setBackground(Color.WHITE);
        boardPanel.setBorder(new LineBorder(Color.BLACK, 2));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton cell = new JButton("X");
                cell.addActionListener(new CellClickListener(boardIndex, row, col));
                cell.setPreferredSize(new Dimension(50,50));
                cell.setBorder(new LineBorder(Color.BLACK,1));
                cell.setOpaque(false);
                cell.setContentAreaFilled(false);
                cell.setForeground(Color.BLACK);
                cell.setFont(new Font("monospaced", Font.PLAIN, 40));
                cell.addActionListener(new CellClickListener(boardIndex, row, col));
                boardPanel.add(cell);
                cells[boardIndex][row][col] = cell;
            }
        }
        return boardPanel;
    }

    private JPanel createOptionPanel() {
        JPanel newOptionPanel = new JPanel();
        newOptionPanel.setLayout(new BoxLayout(newOptionPanel, BoxLayout.X_AXIS));
        newOptionPanel.setBorder(new EmptyBorder(0,5,5,5));

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.PLAIN, 15));
        newGameButton.addActionListener(new NewGameClickListener());

        selectMode = new JComboBox<>(new String[]{"PvP", "PvAI", "AIvAI"});
        selectMode.setFont(new Font("Arial", Font.PLAIN, 15));
        selectMode.setMaximumSize(new Dimension(40, selectMode.getPreferredSize().height));
        selectMode.addActionListener(new SelectModeListener());

        bottomLabel = new JLabel("");
        bottomLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        bottomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        newOptionPanel.add(newGameButton);
        newOptionPanel.add(Box.createRigidArea(new Dimension(5,-1)));
        newOptionPanel.add(selectMode);
        newOptionPanel.add(Box.createHorizontalGlue());
        newOptionPanel.add(bottomLabel);
        newOptionPanel.add(Box.createHorizontalGlue());

        return newOptionPanel;
    }

    private class CellClickListener implements ActionListener {
        private final int boardIndex;
        private final int row;
        private final int col;

        public CellClickListener(int boardIndex, int row, int col) {
            this.boardIndex = boardIndex;
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton cell = cells[boardIndex][row][col];
            currentMove = new int[]{boardIndex, row, col};
            synchronized (cells) {
                cells.notify();
            }
        }
    }

    private class NewGameClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (newGameButton) {
                newGameButton.notify();
            }
        }
    }

    private class SelectModeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox selectMode = (JComboBox) e.getSource();
        }
    }

    public void waitForNewGame() {
        synchronized(newGameButton) {
            try {
                newGameButton.wait();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public int[] waitForMove() {
        synchronized(cells) {
            try {
                cells.wait();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return currentMove;
    }

    public void showBoard(Board board) {
        String[][][] boardArr = board.getBoard();
        for (int boardIndex = 0; boardIndex < boardArr.length; boardIndex++) {
            for (int row = 0; row < boardArr[boardIndex].length; row++) {
                for (int col = 0; col < boardArr[boardIndex][row].length; col++) {
                    cells[boardIndex][row][col].setText(boardArr[boardIndex][row][col]);
                }
            }
        }
    }

    public void setBoardColours(Board board) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                Color col;
                for (int i = 0; i < boardPanels.length; i++) {
                    if (board.getCorrectLocalBoard() == i && !board.isWon) {
                        col = Color.BLUE;
                    } else if (board.isWonBoard(i)) {
                        col = Color.RED;
                    } else {
                        col = Color.WHITE;
                    }
                    boardPanels[i].setBackground(col);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setBottomLabel(String text, boolean error) {
        Color color = Color.BLACK;
        if (error) color = Color.RED;
        bottomLabel.setForeground(color);
        bottomLabel.setText(text);
    }

    public void clearBottomLabel() {
        bottomLabel.setForeground(Color.BLACK);
        bottomLabel.setText("");
    }

    public String getMode() {
        return (String) selectMode.getSelectedItem();
    }

    public static void startGUI() throws InterruptedException, InvocationTargetException {
        System.setProperty( "apple.awt.application.appearance", "system" );

        SwingUtilities.invokeAndWait(() -> {
            frame = new GUI();
            frame.setVisible(true);
        });
    }
}
