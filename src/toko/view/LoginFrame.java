package toko.view;

import toko.model.User;
import toko.util.FileHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L; // Fix #8

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;

    public LoginFrame() {
        setTitle("Login - Aplikasi Toko");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Header
        JLabel lblTitle = new JLabel("APLIKASI TOKO", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        txtUsername = new JTextField(18);
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        txtPassword = new JPasswordField(18);
        formPanel.add(txtPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // South
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setForeground(Color.RED);
        southPanel.add(lblStatus, BorderLayout.NORTH);

        btnLogin = new JButton("LOGIN");
        btnLogin.setPreferredSize(new Dimension(120, 32));
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 13));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(btnLogin);
        southPanel.add(btnPanel, BorderLayout.CENTER);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Events
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Username dan password tidak boleh kosong!");
            return;
        }

        User user = FileHelper.authenticate(username, password);
        if (user != null) {
            FileHelper.logLogin(user.getUsername(), user.getStatus());
            lblStatus.setText(" ");
            JOptionPane.showMessageDialog(this,
                    "Selamat datang, " + user.getUsername() + "!\nRole: " + user.getStatus().toUpperCase(),
                    "Login Berhasil", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new MainFrame(user).setVisible(true);
        } else {
            lblStatus.setText("Username atau password salah!");
            txtPassword.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
