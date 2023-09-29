package client;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

public class ClientGUI extends JFrame {
    public static ClientGUI frame;
    private static NetworkConfigDialog networkConfigDialog;
    private static TutorialDialog tutorialDialog;

    private boolean isNetworked;
    private boolean waitingForNewGame;

    public final Color BACKGROUND = new Color(224, 225, 221);
//    public final Color LINE = new Color(13, 27, 42);
//    public final Color OPTION_PANEL_BACKGROUND = new Color(27, 38, 59);
    public final Color BOARD_INDICATOR = new Color(65, 90, 119);
    public final Color WON_BOARD = new Color(119, 141, 169);
    public final Color ERROR = new Color(230, 57, 70);
    public final Color WIN = new Color(106, 153, 78);

    private JPanel mainPanel;
    private JPanel bottomPanel;
    private JPanel topPanel;
    private JPanel[] boardPanels;
    private JButton newGameButton;
    private JComboBox<String> selectMode;
    private JLabel bottomLabel;
    private JButton[][][] cells;

    private JButton networkButton;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton networkConfigButton;
    private JLabel networkLabel;
    private JLabel playerLabel;
    private JButton tutorialButton;

    private int[] currentMove;
    int clientID;
    int lobbyID;

    public ClientGUI() {
        isNetworked = false;
        waitingForNewGame = false;
        initGUI();
    }

