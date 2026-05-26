package toko;

import toko.view.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Gunakan Look and Feel default sistem
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback ke default Swing
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
