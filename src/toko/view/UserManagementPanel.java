package toko.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import toko.model.User;
import toko.util.FileHelper;

public class UserManagementPanel extends JPanel {

    private static final long serialVersionUID = 1L; // Fix #8

    private JTextField txtUsername, txtPassword;
    private JComboBox<String> cbStatus;
    private JTable table;
    private DefaultTableModel tableModel;
    // Fix #6: simpan referensi logModel agar bisa di-refresh
    private DefaultTableModel logModel;
    private int selectedRow = -1;

    private static final String[] KOLOM = {
        "Username", "Password", "Status"
    };
    private static final String[] ROLES = {
        "admin", "kasir", "manajer"
    };

    public UserManagementPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshTable();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Data User"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = new JTextField(14);
        txtPassword = new JTextField(14);
        cbStatus    = new JComboBox<>(ROLES);

        String[]     labels = {
            "Username:", "Password:", "Status/Role:"
        };
        JComponent[] fields = {
            txtUsername, txtPassword, cbStatus
        };
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; 
            gbc.gridy = i; 
            gbc.weightx = 0; 
            formPanel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 1; 
            formPanel.add(fields[i], gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JButton btnTambah = new JButton("Tambah");
        JButton btnUbah   = new JButton("Ubah");
        JButton btnHapus  = new JButton("Hapus");
        JButton btnBersih = new JButton("Bersihkan");
        // Fix #6: tombol refresh log
        JButton btnRefreshLog = new JButton("Refresh Log");

        for (JButton btn : new JButton[]{btnTambah, btnUbah, btnHapus, btnBersih}) {
            btn.setPreferredSize(new Dimension(100, 28)); 
            btnPanel.add(btn);
        }

        JPanel leftPanel = new JPanel(new BorderLayout(4, 4));
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(btnPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(270, 0));

        tableModel = new DefaultTableModel(KOLOM, 0) {
            private static final long serialVersionUID = 1L;
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);

        // Log login
        String[] logKolom = {
            "Tanggal & Jam", "Username", "Status"
        };
        logModel = new DefaultTableModel(logKolom, 0) {
            private static final long serialVersionUID = 1L;
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };
        JTable logTable = new JTable(logModel);
        logTable.setRowHeight(20);
        JScrollPane logScroll = new JScrollPane(logTable);
        logScroll.setPreferredSize(new Dimension(0, 130));
        refreshLog(); // Fix #6: isi log saat inisialisasi

        JPanel logPanel = new JPanel(new BorderLayout(4, 4));
        logPanel.setBorder(BorderFactory.createTitledBorder("Log Login"));
        JPanel logTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        logTopBar.add(btnRefreshLog);
        logPanel.add(logTopBar, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(4, 4));
        rightPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        rightPanel.add(logPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(280); 
        splitPane.setResizeWeight(0);
        add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> { 
            if (!e.getValueIsAdjusting()) {
                isiFormDariTable();
            } 
        });
        btnTambah.addActionListener(e -> tambahUser());
        btnUbah.addActionListener(e -> ubahUser());
        btnHapus.addActionListener(e -> hapusUser());
        btnBersih.addActionListener(e -> bersihForm());
        btnRefreshLog.addActionListener(e -> refreshLog()); // Fix #6
    }

    // Fix #6: method refreshLog tersendiri agar bisa dipanggil kapan saja
    private void refreshLog() {
        logModel.setRowCount(0);
        for (String[] row : FileHelper.loadLogLogin()){
            logModel.addRow(new Object[]{row[0], row[1], row[2]});
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (User u : FileHelper.loadUsers()){
            tableModel.addRow(new Object[]{
                u.getUsername(), u.getPassword(), u.getStatus()
            });
        }
        selectedRow = -1; 
        bersihForm();
    }

    private void isiFormDariTable() {
        int row = table.getSelectedRow(); 
        if (row < 0) {
            return;
        }
        selectedRow = row;
        txtUsername.setText(tableModel.getValueAt(row, 0).toString());
        txtPassword.setText(tableModel.getValueAt(row, 1).toString());
        cbStatus.setSelectedItem(tableModel.getValueAt(row, 2).toString());
    }

    private void tambahUser() {
        User u = getUserFromForm(); if (u == null) return;
        List<User> list = FileHelper.loadUsers();
        for (User x : list) {
            if (x.getUsername().equalsIgnoreCase(u.getUsername())) {
                JOptionPane.showMessageDialog(this, "Username sudah ada!", "Error", JOptionPane.ERROR_MESSAGE); 
                return;
            }
        }
        list.add(u); 
        FileHelper.saveUsers(list); 
        refreshTable();
        JOptionPane.showMessageDialog(this, "User berhasil ditambahkan!");
    }

    private void ubahUser() {
        if (selectedRow < 0) { 
            JOptionPane.showMessageDialog(this, "Pilih user yang ingin diubah!", "Peringatan", JOptionPane.WARNING_MESSAGE); 
            return; 
        }
        User u = getUserFromForm(); 
        if (u == null) {
            return;
        }
        String oldUsername = tableModel.getValueAt(selectedRow, 0).toString();
        List<User> list = FileHelper.loadUsers();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUsername().equalsIgnoreCase(oldUsername)) {
                list.set(i, u); 
                FileHelper.saveUsers(list); 
                refreshTable();
                JOptionPane.showMessageDialog(this, "User berhasil diubah!"); 
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "User tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void hapusUser() {
        if (selectedRow < 0) { 
            JOptionPane.showMessageDialog(this, "Pilih user yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE); 
            return; 
        }
        String username = tableModel.getValueAt(selectedRow, 0).toString();
        if (username.equals("admin")) { 
            JOptionPane.showMessageDialog(this, "User admin utama tidak dapat dihapus!", "Error", JOptionPane.ERROR_MESSAGE); 
            return; 
        }
        if (JOptionPane.showConfirmDialog(this, "Hapus user '" + username + "'?", "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        List<User> list = FileHelper.loadUsers();
        list.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
        FileHelper.saveUsers(list); 
        refreshTable();
        JOptionPane.showMessageDialog(this, "User berhasil dihapus!");
    }

    private User getUserFromForm() {
        String username = txtUsername.getText().trim(), password = txtPassword.getText().trim();
        String status   = (String) cbStatus.getSelectedItem();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE); 
            return null;
        }
        if (username.contains(",") || password.contains(",")) {
            JOptionPane.showMessageDialog(this, "Tidak boleh mengandung karakter koma!", "Error", JOptionPane.ERROR_MESSAGE); 
            return null;
        }
        return new User(username, password, status);
    }

    private void bersihForm() {
        txtUsername.setText(""); 
        txtPassword.setText(""); 
        cbStatus.setSelectedIndex(0);
        selectedRow = -1; table.clearSelection();
    }
}