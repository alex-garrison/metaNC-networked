package client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

class NetworkConfigDialog extends JDialog {
    private final Color BACKGROUND = ClientGUI.frame.BACKGROUND;
    private final Color OPTION_PANEL_BACKGROUND = ClientGUI.frame.OPTION_PANEL_BACKGROUND;

    private InetAddress DEFAULT_HOST;
    private final int DEFAULT_PORT = 8000;

    private final JTextField ipField;
    private final JTextField portField;
    private final JTextPane clientIDPane;
    private final JTextPane lobbyIDPane;
    private final JButton okButton;
    private final JButton cancelButton;

    private InetAddress serverIpAddress;
    private int serverPort;

    private InetAddress ipAddress;
    private int port;

    public NetworkConfigDialog(ClientGUI clientGUI) {
        super(clientGUI, "Enter IP and Port", true);
        setMinimumSize(new Dimension(250, 200));
        setResizable(false);

        try {
            DEFAULT_HOST = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Could not get localHost : " + e);
        }

        getServerIp();

        ipField = new JTextField(DEFAULT_HOST.getHostAddress(), 15);
        ipField.setFont(new Font("monospaced", Font.PLAIN, 13));
        ipField.setText(String.valueOf(serverIpAddress.getHostAddress()));
        ipField.setAlignmentX(Component.CENTER_ALIGNMENT);

        portField = new JTextField(String.valueOf(DEFAULT_PORT), 5);
        portField.setFont(new Font("monospaced", Font.PLAIN, 13));
        portField.setText(String.valueOf(serverPort));
        portField.setAlignmentX(Component.CENTER_ALIGNMENT);

        clientIDPane = new JTextPane();
        clientIDPane.setFont(new Font("monospaced", Font.PLAIN, 13));
        clientIDPane.setForeground(BACKGROUND);
        clientIDPane.setEditable(false);
        clientIDPane.setBackground(null);
        clientIDPane.setBorder(null);
        if (ClientGUI.frame.clientID == 0) {
            clientIDPane.setText("-");
        } else {
            clientIDPane.setText(String.valueOf(ClientGUI.frame.clientID));
        }

        lobbyIDPane = new JTextPane();
        lobbyIDPane.setFont(new Font("monospaced", Font.PLAIN, 13));
        lobbyIDPane.setForeground(BACKGROUND);
        lobbyIDPane.setEditable(false);
        lobbyIDPane.setBackground(null);
        lobbyIDPane.setBorder(null);
        if (ClientGUI.frame.lobbyID != 0) {
            lobbyIDPane.setText(String.valueOf(ClientGUI.frame.lobbyID));
        } else {
            lobbyIDPane.setText("-");
        }

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            try {
                ipAddress = InetAddress.getByName(ipField.getText());
                port = Integer.parseInt(portField.getText());
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (UnknownHostException ex) {
                JOptionPane.showMessageDialog(this, "Invalid IP address", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel clientIDPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel clientIDLabel = new JLabel("ClientID :");
        clientIDLabel.setForeground(BACKGROUND);
        clientIDPanel.add(clientIDLabel);
        clientIDPanel.add(clientIDPane);
        clientIDPanel.setBackground(OPTION_PANEL_BACKGROUND);

        JPanel lobbyIDPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lobbyIDLabel = new JLabel("LobbyID :");
        lobbyIDLabel.setForeground(BACKGROUND);
        lobbyIDPanel.add(lobbyIDLabel);
        lobbyIDPanel.add(lobbyIDPane);
        lobbyIDPanel.setBackground(OPTION_PANEL_BACKGROUND);

        JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel ipLabel = new JLabel("IP Address:");
        ipLabel.setForeground(BACKGROUND);
        ipPanel.add(ipLabel);
        ipPanel.add(ipField);
        ipPanel.setBackground(OPTION_PANEL_BACKGROUND);

        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(BACKGROUND);
        portPanel.add(portLabel);
        portPanel.add(portField);
        portPanel.setBackground(OPTION_PANEL_BACKGROUND);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setBackground(OPTION_PANEL_BACKGROUND);

        contentPane.add(clientIDPanel);
        contentPane.add(lobbyIDPanel);
        contentPane.add(Box.createRigidArea(new Dimension(-1, 5)));
        contentPane.add(ipPanel);
        contentPane.add(portPanel);
        contentPane.add(buttonPanel);

        contentPane.setBackground(OPTION_PANEL_BACKGROUND);

        getRootPane().setDefaultButton(okButton);
        setContentPane(contentPane);
        setLocationRelativeTo(ClientGUI.frame);
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    private void getServerIp() {
        String urlStr = "https://alex-garrison.github.io/server-info";

        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            StringBuilder content = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            reader.close();

            serverIpAddress = InetAddress.getByName(content.toString().split(":")[0].strip());
            serverPort = Integer.parseInt(content.toString().split(":")[1].strip());
        } catch (IOException e) {
            System.out.println("Error retrieving server IP");
        }
    }
}
