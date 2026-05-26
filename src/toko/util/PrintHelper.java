package toko.util;

import toko.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrintHelper implements Printable {

    private String[] lines;

    public PrintHelper(String[] lines) {
        this.lines = lines;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        Font font = new Font("Monospaced", Font.PLAIN, 10);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int y = lineHeight;
        for (String line : lines) {
            g2d.drawString(line, 0, y);
            y += lineHeight;
        }
        return PAGE_EXISTS;
    }

    public static void cetakStruk(Transaksi trx, String namaToko, String alamat, String telp, String email) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("================================\n");
        sb.append(centerText(namaToko, 32)).append("\n");
        sb.append(centerText(alamat, 32)).append("\n");
        sb.append(centerText(telp, 32)).append("\n");
        sb.append(centerText(email, 32)).append("\n");
        sb.append("================================\n");
        sb.append("No  : ").append(trx.getNoTransaksi()).append("\n");
        sb.append("Tgl : ").append(trx.getWaktu().format(fmt)).append("\n");
        sb.append("Kasir: ").append(trx.getKasir()).append("\n");
        sb.append("--------------------------------\n");
        sb.append(String.format("%-16s %5s %10s%n", "Nama Barang", "Qty", "Subtotal"));
        sb.append("--------------------------------\n");
        for (ItemTransaksi item : trx.getItems()) {
            sb.append(String.format("%-16s%n", item.getNamaBarang()));
            sb.append(String.format("  %d x %-8s  %10s%n",
                    item.getJumlah(),
                    CurrencyFormatter.formatPlain(item.getHargaSatuan()),
                    CurrencyFormatter.formatPlain(item.getSubtotal())));
        }
        sb.append("================================\n");
        sb.append(String.format("%-16s %15s%n", "TOTAL", CurrencyFormatter.format(trx.getTotalBayar())));
        sb.append(String.format("%-16s %15s%n", "BAYAR", CurrencyFormatter.format(trx.getJumlahBayar())));
        sb.append(String.format("%-16s %15s%n", "KEMBALI", CurrencyFormatter.format(trx.getKembalian())));
        sb.append("================================\n");
        sb.append(centerText("Terima Kasih!", 32)).append("\n");

        showPrintPreview(sb.toString(), "Preview Struk - " + trx.getNoTransaksi());
    }

    public static void showPrintPreview(String content, String title) {
        JFrame frame = new JFrame(title);
        JTextArea ta = new JTextArea(content);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);
        JScrollPane scroll = new JScrollPane(ta);
        scroll.setPreferredSize(new Dimension(420, 500));

        JButton btnCetak = new JButton("Cetak");
        btnCetak.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(new PrintHelper(content.split("\n")));
            if (job.printDialog()) {
                try { job.print(); } catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(frame, "Gagal mencetak: " + ex.getMessage());
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnCetak, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }
}
