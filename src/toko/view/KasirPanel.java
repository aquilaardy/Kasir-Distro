package toko.view;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import toko.model.*;
import toko.util.*;

public class KasirPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static String NAMA_TOKO = "Toko Dripsolers";
    private static String ALAMAT    = "Jl. Raya No. 1, Kota Yogyakarta";
    private static String TELP      = "08123456789";
    private static String EMAIL     = "toko@email.com";

    private final User currentUser;
    private JTextField txtKodeNama, txtJumlah, txtBayar;
    private JLabel lblTanggal, lblNoTransaksi, lblTotal, lblKembali;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<ItemTransaksi> keranjang = new ArrayList<>();
    private String noTransaksi;
    private double totalBelanja = 0;

    private static final String[] KOLOM = {"No", "Nama Barang", "Kode", "Harga Satuan", "Jumlah", "Subtotal"};

    public KasirPanel(User user) {
        this.currentUser = user;
        this.noTransaksi = FileHelper.generateNoTransaksi();
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        startClock();
    }


    private void initComponents() {
        JPanel headerPanel = new JPanel(new GridLayout(2, 3, 6, 2));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Info Toko & Transaksi"));
        lblTanggal     = new JLabel("Memuat waktu...");
        lblNoTransaksi = new JLabel(noTransaksi);
        headerPanel.add(new JLabel("Tgl/Jam:")); headerPanel.add(lblTanggal); headerPanel.add(new JLabel(""));
        headerPanel.add(new JLabel("No Transaksi:")); headerPanel.add(lblNoTransaksi);
        headerPanel.add(new JLabel("Kasir: " + currentUser.getUsername()));

        JPanel tokoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        tokoPanel.setBorder(BorderFactory.createTitledBorder("Nama Toko"));
        JLabel lblNamaToko = new JLabel(NAMA_TOKO + " | " + ALAMAT + " | " + TELP);
        lblNamaToko.setFont(new Font("SansSerif", Font.BOLD, 12));
        tokoPanel.add(lblNamaToko);
        JButton btnEditToko = new JButton("Edit Info Toko");
        btnEditToko.addActionListener(e -> editInfoToko(lblNamaToko));
        tokoPanel.add(btnEditToko);

        JPanel topPanel = new JPanel(new BorderLayout(4, 4));
        topPanel.add(tokoPanel, BorderLayout.NORTH);
        topPanel.add(headerPanel, BorderLayout.CENTER);

        // === Panel Input Barang (manual + scan barcode) ===
        JPanel inputPanel = new JPanel(new BorderLayout(4, 4));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Barang"));

        // Baris 1: input manual kode/nama + jumlah
        JPanel inputBaris1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        inputBaris1.add(new JLabel("Kode/Nama Barang:"));
        txtKodeNama = new JTextField(16); inputBaris1.add(txtKodeNama);
        inputBaris1.add(new JLabel("Jumlah:"));
        txtJumlah = new JTextField(6); txtJumlah.setText("1"); inputBaris1.add(txtJumlah);
        JButton btnCariBarang = new JButton("Tambah ke Keranjang");
        inputBaris1.add(btnCariBarang);

        // Baris 2: scan barcode (USB/Bluetooth scanner kirim kode + Enter)
        JPanel inputBaris2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        inputBaris2.setBackground(new Color(235, 245, 255));
        JLabel lblScan = new JLabel("[SCAN]  Barcode:");
        lblScan.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblScan.setForeground(new Color(0, 80, 160));
        inputBaris2.add(lblScan);
        JTextField txtScanBarcode = new JTextField(18);
        txtScanBarcode.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtScanBarcode.setToolTipText("Klik di sini lalu arahkan scanner. Scanner akan otomatis menekan Enter.");
        inputBaris2.add(txtScanBarcode);
        JLabel lblInfoScan = new JLabel("(klik di sini, lalu scan - jumlah = 1 otomatis)");
        lblInfoScan.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblInfoScan.setForeground(Color.GRAY);
        inputBaris2.add(lblInfoScan);

        inputPanel.add(inputBaris1, BorderLayout.NORTH);
        inputPanel.add(inputBaris2, BorderLayout.SOUTH);
        topPanel.add(inputPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Aksi scan: Enter otomatis dari barcode scanner USB/Bluetooth
        txtScanBarcode.addActionListener(e -> {
            String kode = txtScanBarcode.getText().trim();
            txtScanBarcode.setText(""); // reset agar siap scan berikutnya
            if (!kode.isEmpty()) scanBarcodeMasukKeranjang(kode);
            txtScanBarcode.requestFocus();
        });

        tableModel = new DefaultTableModel(KOLOM, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Ringkasan Pembayaran"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font boldFont = new Font("SansSerif", Font.BOLD, 14);
        lblTotal   = new JLabel("Rp 0", SwingConstants.RIGHT); 
        lblTotal.setFont(boldFont);
        lblKembali = new JLabel("Rp 0", SwingConstants.RIGHT); 
        lblKembali.setFont(boldFont);
        lblKembali.setForeground(new Color(0, 128, 0));
        txtBayar = new JTextField("0", 12);
        txtBayar.setFont(boldFont); txtBayar.setHorizontalAlignment(JTextField.RIGHT);

        addSummaryRow(summaryPanel, gbc, "Total Pembelian:", lblTotal, 0);
        addSummaryRow(summaryPanel, gbc, "Jumlah Bayar:", txtBayar, 1);
        addSummaryRow(summaryPanel, gbc, "Kembalian:", lblKembali, 2);

        txtBayar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { 
                hitungKembali(); 
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { 
                hitungKembali(); 
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                hitungKembali(); 
            }
        });

        JPanel btnPanel = new JPanel(new GridLayout(2, 3, 6, 6));
        btnPanel.setBorder(BorderFactory.createTitledBorder("Aksi"));
        JButton btnNambah    = new JButton("Nambah");
        JButton btnUbah      = new JButton("Ubah");
        JButton btnSimpan    = new JButton("Simpan");
        JButton btnLihat     = new JButton("Lihat");
        JButton btnCetak     = new JButton("Cetak Struk");
        JButton btnHapusItem = new JButton("Hapus Item");
        for (JButton btn : new JButton[]{btnNambah, btnUbah, btnSimpan, btnLihat, btnCetak, btnHapusItem}) {
            btn.setPreferredSize(new Dimension(100, 32)); btnPanel.add(btn);
        }

        bottomPanel.add(summaryPanel, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        btnCariBarang.addActionListener(e -> tambahKeKeranjang());
        txtKodeNama.addActionListener(e -> tambahKeKeranjang());
        btnNambah.addActionListener(e -> resetTransaksi());
        btnUbah.addActionListener(e -> ubahItem());
        btnSimpan.addActionListener(e -> simpanTransaksi());
        btnLihat.addActionListener(e -> lihatTransaksi());
        btnCetak.addActionListener(e -> cetakStruk());
        btnHapusItem.addActionListener(e -> hapusItem());
    }

    private void addSummaryRow(JPanel p, GridBagConstraints gbc, String label, JComponent comp, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label); lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1; p.add(comp, gbc);
    }

    private void startClock() {
        new Timer(1000, e ->
            lblTanggal.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
        ).start();
    }

    private void tambahKeKeranjang() {
        String input  = txtKodeNama.getText().trim();
        String jmlStr = txtJumlah.getText().trim();
        if (input.isEmpty()) { JOptionPane.showMessageDialog(this, "Masukkan kode atau nama barang!"); return; }

        Barang barang = FileHelper.findBarangByKode(input);
        if (barang == null) barang = FileHelper.findBarangByNama(input);
        if (barang == null) { JOptionPane.showMessageDialog(this, "Barang tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE); return; }

        int jumlah;
        try { jumlah = Integer.parseInt(jmlStr); if (jumlah <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Jumlah harus angka positif!", "Error", JOptionPane.ERROR_MESSAGE); return; }

        if (jumlah > barang.getJumlah()) { JOptionPane.showMessageDialog(this, "Stok tidak mencukupi! Stok: " + barang.getJumlah(), "Error", JOptionPane.ERROR_MESSAGE); return; }

        for (ItemTransaksi item : keranjang) {
            if (item.getKodeBarang().equals(barang.getKodeBarang())) {
                int newJml = item.getJumlah() + jumlah;
                if (newJml > barang.getJumlah()) { JOptionPane.showMessageDialog(this, "Total jumlah melebihi stok!"); return; }
                item.setJumlah(newJml); refreshKeranjang();
                txtKodeNama.setText(""); txtJumlah.setText("1"); return;
            }
        }
        keranjang.add(new ItemTransaksi(noTransaksi, barang.getKodeBarang(), barang.getNamaBarang(), barang.getHargaJual(), jumlah));
        refreshKeranjang(); 
        txtKodeNama.setText(""); 
        txtJumlah.setText("1"); 
        txtKodeNama.requestFocus();
    }

    private void refreshKeranjang() {
        tableModel.setRowCount(0); totalBelanja = 0; int no = 1;
        for (ItemTransaksi item : keranjang) {
            tableModel.addRow(new Object[]{ no++, item.getNamaBarang(), item.getKodeBarang(),
                CurrencyFormatter.format(item.getHargaSatuan()), item.getJumlah(), CurrencyFormatter.format(item.getSubtotal()) });
            totalBelanja += item.getSubtotal();
        }
        lblTotal.setText(CurrencyFormatter.format(totalBelanja));
        hitungKembali();
    }

    private void hitungKembali() {
        try {
            String raw = txtBayar.getText().trim().replaceAll("[^0-9]", "");
            if (raw.isEmpty()) { lblKembali.setText(CurrencyFormatter.format(0)); return; }
            double bayar   = Double.parseDouble(raw);
            double kembali = bayar - totalBelanja;
            lblKembali.setText(CurrencyFormatter.format(Math.max(0, kembali)));
            lblKembali.setForeground(kembali >= 0 ? new Color(0, 128, 0) : Color.RED);
        } catch (NumberFormatException ignored) { lblKembali.setText(CurrencyFormatter.format(0)); }
    }

    private double getBayarValue() {
        try { String raw = txtBayar.getText().trim().replaceAll("[^0-9]", ""); 
            return raw.isEmpty() ? 0 : Double.parseDouble(raw); 
        }
        catch (NumberFormatException e) { 
            return 0; 
        }
    }

    private void simpanTransaksi() {
        if (keranjang.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Keranjang kosong!"); 
            return; 
        }
        double bayar = getBayarValue();
        if (bayar <= 0) { 
            JOptionPane.showMessageDialog(this, "Masukkan jumlah bayar yang valid!"); 
            return; 
        }
        if (bayar < totalBelanja) {
            JOptionPane.showMessageDialog(this, "Pembayaran kurang!\nTotal  : " + CurrencyFormatter.format(totalBelanja)
                + "\nBayar  : " + CurrencyFormatter.format(bayar), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double kembali = bayar - totalBelanja;
        Transaksi trx = new Transaksi(noTransaksi, currentUser.getUsername());
        for (ItemTransaksi item : keranjang) trx.addItem(item);
        trx.setTotalBayar(totalBelanja); trx.setJumlahBayar(bayar); trx.setKembalian(kembali);

        List<Barang> stok = FileHelper.loadBarang();
        for (ItemTransaksi item : keranjang){
            for (Barang b : stok){
                if (b.getKodeBarang().equals(item.getKodeBarang())) { 
                    b.setJumlah(b.getJumlah() - item.getJumlah()); break; 
                }
            }
        }
        FileHelper.saveBarang(stok);
        FileHelper.simpanTransaksi(trx);

        JOptionPane.showMessageDialog(this, "Transaksi berhasil disimpan!\nKembalian: " + CurrencyFormatter.format(kembali), "Sukses", JOptionPane.INFORMATION_MESSAGE);
        if (JOptionPane.showConfirmDialog(this, "Cetak struk?", "Cetak", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            PrintHelper.cetakStruk(trx, NAMA_TOKO, ALAMAT, TELP, EMAIL);
        }
        resetTransaksi();
    }

    private void resetTransaksi() {
        keranjang.clear(); tableModel.setRowCount(0); totalBelanja = 0;
        lblTotal.setText("Rp 0"); lblKembali.setText("Rp 0");
        lblKembali.setForeground(new Color(0, 128, 0));
        txtBayar.setText("0");
        noTransaksi = FileHelper.generateNoTransaksi();
        lblNoTransaksi.setText(noTransaksi); // Fix #1: update label di layar
        txtKodeNama.setText(""); txtJumlah.setText("1");
    }

    private void ubahItem() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih item yang ingin diubah!"); return; }
        ItemTransaksi item = keranjang.get(row);
        String input = JOptionPane.showInputDialog(this, "Jumlah baru untuk " + item.getNamaBarang() + ":", item.getJumlah());
        if (input == null) return;
        try {
            int newJml = Integer.parseInt(input.trim());
            if (newJml <= 0) { JOptionPane.showMessageDialog(this, "Jumlah harus > 0!"); return; }
            Barang b = FileHelper.findBarangByKode(item.getKodeBarang());
            if (b != null && newJml > b.getJumlah()) { JOptionPane.showMessageDialog(this, "Stok tidak mencukupi! Stok: " + b.getJumlah()); return; }
            item.setJumlah(newJml); refreshKeranjang();
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Input tidak valid!"); }
    }

    private void hapusItem() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih item yang ingin dihapus!"); return; }
        keranjang.remove(row); refreshKeranjang();
    }

    private void cetakStruk() {
        if (keranjang.isEmpty()) { JOptionPane.showMessageDialog(this, "Keranjang kosong!"); return; }
        double bayar = getBayarValue();
        if (bayar <= 0) bayar = totalBelanja; // Fix #5: fallback jika belum isi bayar
        Transaksi trx = new Transaksi(noTransaksi, currentUser.getUsername());
        for (ItemTransaksi item : keranjang) trx.addItem(item);
        trx.setTotalBayar(totalBelanja); trx.setJumlahBayar(bayar); trx.setKembalian(Math.max(0, bayar - totalBelanja));
        PrintHelper.cetakStruk(trx, NAMA_TOKO, ALAMAT, TELP, EMAIL);
    }

    private void lihatTransaksi() {
        List<Transaksi> list = FileHelper.loadTransaksi();
        if (list.isEmpty()) { JOptionPane.showMessageDialog(this, "Belum ada transaksi tersimpan!"); return; }
        String[] cols = {"No Trx", "Tanggal", "Kasir", "Total"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Transaksi trx : list)
            m.addRow(new Object[]{ trx.getNoTransaksi(), trx.getWaktu().format(fmt), trx.getKasir(), CurrencyFormatter.format(trx.getTotalBayar()) });
        JTable t = new JTable(m);
        JScrollPane sp = new JScrollPane(t); sp.setPreferredSize(new Dimension(500, 300));
        JOptionPane.showMessageDialog(this, sp, "Daftar Transaksi", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Scan barcode: lookup barang berdasarkan kode, langsung masuk keranjang dengan jumlah 1.
     * Jika barang sudah ada di keranjang, jumlahnya bertambah 1.
     */
    private void scanBarcodeMasukKeranjang(String kodeBarcode) {
        Barang barang = FileHelper.findBarangByKode(kodeBarcode);
        if (barang == null) {
            JOptionPane.showMessageDialog(this,
                "Barang dengan kode barcode \"" + kodeBarcode + "\" tidak ditemukan!\n" +
                "Pastikan kode barang sesuai dengan yang terdaftar di stok.",
                "Barang Tidak Ditemukan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Cek stok
        if (barang.getJumlah() <= 0) {
            JOptionPane.showMessageDialog(this,
                "Stok barang \"" + barang.getNamaBarang() + "\" habis!",
                "Stok Habis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Cek apakah barang sudah ada di keranjang
        for (ItemTransaksi item : keranjang) {
            if (item.getKodeBarang().equals(barang.getKodeBarang())) {
                int newJml = item.getJumlah() + 1;
                if (newJml > barang.getJumlah()) {
                    JOptionPane.showMessageDialog(this,
                        "Jumlah melebihi stok! Stok tersedia: " + barang.getJumlah(),
                        "Stok Tidak Cukup", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setJumlah(newJml);
                refreshKeranjang();
                return;
            }
        }
        // Tambah baru ke keranjang dengan jumlah 1
        keranjang.add(new ItemTransaksi(noTransaksi, barang.getKodeBarang(),
            barang.getNamaBarang(), barang.getHargaJual(), 1));
        refreshKeranjang();
    }

    private void editInfoToko(JLabel lblNamaToko) {
        JTextField fNama = new JTextField(NAMA_TOKO), fAlamat = new JTextField(ALAMAT),
                   fTelp = new JTextField(TELP), fEmail = new JTextField(EMAIL);
        Object[] msg = {"Nama Toko:", fNama, "Alamat:", fAlamat, "Telp/WA:", fTelp, "Email:", fEmail};
        if (JOptionPane.showConfirmDialog(this, msg, "Edit Info Toko", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            NAMA_TOKO = fNama.getText().trim(); ALAMAT = fAlamat.getText().trim();
            TELP = fTelp.getText().trim(); EMAIL = fEmail.getText().trim();
            lblNamaToko.setText(NAMA_TOKO + " | " + ALAMAT + " | " + TELP);
            JOptionPane.showMessageDialog(this, "Info toko diperbarui!");
        }
    }
}
