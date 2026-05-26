package toko.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Transaksi {
    private String noTransaksi;
    private LocalDateTime waktu;
    private String kasir;
    private List<ItemTransaksi> items;
    private double totalBayar;
    private double jumlahBayar;
    private double kembalian;

    public Transaksi(String noTransaksi, String kasir) {
        this.noTransaksi = noTransaksi;
        this.kasir = kasir;
        this.waktu = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    public Transaksi(String noTransaksi, LocalDateTime waktu, String kasir,List<ItemTransaksi> items, double totalBayar, double jumlahBayar, double kembalian) {
        this.noTransaksi = noTransaksi;
        this.waktu = waktu;
        this.kasir = kasir;
        this.items = items;
        this.totalBayar = totalBayar;
        this.jumlahBayar = jumlahBayar;
        this.kembalian = kembalian;
    }

    public void addItem(ItemTransaksi item) { 
        items.add(item); 
    }

    public void hitungTotal() {
        totalBayar = 0;
        for (ItemTransaksi item : items) {
            totalBayar += item.getSubtotal();
        }
    }

    public String getNoTransaksi() { 
        return noTransaksi; 
    }

    public LocalDateTime getWaktu() { 
        return waktu; 
    }

    public String getKasir() { 
        return kasir; 
    }

    public List<ItemTransaksi> getItems() { 
        return items; 
    }

    public double getTotalBayar() { 
        return totalBayar; 
    }
    
    public double getJumlahBayar() { 
        return jumlahBayar; 
    }

    public double getKembalian() { 
        return kembalian; 
    }

    public void setTotalBayar(double totalBayar) {
        this.totalBayar = totalBayar; 
    }
    public void setJumlahBayar(double jumlahBayar) { 
        this.jumlahBayar = jumlahBayar; 
    }
    public void setKembalian(double kembalian) { 
        this.kembalian = kembalian; 
    }

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // format pembelian
    public String toHeaderString() {
        return noTransaksi + "|" + waktu.format(FORMATTER) + "|" + kasir + "|" + totalBayar + "|" + jumlahBayar + "|" + kembalian;
    }
}
