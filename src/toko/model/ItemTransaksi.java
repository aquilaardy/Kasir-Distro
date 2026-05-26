package toko.model;

public class ItemTransaksi {
    private String noTransaksi;
    private String kodeBarang;
    private String namaBarang;
    private double hargaSatuan;
    private int jumlah;

    public ItemTransaksi(String noTransaksi, String kodeBarang, String namaBarang, double hargaSatuan, int jumlah) {
        this.noTransaksi = noTransaksi;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.hargaSatuan = hargaSatuan;
        this.jumlah = jumlah;
    }

    public double getSubtotal() { 
        return hargaSatuan * jumlah; 
    }

    public String getNoTransaksi() { 
        return noTransaksi; 
    }

    public String getKodeBarang() {
        return kodeBarang; 
    }

    public String getNamaBarang() {
        return namaBarang; 
    }

    public double getHargaSatuan() { 
        return hargaSatuan; 
    }

    public int getJumlah() { 
        return jumlah;
    }

    public void setJumlah(int jumlah) { 
        this.jumlah = jumlah; 
    }

    // format untuk setiap transaksi yang terjadi
    @Override
    public String toString() {
        return noTransaksi + "|" + kodeBarang + "|" + namaBarang + "|" + hargaSatuan + "|" + jumlah;
    }

    public static ItemTransaksi fromString(String line) {
        String[] p = line.split("\\|");
        if (p.length < 5) return null;
        try {
            return new ItemTransaksi(p[0].trim(), p[1].trim(), p[2].trim(),
                    Double.parseDouble(p[3].trim()), Integer.parseInt(p[4].trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
