package toko.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import toko.model.Barang;
import toko.util.BarcodeGenerator;
import toko.util.CurrencyFormatter;
import toko.util.FileHelper;


/**
 * Dialog untuk menampilkan, mencetak, dan mengekspor barcode barang.
 * Dipanggil dari StokBarangPanel.
 */
public class BarcodePanel extends JDialog {

    private static final long serialVersionUID = 1L;

    // Preview area
    private JLabel lblBarcodeImage;
    private JLabel lblKodeBarang, lblNamaBarang, lblHarga;
    private JSpinner spinJumlahCetak;
    private JComboBox<String> cbUkuran;
    JTable tabelBarang;
    DefaultTableModel tabelModel;

    // Barang yang sedang dipilih
    private Barang selectedBarang;

    private static final String[] UKURAN = {
        "Kecil (200x60)", "Sedang (300x90)", "Besar (450x120)"
    };
    private static final int[][] UKURAN_DIM = {
        {200, 60}, {300, 90}, {450, 120}
    };

    public BarcodePanel(Frame parent) {
        super(parent, "Barcode Barang", true);
        setMinimumSize(new Dimension(820, 560));
        initComponents();
        loadBarang();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== KIRI: Tabel daftar barang =====
        JPanel leftPanel = new JPanel(new BorderLayout(4, 4));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Pilih Barang"));
        leftPanel.setPreferredSize(new Dimension(300, 0));

        // Search
        JPanel searchPanel = new JPanel(new BorderLayout(4, 4));
        JTextField txtCari = new JTextField();
        searchPanel.add(new JLabel("Cari: "), BorderLayout.WEST);
        searchPanel.add(txtCari, BorderLayout.CENTER);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        String[] kolom = {"Kode", "Nama Barang", "Harga Jual"};
        tabelModel = new DefaultTableModel(kolom, 0) {
            private static final long serialVersionUID = 1L;
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };
        tabelBarang = new JTable(tabelModel);
        tabelBarang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelBarang.setRowHeight(22);
        tabelBarang.getColumnModel().getColumn(0).setPreferredWidth(70);
        tabelBarang.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabelBarang.getColumnModel().getColumn(2).setPreferredWidth(90);
        leftPanel.add(new JScrollPane(tabelBarang), BorderLayout.CENTER);

        // ===== KANAN: Preview barcode =====
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));

        // Info barang
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 4, 4));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Info Barang"));
        lblKodeBarang = new JLabel("-");
        lblNamaBarang = new JLabel("-");
        lblHarga      = new JLabel("-");
        infoPanel.add(new JLabel("Kode:")); infoPanel.add(lblKodeBarang);
        infoPanel.add(new JLabel("Nama:")); infoPanel.add(lblNamaBarang);
        infoPanel.add(new JLabel("Harga Jual:")); infoPanel.add(lblHarga);
        rightPanel.add(infoPanel, BorderLayout.NORTH);

        // Preview barcode
        JPanel previewPanel = new JPanel(new BorderLayout(4, 4));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview Barcode"));
        lblBarcodeImage = new JLabel("← Pilih barang untuk melihat barcode", SwingConstants.CENTER);
        lblBarcodeImage.setPreferredSize(new Dimension(480, 150));
        lblBarcodeImage.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblBarcodeImage.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblBarcodeImage.setForeground(Color.GRAY);
        previewPanel.add(lblBarcodeImage, BorderLayout.CENTER);
        rightPanel.add(previewPanel, BorderLayout.CENTER);

        // Opsi cetak
        JPanel opsiPanel = new JPanel(new GridBagLayout());
        opsiPanel.setBorder(BorderFactory.createTitledBorder("Opsi Cetak"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        opsiPanel.add(new JLabel("Ukuran Barcode:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cbUkuran = new JComboBox<>(UKURAN);
        cbUkuran.setSelectedIndex(1); // default: Sedang
        opsiPanel.add(cbUkuran, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        opsiPanel.add(new JLabel("Jumlah Label Cetak:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        spinJumlahCetak = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        opsiPanel.add(spinJumlahCetak, gbc);

        // Tombol aksi
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        JButton btnCetak  = new JButton("Cetak Barcode");
        JButton btnSimpan = new JButton("Simpan PNG");
        JButton btnCetakSemua = new JButton("Cetak Semua Barang");
        JButton btnTutup  = new JButton("Tutup");

        btnCetak.setPreferredSize(new Dimension(130, 30));
        btnSimpan.setPreferredSize(new Dimension(120, 30));
        btnCetakSemua.setPreferredSize(new Dimension(160, 30));
        btnTutup.setPreferredSize(new Dimension(80, 30));

        btnPanel.add(btnCetak); btnPanel.add(btnSimpan);
        btnPanel.add(btnCetakSemua); btnPanel.add(btnTutup);

        JPanel southRight = new JPanel(new BorderLayout(4, 4));
        southRight.add(opsiPanel, BorderLayout.CENTER);
        southRight.add(btnPanel, BorderLayout.SOUTH);
        rightPanel.add(southRight, BorderLayout.SOUTH);

        // ===== SPLITTER =====
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(300);
        split.setResizeWeight(0);
        add(split, BorderLayout.CENTER);

        // ===== EVENTS =====
        tabelBarang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) piliBarang();
        });

        cbUkuran.addActionListener(e -> refreshPreview());

        txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { 
                filterTabel(txtCari.getText()); 
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { 
                filterTabel(txtCari.getText()); 
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { 
                filterTabel(txtCari.getText()); 
            }
        });

        btnCetak.addActionListener(e -> cetakBarcode());
        btnSimpan.addActionListener(e -> simpanPng());
        btnCetakSemua.addActionListener(e -> cetakSemuaBarcode());
        btnTutup.addActionListener(e -> dispose());
    }

    private void loadBarang() {
        tabelModel.setRowCount(0);
        for (Barang b : FileHelper.loadBarang()) {
            tabelModel.addRow(new Object[]{
                b.getKodeBarang(), b.getNamaBarang(),
                CurrencyFormatter.format(b.getHargaJual())
            });
        }
    }

    private void filterTabel(String keyword) {
        tabelModel.setRowCount(0);
        keyword = keyword.trim().toLowerCase();
        for (Barang b : FileHelper.loadBarang()) {
            if (keyword.isEmpty()
                    || b.getKodeBarang().toLowerCase().contains(keyword)
                    || b.getNamaBarang().toLowerCase().contains(keyword)) {
                tabelModel.addRow(new Object[]{
                    b.getKodeBarang(), b.getNamaBarang(),
                    CurrencyFormatter.format(b.getHargaJual())
                });
            }
        }
    }

    private void piliBarang() {
        int row = tabelBarang.getSelectedRow();
        if (row < 0) return;
        String kode = tabelModel.getValueAt(row, 0).toString();
        selectedBarang = FileHelper.findBarangByKode(kode);
        if (selectedBarang == null) return;

        lblKodeBarang.setText(selectedBarang.getKodeBarang());
        lblNamaBarang.setText(selectedBarang.getNamaBarang());
        lblHarga.setText(CurrencyFormatter.format(selectedBarang.getHargaJual()));
        refreshPreview();
    }

    private void refreshPreview() {
        if (selectedBarang == null) return;
        int[] dim = UKURAN_DIM[cbUkuran.getSelectedIndex()];
        BufferedImage img = BarcodeGenerator.generate(selectedBarang.getKodeBarang(), dim[0], dim[1]);

        // gambar dengan kode teks di bawahnya
        BufferedImage combined = buildLabelImage(selectedBarang, img, dim[0], dim[1]);

        // skala preview agar tidak terpotong
        int previewW = lblBarcodeImage.getWidth();
        if (previewW <= 0) previewW = 460;
        int targetW = Math.min(combined.getWidth(), previewW - 20); // beri margin 20px kiri-kanan
        ImageIcon icon = new ImageIcon(combined.getScaledInstance(targetW, -1, Image.SCALE_SMOOTH));
        lblBarcodeImage.setIcon(icon);
        lblBarcodeImage.setText("");
        lblBarcodeImage.setHorizontalAlignment(SwingConstants.CENTER); // pastikan tengah
    }

    private BufferedImage buildLabelImage(Barang b, BufferedImage barcode, int bw, int bh) {
        int padding    = 30;  // padding kiri-kanan lebih besar agar tidak mepet
        int textHeight = 18;
        int lineGap    = 6;
        int totalH     = padding + textHeight + lineGap + bh + lineGap + textHeight + lineGap + textHeight + padding;
        int totalW     = bw + padding * 2; // lebar barcode + padding kiri & kanan

        BufferedImage img = new BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Background putih
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, totalW, totalH);

        // Border tipis di sekeliling label
        g.setColor(new Color(200, 200, 200));
        g.drawRect(1, 1, totalW - 2, totalH - 2);

        int y = padding;

        // Nama barang (bold, tengah)
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.setColor(Color.BLACK);
        drawCentered(g, b.getNamaBarang(), totalW, y + textHeight);
        y += textHeight + lineGap;

        // Gambar barcode — ditaruh tepat di tengah horizontal
        int barcodeX = (totalW - barcode.getWidth()) / 2; // hitung posisi tengah
        if (barcodeX < padding) barcodeX = padding;       // minimal padding
        g.drawImage(barcode, barcodeX, y, null);
        y += bh + lineGap;

        // Kode barang (monospace, tengah)
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.setColor(Color.DARK_GRAY);
        drawCentered(g, b.getKodeBarang(), totalW, y + textHeight);
        y += textHeight + lineGap;

        // Harga jual (bold, tengah)
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(0, 100, 0)); // warna hijau gelap untuk harga
        drawCentered(g, CurrencyFormatter.format(b.getHargaJual()), totalW, y + textHeight);

        g.dispose();
        return img;
    }

    private void drawCentered(Graphics2D g, String text, int panelWidth, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (panelWidth - fm.stringWidth(text)) / 2;
        g.drawString(text, Math.max(0, x), y);
    }

    private void cetakBarcode() {
        if (selectedBarang == null) {
            JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu!"); return;
        }
        int jumlah = (int) spinJumlahCetak.getValue();
        int[] dim  = UKURAN_DIM[cbUkuran.getSelectedIndex()];
        cetakLabelBarcode(List.of(selectedBarang), jumlah, dim[0], dim[1]);
    }

    private void cetakSemuaBarcode() {
        List<Barang> semuaBarang = FileHelper.loadBarang();
        if (semuaBarang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data barang!"); return;
        }
        int jumlah = (int) spinJumlahCetak.getValue();
        int[] dim  = UKURAN_DIM[cbUkuran.getSelectedIndex()];
        int confirm = JOptionPane.showConfirmDialog(this,
            "Cetak barcode untuk " + semuaBarang.size() + " barang, masing-masing " + jumlah + " label?",
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION)
            cetakLabelBarcode(semuaBarang, jumlah, dim[0], dim[1]);
    }

    private void cetakLabelBarcode(List<Barang> barangList, int jumlahPerBarang, int bw, int bh) {
        // Bangun satu halaman berisi semua label (2 kolom)
        int cols       = 2;
        int padding    = 10;
        int labelW     = bw + padding * 2;
        int textH      = 16;
        int labelH     = padding + textH + 4 + bh + 4 + textH + 4 + textH + padding;
        int gapX       = 16;
        int gapY       = 12;

        int totalLabels = barangList.size() * jumlahPerBarang;
        int rows        = (int) Math.ceil((double) totalLabels / cols);

        int pageW = cols * labelW + (cols - 1) * gapX + 40;
        int pageH = rows * labelH + (rows - 1) * gapY + 40;

        // Render ke BufferedImage besar
        BufferedImage page = new BufferedImage(pageW, pageH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = page.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.fillRect(0, 0, pageW, pageH);

        int col = 0, row = 0;
        for (Barang b : barangList) {
            BufferedImage barcode = BarcodeGenerator.generate(b.getKodeBarang(), bw, bh);
            BufferedImage label   = buildLabelImage(b, barcode, bw, bh);
            for (int i = 0; i < jumlahPerBarang; i++) {
                int x = 20 + col * (labelW + gapX);
                int y = 20 + row * (labelH + gapY);
                // Border tipis label
                g.setColor(new Color(200, 200, 200));
                g.drawRect(x - 1, y - 1, labelW + 1, labelH + 1);
                g.drawImage(label, x, y, null);
                col++;
                if (col >= cols) { col = 0; row++; }
            }
        }
        g.dispose();

        // Tampilkan print preview di JFrame baru
        JFrame previewFrame = new JFrame("Preview Cetak Barcode");
        ImageIcon pageIcon = new ImageIcon(page.getScaledInstance(
            Math.min(page.getWidth(), 800), -1, Image.SCALE_SMOOTH));
        JLabel previewLabel = new JLabel(pageIcon);
        JScrollPane scroll = new JScrollPane(previewLabel);
        scroll.setPreferredSize(new Dimension(820, 550));

        JButton btnPrint = new JButton("Cetak");
        btnPrint.addActionListener(ev -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            final BufferedImage finalPage = page;
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) graphics;
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double scaleX = pageFormat.getImageableWidth()  / finalPage.getWidth();
                double scaleY = pageFormat.getImageableHeight() / finalPage.getHeight();
                double scale  = Math.min(scaleX, scaleY);
                g2.scale(scale, scale);
                g2.drawImage(finalPage, 0, 0, null);
                return Printable.PAGE_EXISTS;
            });
            if (job.printDialog()) {
                try { job.print(); }
                catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(previewFrame, "Gagal mencetak: " + ex.getMessage());
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPrint, BorderLayout.SOUTH);
        previewFrame.setContentPane(panel);
        previewFrame.pack();
        previewFrame.setLocationRelativeTo(this);
        previewFrame.setVisible(true);
    }

    private void simpanPng() {
        if (selectedBarang == null) {
            JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu!"); return;
        }
        int[] dim = UKURAN_DIM[cbUkuran.getSelectedIndex()];
        BufferedImage barcode  = BarcodeGenerator.generate(selectedBarang.getKodeBarang(), dim[0], dim[1]);
        BufferedImage labelImg = buildLabelImage(selectedBarang, barcode, dim[0], dim[1]);

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Barcode sebagai PNG");
        chooser.setSelectedFile(new File("barcode_" + selectedBarang.getKodeBarang() + ".png"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png"))
                file = new File(file.getAbsolutePath() + ".png");
            try {
                ImageIO.write(labelImg, "PNG", file);
                JOptionPane.showMessageDialog(this, "Barcode berhasil disimpan:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}