package toko.view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import toko.model.Barang;
import toko.util.CurrencyFormatter;
import toko.util.FileHelper;
import toko.util.PrintHelper;

public class StokBarangPanel extends JPanel {

    private static final long serialVersionUID = 1L; // Fix #8

    private JTextField txtKode, txtNama, txtHargaDasar, txtHargaJual, txtJumlah, txtCari;
    private JTable table;
    private DefaultTableModel tableModel;
    private int selectedRow = -1;
    private static String kodeAsli = "";

    private static final String[] KOLOM = {"Kode", "Nama Barang", "Harga Dasar", "Harga Jual", "Jumlah"};

    public StokBarangPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshTable();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Data Barang"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Kode Barang:", "Nama Barang:", "Harga Dasar:", "Harga Jual:", "Jumlah:"};
        txtKode       = new JTextField(14);
        txtNama       = new JTextField(14);
        txtHargaDasar = new JTextField(14);
        txtHargaJual  = new JTextField(14);
        txtJumlah     = new JTextField(14);
        JTextField[] fields = {txtKode, txtNama, txtHargaDasar, txtHargaJual, txtJumlah};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0; formPanel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 1; formPanel.add(fields[i], gbc);
        }

        // Layout tombol 2 baris x 3 kolom agar semua tombol muat di panel kiri
        JPanel btnPanel = new JPanel(new GridLayout(2, 3, 6, 6));
        JButton btnTambah  = new JButton("Tambah");
        JButton btnUbah    = new JButton("Ubah");
        JButton btnHapus   = new JButton("Hapus");
        JButton btnBersih  = new JButton("Bersihkan");
        JButton btnCetak   = new JButton("Cetak Stok");
        JButton btnBarcode = new JButton("Barcode");
        
        for (JButton btn : new JButton[]{btnTambah, btnUbah, btnHapus, btnBersih, btnCetak, btnBarcode}) {
            btn.setPreferredSize(new Dimension(88, 30)); btnPanel.add(btn);
        }
        btnPanel.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));

        JPanel leftPanel = new JPanel(new BorderLayout(4, 4));
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(btnPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(290, 0));

        tableModel = new DefaultTableModel(KOLOM, 0) {
            private static final long serialVersionUID = 1L;
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);

        JPanel cariPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cariPanel.add(new JLabel("Cari Barang:"));
        txtCari = new JTextField(18); cariPanel.add(txtCari);
        JButton btnCari = new JButton("Cari"); cariPanel.add(btnCari);
        JButton btnTampilSemua = new JButton("Tampil Semua"); cariPanel.add(btnTampilSemua);

        JPanel rightPanel = new JPanel(new BorderLayout(4, 4));
        rightPanel.add(cariPanel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(290); splitPane.setResizeWeight(0);
        add(splitPane, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> { 
            if (!e.getValueIsAdjusting()) {
                isiFormDariTable();
            }
        });
        btnTambah.addActionListener(e -> tambahBarang());
        btnUbah.addActionListener(e -> ubahBarang());
        btnHapus.addActionListener(e -> hapusBarang());
        btnBersih.addActionListener(e -> bersihForm());
        btnCetak.addActionListener(e -> cetakStok());
        btnBarcode.addActionListener(e -> bukaBarcode());
        btnCari.addActionListener(e -> cariBarang());
        txtCari.addActionListener(e -> cariBarang());
        btnTampilSemua.addActionListener(e -> refreshTable());
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Barang b : FileHelper.loadBarang()) {
            tableModel.addRow(new Object[]{
                b.getKodeBarang(), b.getNamaBarang(),
                CurrencyFormatter.format(b.getHargaDasar()),
                CurrencyFormatter.format(b.getHargaJual()),
                b.getJumlah()
            });
        }
        selectedRow = -1; bersihForm();
    }

    private void isiFormDariTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedRow = row;
        txtKode.setText(tableModel.getValueAt(row, 0).toString());
        txtNama.setText(tableModel.getValueAt(row, 1).toString());
        kodeAsli = txtKode.getText(); // ← simpan kode asli
        Barang b = FileHelper.findBarangByKode(txtKode.getText());
        if (b != null) {
            txtHargaDasar.setText(String.valueOf((long) b.getHargaDasar()));
            txtHargaJual.setText(String.valueOf((long) b.getHargaJual()));
            txtJumlah.setText(String.valueOf(b.getJumlah()));
        }
    }

    private void tambahBarang() {
        Barang b = getBarangFromForm(); if (b == null) return;
        List<Barang> list = FileHelper.loadBarang();
        for (Barang x : list) {
            if (x.getKodeBarang().equalsIgnoreCase(b.getKodeBarang())) {
                JOptionPane.showMessageDialog(this, "Kode barang sudah ada!", "Error", JOptionPane.ERROR_MESSAGE); return;
            }
        }
        list.add(b); FileHelper.saveBarang(list); refreshTable();
        JOptionPane.showMessageDialog(this, "Barang berhasil ditambahkan!\nBarcode otomatis dibuat untuk kode: " + b.getKodeBarang());
        // Otomatis buka preview barcode untuk barang yang baru ditambahkan
        tampilBarcodeOtomatis(b);
    }

    /**
     * Otomatis menampilkan dialog barcode setelah barang baru ditambahkan.
     * Langsung memilih barang tersebut di BarcodePanel.
     */
    private void tampilBarcodeOtomatis(Barang barangBaru) {
        int jawab = JOptionPane.showConfirmDialog(this,
            "Apakah ingin melihat/mencetak barcode untuk barang baru?\n" +
            "Kode: " + barangBaru.getKodeBarang() + " - " + barangBaru.getNamaBarang(),
            "Barcode Otomatis", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (jawab != JOptionPane.YES_OPTION) {
            return;
        }

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        BarcodePanel dialog = new BarcodePanel(parent);
        // Langsung pilih barang yang baru ditambahkan
        for (int i = 0; i < dialog.tabelModel.getRowCount(); i++) {
            if (dialog.tabelModel.getValueAt(i, 0).toString().equalsIgnoreCase(barangBaru.getKodeBarang())) {
                dialog.tabelBarang.setRowSelectionInterval(i, i);
                dialog.tabelBarang.scrollRectToVisible(dialog.tabelBarang.getCellRect(i, 0, true));
                break;
            }
        }
        dialog.setVisible(true);
    }

    private void ubahBarang() {
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Pilih barang yang ingin diubah!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Barang b = getBarangFromForm();
        if (b == null) return;

        List<Barang> list = FileHelper.loadBarang();

        // Cek jika kode barang diubah
        boolean kodeDiganti = !b.getKodeBarang().equalsIgnoreCase(kodeAsli);

        if (kodeDiganti) {
            // Pastikan kode baru belum dipakai barang lain
            for (Barang x : list) {
                if (x.getKodeBarang().equalsIgnoreCase(b.getKodeBarang())) {
                    JOptionPane.showMessageDialog(this,
                        "Kode barang \"" + b.getKodeBarang() + "\" sudah dipakai barang lain!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Konfirmasi ubah kode karena berdampak ke transaksi
            int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Kode barang akan diubah dari \"" + kodeAsli + "\" → \"" + b.getKodeBarang() + "\"\n" +
                "Barcode lama tidak akan bisa digunakan lagi.\n\n" +
                "Lanjutkan?",
                "Konfirmasi Ubah Kode", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (konfirmasi != JOptionPane.YES_OPTION) return;
        }

        // Update data barang
        boolean ditemukan = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getKodeBarang().equalsIgnoreCase(kodeAsli)) {
                list.set(i, b);
                ditemukan = true;
                break;
            }
        }

        if (!ditemukan) {
            JOptionPane.showMessageDialog(this,
                "Barang tidak ditemukan!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FileHelper.saveBarang(list);

        // Jika kode diganti, update juga di file item_transaksi.txt
        if (kodeDiganti) {
            updateKodeTransaksi(kodeAsli, b.getKodeBarang());
        }

        refreshTable();
        kodeAsli = ""; // reset kode asli

        JOptionPane.showMessageDialog(this,
            "Barang berhasil diubah!" +
            (kodeDiganti ? "\nBarcode baru otomatis dibuat untuk kode: " + b.getKodeBarang() : ""),
            "Sukses", JOptionPane.INFORMATION_MESSAGE);

        // Jika kode diganti, tawarkan lihat barcode baru
        if (kodeDiganti) {
            tampilBarcodeOtomatis(b);
        }
    }

    private void updateKodeTransaksi(String kodeLama, String kodeBaru) {
        List<String> lines = FileHelper.readLines(FileHelper.FILE_ITEM_TRX);
        List<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("\\|");
            // Format item_transaksi: noTrx|kodeBarang|namaBarang|harga|jumlah|subtotal
            if (parts.length >= 2 && parts[1].trim().equalsIgnoreCase(kodeLama)) {
                parts[1] = kodeBaru; // ganti kode lama dengan kode baru
                updated.add(String.join("|", parts));
            } else {
                updated.add(line);
            }
        }
        FileHelper.writeLines(FileHelper.FILE_ITEM_TRX, updated, false);
    }

    private void hapusBarang() {
        if (selectedRow < 0) { JOptionPane.showMessageDialog(this, "Pilih barang yang ingin dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE); return; }
        String kode = tableModel.getValueAt(selectedRow, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Hapus barang kode " + kode + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        List<Barang> list = FileHelper.loadBarang();
        list.removeIf(b -> b.getKodeBarang().equalsIgnoreCase(kode));
        FileHelper.saveBarang(list); 
        refreshTable();
        JOptionPane.showMessageDialog(this, "Barang berhasil dihapus!");
    }

    private void cariBarang() {
        String keyword = txtCari.getText().trim().toLowerCase();
        if (keyword.isEmpty()) { refreshTable(); return; }
        tableModel.setRowCount(0);
        for (Barang b : FileHelper.loadBarang()) {
            if (b.getKodeBarang().toLowerCase().contains(keyword) || b.getNamaBarang().toLowerCase().contains(keyword)) {
                tableModel.addRow(new Object[]{
                    b.getKodeBarang(), b.getNamaBarang(),
                    CurrencyFormatter.format(b.getHargaDasar()),
                    CurrencyFormatter.format(b.getHargaJual()),
                    b.getJumlah()
                });
            }
        }
    }

    private void cetakStok() {
        List<Barang> list = FileHelper.loadBarang();
        if (list.isEmpty()) { JOptionPane.showMessageDialog(this, "Tidak ada data stok!"); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(65)).append("\n");
        sb.append(centerText("LAPORAN STOK BARANG", 65)).append("\n");
        sb.append("=".repeat(65)).append("\n");
        sb.append(String.format("%-5s %-20s %-13s %-13s %6s%n", "No", "Nama Barang", "Harga Dasar", "Harga Jual", "Stok"));
        sb.append("-".repeat(65)).append("\n");
        int no = 1;
        for (Barang b : list) {
            sb.append(String.format("%-5d %-20s %-13s %-13s %6d%n", no++, b.getNamaBarang(),
                CurrencyFormatter.formatPlain(b.getHargaDasar()),
                CurrencyFormatter.formatPlain(b.getHargaJual()), b.getJumlah()));
        }
        sb.append("=".repeat(65)).append("\n");
        PrintHelper.showPrintPreview(sb.toString(), "Cetak Stok Barang");
    }

    private Barang getBarangFromForm() {
        String kode = txtKode.getText().trim(), nama = txtNama.getText().trim(),
               hd   = txtHargaDasar.getText().trim(), hj = txtHargaJual.getText().trim(),
               jml  = txtJumlah.getText().trim();
        if (kode.isEmpty() || nama.isEmpty() || hd.isEmpty() || hj.isEmpty() || jml.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE); 
            return null;
        }
        try {
            double hargaDasar = Double.parseDouble(hd), hargaJual = Double.parseDouble(hj);
            int jumlah = Integer.parseInt(jml);
            if (hargaDasar < 0 || hargaJual < 0 || jumlah < 0) {
                JOptionPane.showMessageDialog(this, "Nilai tidak boleh negatif!", "Peringatan", JOptionPane.WARNING_MESSAGE); return null;
            }
            return new Barang(kode, nama, hargaDasar, hargaJual, jumlah);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga dan Jumlah harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE); return null;
        }
    }

    private void bersihForm() {
        txtKode.setText(""); txtNama.setText("");
        txtHargaDasar.setText(""); txtHargaJual.setText(""); txtJumlah.setText("");
        selectedRow = -1; table.clearSelection();
    }

    private String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return " ".repeat((width - text.length()) / 2) + text;
    }

    private void bukaBarcode() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        BarcodePanel dialog = new BarcodePanel(parent);
        // Jika ada barang yang dipilih, langsung seleksi di dialog
        if (selectedRow >= 0) {
            String kode = tableModel.getValueAt(selectedRow, 0).toString();
            for (int i = 0; i < dialog.tabelModel.getRowCount(); i++) {
                if (dialog.tabelModel.getValueAt(i, 0).toString().equals(kode)) {
                    dialog.tabelBarang.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
        dialog.setVisible(true);
    }
}
