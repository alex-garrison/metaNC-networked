package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TutorialDialog extends JDialog {
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JPanel buttonPanel;
    private final int maxPages = 4;
    private int currentIndex = 1;

    public TutorialDialog(ClientGUI clientGUI) {
        super(clientGUI,"Tutorial", true);
        setMinimumSize(new Dimension(475, 425));
        setResizable(false);

        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        for (int i = 1; i <= maxPages; i++) {
            JPanel textPanel = createTutorialPanel(i);

            cardPanel.add(textPanel);
        }

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(new EmptyBorder(0,0,10,0));

        JButton backButton = new JButton("<");
        backButton.setPreferredSize(new Dimension(40,40));
        backButton.addActionListener(e -> showPreviousCard());

        JButton forwardButton = new JButton(">");
        forwardButton.setPreferredSize(new Dimension(40,40));
        forwardButton.addActionListener(e -> showNextCard());

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(backButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(50, 0)));
        buttonPanel.add(forwardButton);
        buttonPanel.add(Box.createHorizontalGlue());


        add(cardPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showPreviousCard() {
        if (currentIndex > 1) {
            currentIndex--;
            cardLayout.previous(cardPanel);
        }
    }

    private void showNextCard() {
        if (currentIndex < maxPages) {
            currentIndex++;
            cardLayout.next(cardPanel);
        }
    }

    private JPanel createTutorialPanel(int tutorialPage) {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setHighlighter(null);
        editorPane.setBorder(new EmptyBorder(10,10,10,10));
        editorPane.setFont(new Font("monospaced", Font.PLAIN, 14));
        editorPane.setContentType("text/html");

        String fileName = "src/client/tutorialPages/page" + tutorialPage + ".html";

        try {
            File file = new File(fileName);
            String fileURL = file.toURI().toURL().toString();
            editorPane.setPage(fileURL);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading HTML file", "Error", JOptionPane.ERROR_MESSAGE);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(editorPane, BorderLayout.CENTER);
        return panel;
    }
}