    private void initGUI() {
        FlatMacDarkLaf.setup();

        setTitle("Ultimate Noughts and Crosses");
        setMinimumSize(new Dimension(450, 500));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3,3, 3, 3));
        mainPanel.setBackground(Color.WHITE);

        boardPanels = new JPanel[9];
        cells = new JButton[9][3][3];

        for (int boardIndex = 0; boardIndex < 9; boardIndex++) {
            JPanel boardPanel = createBoardPanel(boardIndex);
            boardPanels[boardIndex] = boardPanel;
            mainPanel.add(boardPanel);
        }

        bottomPanel = createBottomPanel();
        topPanel = createTopPanel();

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);
    }

    private JPanel createBoardPanel(int boardIndex) {
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3,3, -1, -1));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton cell = new JButton("");
                cell.setPreferredSize(new Dimension(50,50));
                cell.setBorder(new LineBorder(Color.WHITE,1));
                cell.setOpaque(false);
                cell.setContentAreaFilled(false);
                cell.setFont(new Font("monospaced", Font.PLAIN, 40));
                cell.addActionListener(new CellClickListener(boardIndex, row, col));
                boardPanel.add(cell);
                cells[boardIndex][row][col] = cell;
            }
        }
        return boardPanel;
    }

    private void setWinPanel(int boardIndex, Board board) {
        boardPanels[boardIndex].removeAll();
        boardPanels[boardIndex].setLayout(new BorderLayout());

        JLabel label = new JLabel(board.getLocalBoardWins()[boardIndex].getWinner());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("monospaced", Font.PLAIN, 80));

        boardPanels[boardIndex].add(label, BorderLayout.CENTER);

        boardPanels[boardIndex].revalidate();
        boardPanels[boardIndex].repaint();
    }

    private JPanel createBottomPanel() {
        JPanel gameOptionPanel = new JPanel();
        gameOptionPanel.setLayout(new BoxLayout(gameOptionPanel, BoxLayout.X_AXIS));
        gameOptionPanel.setPreferredSize(new Dimension((int) gameOptionPanel.getPreferredSize().getWidth(), 40));
        gameOptionPanel.setBorder(new EmptyBorder(3,5,3,5));

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font(newGameButton.getFont().getFontName(), Font.PLAIN, 15));
        newGameButton.addActionListener(new NewGameClickListener());

        selectMode = new JComboBox<>(new String[]{"Set mode", "-", "PvP", "PvAI", "AIvAI"});
        selectMode.setFont(new Font(selectMode.getFont().getFontName(), Font.PLAIN, 15));
        selectMode.setMaximumSize(new Dimension(40, selectMode.getPreferredSize().height));

        bottomLabel = new JLabel("");
        bottomLabel.setFont(new Font(bottomLabel.getFont().getFontName(), Font.PLAIN, 20));
        bottomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon tutorialIcon = new ImageIcon("/Users/alex/Programming/NEA/Actual/metaNC-networked/src/client/icons/tutorialIcon.png");
        tutorialIcon = new ImageIcon(tutorialIcon.getImage().getScaledInstance(20,20, java.awt.Image.SCALE_SMOOTH));
        tutorialButton = new JButton(tutorialIcon);
        tutorialButton.addActionListener(new TutorialListener());

        gameOptionPanel.add(newGameButton);
        gameOptionPanel.add(Box.createRigidArea(new Dimension(5,-1)));
        gameOptionPanel.add(selectMode);
        gameOptionPanel.add(Box.createHorizontalGlue());
        gameOptionPanel.add(bottomLabel);
        gameOptionPanel.add(Box.createHorizontalGlue());
        gameOptionPanel.add(tutorialButton);

        return gameOptionPanel;
    }

    private JPanel createTopPanel() {
        JPanel networkOptionPanel = new JPanel();
        networkOptionPanel.setLayout(new BoxLayout(networkOptionPanel, BoxLayout.X_AXIS));
        networkOptionPanel.setBorder(new EmptyBorder(3,5,3,5));
        networkOptionPanel.setPreferredSize(new Dimension((int) networkOptionPanel.getPreferredSize().getWidth(), 40));

        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ConnectClickListener());
        connectButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(new DisconnectClickListener());
        disconnectButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        ImageIcon configIcon = new ImageIcon("/Users/alex/Programming/NEA/Actual/metaNC-networked/src/client/icons/configIcon.png");
        configIcon = new ImageIcon(configIcon.getImage().getScaledInstance(20,20, java.awt.Image.SCALE_SMOOTH));
        networkConfigButton = new JButton(configIcon);
        networkConfigButton.addActionListener(new NetworkConfigListener());

        networkLabel = new JLabel("");
        networkLabel.setFont(new Font(networkLabel.getFont().getFontName(), Font.PLAIN, 20));
        networkLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        playerLabel = new JLabel("");
        playerLabel.setFont(new Font(playerLabel.getFont().getFontName(), Font.PLAIN, 35));
        playerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        networkButton = new JButton();
        networkButton.setPreferredSize(new Dimension(110, networkButton.getPreferredSize().height));
        setNetworkButtonFunction(true);

        networkOptionPanel.add(networkButton);
        networkOptionPanel.add(networkConfigButton);
        networkOptionPanel.add(Box.createRigidArea(new Dimension(5,-1)));
        networkOptionPanel.add(Box.createHorizontalGlue());
        networkOptionPanel.add(networkLabel);
        networkOptionPanel.add(Box.createHorizontalGlue());
        networkOptionPanel.add(playerLabel);
        networkOptionPanel.add(Box.createRigidArea(new Dimension(20,-1)));

        return networkOptionPanel;
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
            currentMove = new int[]{boardIndex, row, col};

            if (isNetworked) {
                if (ClientMain.client != null) {
                    ClientMain.client.turn(currentMove);
                }
            } else {
                synchronized (cells) {
                    cells.notify();
                }
            }
        }
    }

    private class NewGameClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isNetworked) {
                if (ClientMain.client != null) {
                    if (ClientMain.client.isConnected() && getMode().equals("PvP - online")) {
                        ClientMain.client.sendNewGame();
                    }
                }
            } else {
                if (waitingForNewGame) {
                    synchronized (newGameButton) {
                        newGameButton.notify();
                    }
                } else {
                    ClientMain.restartGameloop(false);
                }
            }
        }
    }

    private class ConnectClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!ClientMain.isHostSet()) {
                if (networkConfigDialog == null) {
                    networkConfigDialog = new NetworkConfigDialog(frame);
                }

                networkConfigDialog.setVisible(true);
                networkConfigDialog.setLocationRelativeTo(frame);

                if (networkConfigDialog.getIpAddress() != null && networkConfigDialog.getPort() != 0) {
                    ClientMain.setHostAndPort(networkConfigDialog.getIpAddress(), networkConfigDialog.getPort());
                } else {
                    return;
                }
            }

            ClientMain.startClient();
            setNetworkButtonFunction(false);
        }
    }

    private class DisconnectClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (ClientMain.client != null) {
                ClientMain.client.disconnect();
                setNetworkButtonFunction(true);
            }
        }
    }

    private class NetworkConfigListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (networkConfigDialog == null) {
                networkConfigDialog = new NetworkConfigDialog(frame);
            }

            networkConfigDialog.setVisible(true);
            networkConfigDialog.setLocationRelativeTo(frame);

            if (networkConfigDialog.getIpAddress() != null && networkConfigDialog.getPort() != 0) {
                ClientMain.setHostAndPort(networkConfigDialog.getIpAddress(), networkConfigDialog.getPort());
            }
        }
    }

    private class TutorialListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (tutorialDialog == null) {
                tutorialDialog = new TutorialDialog(frame);
            }

            tutorialDialog.setVisible(true);
            tutorialDialog.setLocationRelativeTo(frame);
        }
    }

    public void waitForNewGame() throws InterruptedException {
        if (!isNetworked) {
            waitingForNewGame = true;
            synchronized(newGameButton) {
                newGameButton.wait();
            }
            waitingForNewGame = false;
        }
    }

    public void waitForModeSelect() throws InterruptedException {
        if (!isNetworked) {
            while ((!(getMode().equals("PvP") || getMode().equals("PvAI") || getMode().equals("AIvAI"))) && !Thread.currentThread().isInterrupted()) {

                Thread.sleep(100);

            }
        }
    }

    public int[] waitForMove() throws InterruptedException {
        if (!isNetworked) {
            synchronized(cells) {
                cells.wait();
            }
            return currentMove;
        } else {
            return null;
        }
    }

    public void updateBoard(Board board) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                int[] lastMove = board.getLastMove();
                String[][][] boardArr = board.getBoard();
                for (int boardIndex = 0; boardIndex < boardArr.length; boardIndex++) {
                    for (int row = 0; row < boardArr[boardIndex].length; row++) {
                        for (int col = 0; col < boardArr[boardIndex][row].length; col++) {
                            cells[boardIndex][row][col].setText(boardArr[boardIndex][row][col]);
                            if ((boardIndex == lastMove[0]) && (row == lastMove[1]) && (col == lastMove[2])) {
                                cells[boardIndex][row][col].setForeground(ERROR);
                            } else {
                                cells[boardIndex][row][col].setForeground(Color.WHITE);
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setBoardColours(Board board, String clientPlayer) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                Color defaultCol = topPanel.getBackground();
                Color col;
                for (int i = 0; i < boardPanels.length; i++) {
                    col = defaultCol;
                    if (board.getCorrectLocalBoard() == i && !board.isWon && board.whoseTurn().equals(clientPlayer)) {
                        col = BOARD_INDICATOR;
                    } else if (board.isWonBoard(i)) {
                        if (boardPanels[i].getComponent(0).getFont().getSize() != 80) {
                            setWinPanel(i, board);
                        }
                        col = WON_BOARD;
                    }
                    boardPanels[i].setBackground(col);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setBoardColours(Board board) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                Color defaultCol = topPanel.getBackground();
                Color col;
                for (int i = 0; i < boardPanels.length; i++) {
                    col = defaultCol;
                    if (board.getCorrectLocalBoard() == i && !board.isWon) {
                        col = BOARD_INDICATOR;
                    } else if (board.isWonBoard(i)) {
                        if (boardPanels[i].getComponent(0).getFont().getSize() != 80) {
                            setWinPanel(i, board);
                        }
                        col = WON_BOARD;
                    }
                    boardPanels[i].setBackground(col);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setNetworkButtonFunction(Boolean isConnect) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setNetworkButtonFunction(isConnect));
        } else {
            if (ClientMain.client != null) {
                if (isConnect == ClientMain.client.isConnected()) {
                    return;
                }
            }
            JButton[] networkButtons = new JButton[]{connectButton, disconnectButton};
            int buttonSelector = isConnect ? 0 : 1;

            for (ActionListener listener: networkButton.getActionListeners()) {
                networkButton.removeActionListener(listener);
            }
            networkButton.setText(networkButtons[buttonSelector].getText());
            networkButton.addActionListener(networkButtons[buttonSelector].getActionListeners()[0]);
            networkButton.revalidate();
            networkButton.repaint();
        }
    }

    public void setBottomLabel(String text, boolean error, boolean win) {
        Color color = BACKGROUND;
        if (error) color = ERROR;
        else if (win) color = WIN;
        bottomLabel.setForeground(color);
        bottomLabel.setText(text);
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setLobbyID(int lobbyID) {
        this.lobbyID = lobbyID;
    }

    public void setNetworkLabel(String text, boolean error) {
        Color color = BACKGROUND;
        if (error) color = ERROR;
        networkLabel.setForeground(color);
        networkLabel.setText(text);
    }

    public void setPlayerLabel(String player, boolean isTurn) {
        Color color = BACKGROUND;
        if (isTurn) color = ERROR;
        playerLabel.setForeground(color);
        playerLabel.setText(player);
    }

    public void setNetworked(boolean networked) {
        isNetworked = networked;
    }

    public void resetBoardPanels() {
        SwingUtilities.invokeLater(() -> {
            mainPanel.removeAll();
            for (int boardIndex = 0; boardIndex < 9; boardIndex++) {
                JPanel boardPanel = createBoardPanel(boardIndex);
                boardPanels[boardIndex] = boardPanel;
                mainPanel.add(boardPanel);
            }
            mainPanel.revalidate();
            mainPanel.repaint();
        });

    }

    public void clearBottomLabel() {
        bottomLabel.setForeground(BACKGROUND);
        bottomLabel.setText("");
    }

    public void clearNetworkLabel() {
        networkLabel.setForeground(BACKGROUND);
        networkLabel.setText("");
    }

    public void clearPlayerLabel() {
        playerLabel.setForeground(BACKGROUND);
        playerLabel.setText("");
    }

    public String getMode() {
        return (String) selectMode.getSelectedItem();
    }

    public void setNetworkMode(boolean isConnected) {
        if (isConnected) {
            selectMode.setModel(new DefaultComboBoxModel<>(new String[]{"PvP - online"}));
        } else {
            selectMode.setModel(new DefaultComboBoxModel<>(new String[]{"Set mode", "-", "PvP", "PvAI", "AIvAI"}));
        }

    }

    public String getPlayerLabel() {
        return playerLabel.getText();
    }

    public static void startGUI() throws InterruptedException, InvocationTargetException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac os") || os.contains("macos")) {
            System.setProperty("apple.awt.application.appearance", "system");
            System.setProperty("apple.awt.application.name", "Ultimate Noughts and Crosses");
        }


        SwingUtilities.invokeAndWait(() -> {
            frame = new ClientGUI();
            frame.setVisible(true);
        });
    }
}