package toko.view;

import java.awt.*;
import javax.swing.*;
import toko.model.User;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L; // Fix #8

    private User currentUser;
    private JTabbedPane tabbedPane;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Aplikasi Toko - [" + user.getUsername() + " | " + user.getStatus().toUpperCase() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menuApp = new JMenu("Aplikasi");
        JMenuItem miLogout = new JMenuItem("Logout");
        JMenuItem miKeluar  = new JMenuItem("Keluar");
        miLogout.addActionListener(e -> doLogout());
        miKeluar.addActionListener(e -> System.exit(0));
        menuApp.add(miLogout);
        menuApp.addSeparator();
        menuApp.add(miKeluar);
        menuBar.add(menuApp);

        // Info menu
        JMenu menuInfo = new JMenu("Info");
        JMenuItem miAbout = new JMenuItem("Tentang Aplikasi");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Aplikasi Toko v1.0\nDibuat dengan Java Swing\n\nFitur:\n- Login Multi-Role\n- Stok Barang\n- Kasir\n- Laporan",
                "Tentang", JOptionPane.INFORMATION_MESSAGE));
        menuInfo.add(miAbout);
        menuBar.add(menuInfo);
        setJMenuBar(menuBar);

        // Tabs sesuai role
        tabbedPane = new JTabbedPane();
        String role = currentUser.getStatus().toLowerCase();

        // Kasir: semua role bisa akses kasir
        // Admin: semua
        // Manajer: stok, kasir, laporan

        if (role.equals("admin")) {
            tabbedPane.addTab("Stok Barang", new StokBarangPanel());
            tabbedPane.addTab("Kasir",       new KasirPanel(currentUser));
            tabbedPane.addTab("Laporan",     new LaporanPanel());
            tabbedPane.addTab("Manajemen User", new UserManagementPanel());
        } else if (role.equals("manajer")) {
            tabbedPane.addTab("Stok Barang", new StokBarangPanel());
            tabbedPane.addTab("Kasir",       new KasirPanel(currentUser));
            tabbedPane.addTab("Laporan",     new LaporanPanel());
        } else { // kasir
            tabbedPane.addTab("Kasir",       new KasirPanel(currentUser));
        }

        if (tabbedPane.getTabCount() == 1){
            tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
            UIManager.put("TabbedPane.tabAreaInsets", new Insets(0,0,0,0));
            tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI(){
                @Override
                protected int calculateTabAreaHeight(int placement, int runCount, int maxTabHeight){
                    return 0;
                }
            });
        }

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel lblInfo = new JLabel("  Login sebagai: " + currentUser.getUsername()
                + " | Role: " + currentUser.getStatus().toUpperCase());
        lblInfo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusBar.add(lblInfo, BorderLayout.WEST);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin logout?", "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
