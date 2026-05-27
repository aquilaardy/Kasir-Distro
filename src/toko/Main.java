package toko;

import javax.swing.*;
import toko.view.LoginFrame;

public class Main {
    public static void main(String[] args) {
        // Gunakan Look and Feel default sistem
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            // Fallback ke default Swing
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
