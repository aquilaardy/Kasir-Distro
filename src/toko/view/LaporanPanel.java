package toko.view;

import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.*;
import toko.model.*;
import toko.util.*;

public class LaporanPanel extends JPanel {

    private static final long serialVersionUID = 1L; // Fix #8

    private JComboBox<String> cbJenis, cbPeriode;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblJudul, lblTotal;

    // Fix #4: simpan referensi komponen filter sebagai field agar tidak perlu scan getName()
    private JSpinner spinHarian;
    private JSpinner spinnerTahun;
    private JComboBox<String> cbBulan, cbMinggu;
    private JPanel filterPanel;

    private static final String[] JENIS   = {"Stok Barang", "Kasir (Penjualan)"};
    private static final String[] PERIODE = {"Harian", "Mingguan", "Bulanan", "Tahunan"};
    private static final String[] BULAN_NAMA = {
        "Januari","Februari","Maret","April","Mei","Juni",
        "Juli","Agustus","September","Oktober","November","Desember"
    };

    public LaporanPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(6, 6));

        JPanel kontrolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        kontrolPanel.setBorder(BorderFactory.createTitledBorder("Filter Laporan"));

        kontrolPanel.add(new JLabel("Jenis:"));
        cbJenis = new JComboBox<>(JENIS); kontrolPanel.add(cbJenis);

        kontrolPanel.add(new JLabel("Periode:"));
        cbPeriode = new JComboBox<>(PERIODE); kontrolPanel.add(cbPeriode);

        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        kontrolPanel.add(filterPanel);

        JButton btnTampil = new JButton("Tampilkan");
        btnTampil.setFont(new Font("SansSerif", Font.BOLD, 12)); kontrolPanel.add(btnTampil);
        JButton btnCetak = new JButton("Cetak"); kontrolPanel.add(btnCetak);

        topPanel.add(kontrolPanel, BorderLayout.NORTH);
        lblJudul = new JLabel("", SwingConstants.CENTER);
        lblJudul.setFont(new Font("SansSerif", Font.BOLD, 14));
        topPanel.add(lblJudul, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal = new JLabel(""); lblTotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusPanel.add(lblTotal);
        add(statusPanel, BorderLayout.SOUTH);

        cbPeriode.addActionListener(e -> updateFilterPanel());
        btnTampil.addActionListener(e -> tampilkanLaporan());
        btnCetak.addActionListener(e -> cetakLaporan());

        updateFilterPanel();
    }

    // Fix #4: tidak pakai getName(), langsung simpan referensi ke field
    private void updateFilterPanel() {
        filterPanel.removeAll();
        spinHarian = null; spinnerTahun = null; cbBulan = null; cbMinggu = null;

        String periode = (String) cbPeriode.getSelectedItem();
        int tahunSekarang = LocalDate.now().getYear();

        switch (periode) {
            case "Harian":
                filterPanel.add(new JLabel("Tanggal:"));
                spinHarian = new JSpinner(new SpinnerDateModel());
                spinHarian.setEditor(new JSpinner.DateEditor(spinHarian, "dd/MM/yyyy"));
                spinHarian.setPreferredSize(new Dimension(110, 26));
                filterPanel.add(spinHarian);
                break;

            case "Mingguan":
                filterPanel.add(new JLabel("Tahun:"));
                spinnerTahun = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
                spinnerTahun.setPreferredSize(new Dimension(70, 26));
                filterPanel.add(spinnerTahun);
                filterPanel.add(new JLabel("Minggu ke-:"));
                cbMinggu = new JComboBox<>();
                for (int i = 1; i <= 53; i++) cbMinggu.addItem("Minggu " + i);
                cbMinggu.setSelectedIndex(LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()) - 1);
                filterPanel.add(cbMinggu);
                break;

            case "Bulanan":
                filterPanel.add(new JLabel("Tahun:"));
                spinnerTahun = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
                spinnerTahun.setPreferredSize(new Dimension(70, 26));
                filterPanel.add(spinnerTahun);
                filterPanel.add(new JLabel("Bulan:"));
                cbBulan = new JComboBox<>(BULAN_NAMA);
                cbBulan.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
                filterPanel.add(cbBulan);
                break;

            case "Tahunan":
                filterPanel.add(new JLabel("Tahun:"));
                spinnerTahun = new JSpinner(new SpinnerNumberModel(tahunSekarang, 2000, 2100, 1));
                spinnerTahun.setPreferredSize(new Dimension(70, 26));
                filterPanel.add(spinnerTahun);
                break;
        }

        filterPanel.revalidate();
        filterPanel.repaint();
    }

    private void tampilkanLaporan() {
        String jenis   = (String) cbJenis.getSelectedItem();
        String periode = (String) cbPeriode.getSelectedItem();
        if ("Stok Barang".equals(jenis)) tampilLaporanStok(periode);
        else tampilLaporanKasir(periode);
    }

    private void tampilLaporanStok(String periode) {
        tableModel.setRowCount(0); tableModel.setColumnCount(0);
        for (String col : new String[]{"No","Kode Barang","Nama Barang","Harga Dasar","Harga Jual","Jumlah Stok"})
            tableModel.addColumn(col);

        List<Barang> list = FileHelper.loadBarang();
        int no = 1;
        for (Barang b : list) {
            tableModel.addRow(new Object[]{ no++, b.getKodeBarang(), b.getNamaBarang(),
                CurrencyFormatter.format(b.getHargaDasar()), CurrencyFormatter.format(b.getHargaJual()), b.getJumlah() });
        }
        setColumnWidth(0,40); setColumnWidth(1,90); setColumnWidth(2,180);
        setColumnWidth(3,110); setColumnWidth(4,110); setColumnWidth(5,80);
        lblJudul.setText("Laporan Stok Barang - " + periode);
        lblTotal.setText("Total: " + list.size() + " jenis barang");
    }

    private void tampilLaporanKasir(String periode) {
        tableModel.setRowCount(0); tableModel.setColumnCount(0);
        for (String col : new String[]{"No","No Transaksi","Tanggal","Kasir","Nama Barang","Jumlah","Harga Satuan","Subtotal"})
            tableModel.addColumn(col);

        List<Transaksi> filtered = filterTransaksi(FileHelper.loadTransaksi(), periode);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        int no = 1; double grandTotal = 0;
        for (Transaksi trx : filtered) {
            for (ItemTransaksi item : trx.getItems()) {
                tableModel.addRow(new Object[]{ no++, trx.getNoTransaksi(), trx.getWaktu().format(fmt),
                    trx.getKasir(), item.getNamaBarang(), item.getJumlah(),
                    CurrencyFormatter.format(item.getHargaSatuan()), CurrencyFormatter.format(item.getSubtotal()) });
                grandTotal += item.getSubtotal();
            }
        }
        setColumnWidth(0,40); setColumnWidth(1,90); setColumnWidth(2,120);
        setColumnWidth(3,80); setColumnWidth(4,150); setColumnWidth(5,60);
        setColumnWidth(6,110); setColumnWidth(7,110);
        lblJudul.setText("Laporan Penjualan - " + periode);
        lblTotal.setText("Transaksi: " + filtered.size() + "  |  Grand Total: " + CurrencyFormatter.format(grandTotal));
    }

    // Fix #4: langsung pakai field referensi, tidak scan komponen
    private List<Transaksi> filterTransaksi(List<Transaksi> semua, String periode) {
        LocalDate now = LocalDate.now();
        switch (periode) {
            case "Harian": {
                LocalDate tgl = now;
                if (spinHarian != null) {
                    Date d = (Date) spinHarian.getValue();
                    tgl = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                }
                final LocalDate tglFilter = tgl;
                return semua.stream().filter(t -> t.getWaktu().toLocalDate().equals(tglFilter)).collect(Collectors.toList());
            }
            case "Mingguan": {
                int tahun  = spinnerTahun != null ? (int) spinnerTahun.getValue() : now.getYear();
                int minggu = cbMinggu    != null ? cbMinggu.getSelectedIndex() + 1 : 1;
                return semua.stream().filter(t -> {
                    int tw = t.getWaktu().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
                    return t.getWaktu().getYear() == tahun && tw == minggu;
                }).collect(Collectors.toList());
            }
            case "Bulanan": {
                int tahun = spinnerTahun != null ? (int) spinnerTahun.getValue() : now.getYear();
                int bulan = cbBulan     != null ? cbBulan.getSelectedIndex() + 1 : now.getMonthValue();
                return semua.stream().filter(t -> t.getWaktu().getYear() == tahun && t.getWaktu().getMonthValue() == bulan).collect(Collectors.toList());
            }
            case "Tahunan": {
                int tahun = spinnerTahun != null ? (int) spinnerTahun.getValue() : now.getYear();
                return semua.stream().filter(t -> t.getWaktu().getYear() == tahun).collect(Collectors.toList());
            }
            default: return semua;
        }
    }

    private void cetakLaporan() {
        if (tableModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Tidak ada data untuk dicetak!"); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(80)).append("\n");
        sb.append(centerText(lblJudul.getText(), 80)).append("\n");
        sb.append(centerText("Dicetak: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), 80)).append("\n");
        sb.append("=".repeat(80)).append("\n");
        StringBuilder header = new StringBuilder();
        for (int c = 0; c < tableModel.getColumnCount(); c++){
            header.append(String.format("%-14s", tableModel.getColumnName(c)));
        }
        sb.append(header).append("\n").append("-".repeat(80)).append("\n");
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                Object val = tableModel.getValueAt(r, c);
                row.append(String.format("%-14s", val != null ? val.toString() : ""));
            }
            sb.append(row).append("\n");
        }
        sb.append("=".repeat(80)).append("\n").append(lblTotal.getText()).append("\n");
        PrintHelper.showPrintPreview(sb.toString(), "Cetak " + lblJudul.getText());
    }

    private void setColumnWidth(int col, int width) {
        if (col < table.getColumnCount()) table.getColumnModel().getColumn(col).setPreferredWidth(width);
    }

    private String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return " ".repeat((width - text.length()) / 2) + text;
    }
}
