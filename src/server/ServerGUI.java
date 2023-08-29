package server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;

public class ServerGUI extends JFrame {
    public static ServerGUI frame;

    private final Color TEXT = new Color(224, 225, 221);
    private final Color BACKGROUND = new Color(27, 38, 59);
    private final Color OUTPUT_BACKGROUND = new Color(141, 140, 150);

    private static JPanel serverOutputPanel;
    private static JTextArea serverOutput;
    private static JPanel optionPanel;
    private static JButton startServerButton;
    private static JButton stopServerButton;
    private static JButton serverButton;
    private static JLabel networkLabel;

    private ServerGUI() {
        initGUI();
    }

    private void initGUI() {
        setTitle("uNC - Server");
        setMinimumSize(new Dimension(500, 500));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().setBackground(BACKGROUND);

        optionPanel = new JPanel();
        optionPanel.setBorder(new EmptyBorder(10,20,0,25));
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.X_AXIS));
        optionPanel.setBackground(BACKGROUND);

        startServerButton = new JButton("Start");
        startServerButton.addActionListener(new StartServerListener());

        stopServerButton = new JButton("Stop");
        stopServerButton.addActionListener(new StopServerListener());

        serverButton = new JButton();
        serverButton.setFont(new Font("Arial", Font.PLAIN, 20));
        setServerButtonFunction(true);

        networkLabel = new JLabel();
        networkLabel.setFont(new Font("monospaced", Font.PLAIN, 20));
        networkLabel.setForeground(TEXT);

        optionPanel.add(serverButton);
        optionPanel.add(Box.createHorizontalGlue());
        optionPanel.add(networkLabel);

        serverOutputPanel = new JPanel();
        serverOutputPanel.setBorder(new EmptyBorder(0,5,0,5));
        serverOutputPanel.setBackground(BACKGROUND);

        serverOutput = new JTextArea();
        serverOutput.setFont(new Font("monospaced", Font.PLAIN, 15));
        serverOutput.setBackground(OUTPUT_BACKGROUND);
        serverOutput.setEditable(false);
        serverOutput.setBorder(new EmptyBorder(5,5,5,5));
        DefaultCaret caret = (DefaultCaret) serverOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(serverOutput);
        scrollPane.setBorder(new LineBorder(TEXT, 5));
        scrollPane.setBackground(OUTPUT_BACKGROUND);
        scrollPane.setPreferredSize(new Dimension(450, 400));
        serverOutputPanel.add(scrollPane);

        add(optionPanel);
        add(Box.createVerticalGlue());
        add(serverOutputPanel);
    }

    private class StartServerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ServerMain.serverMain.startServer();
            setServerButtonFunction(false);
        }
    }

    private class StopServerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ServerMain.serverMain.stopServer();
            setServerButtonFunction(true);
        }
    }

    public static void setServerButtonFunction(Boolean isStart) {
        JButton[] serverButtons = new JButton[]{startServerButton, stopServerButton};
        int buttonSelector = isStart ? 0 : 1;

        for (ActionListener listener: serverButton.getActionListeners()) {
            serverButton.removeActionListener(listener);
        }

        serverButton.setText(serverButtons[buttonSelector].getText());
        serverButton.addActionListener(serverButtons[buttonSelector].getActionListeners()[0]);
        serverButton.revalidate();
        serverButton.repaint();
    }

    public static void println(String text) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(() -> serverOutput.append(text + "\n"));
            } catch (InterruptedException | InvocationTargetException e) {}
        }
    }

    public static void clear() {
        serverOutput.setText("");
    }

    public static void setNetworkLabel(InetAddress host, int port) {
        networkLabel.setText(host.getHostAddress() + ":" + port);
    }

    public static void clearNetworkLabel() {
        networkLabel.setText("");
    }

    public static void startGUI() throws InterruptedException, InvocationTargetException {
        System.setProperty("apple.awt.application.appearance", "system");
        System.setProperty("apple.awt.application.name", "Ultimate Noughts and Crosses");

        SwingUtilities.invokeAndWait(() -> {
            frame = new ServerGUI();
            frame.setVisible(true);
        });
    }
}
