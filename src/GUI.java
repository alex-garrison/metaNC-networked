import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame {
    public static GUI frame;

    public GUI() {
        initGUI();
    }

    private void initGUI() {
        setTitle("Ultimate Noughts and Crosses");
        setMinimumSize(new Dimension(450, 450));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new GUI();
            frame.setVisible(true);
        });
    }
}
