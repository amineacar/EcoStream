package ecostream;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class EcoStream {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Eco-Stream: Recycling Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);

            CardLayout card = new CardLayout();
            JPanel root = new JPanel(card);

            GamePanel gamePanel = new GamePanel();

            StartPanel startPanel = new StartPanel(
                () -> { 
                    card.show(root, "GAME");
                    gamePanel.requestFocusInWindow(); 
                },
                () -> System.exit(0) 
            );

            root.add(startPanel, "MENU");
            root.add(gamePanel, "GAME");

            frame.setContentPane(root);
            card.show(root, "MENU");

            frame.setVisible(true);
            startPanel.requestFocusInWindow();
        });
    }
}
