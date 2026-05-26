package toko.model;

public class Barang {
    private String kodeBarang;
    private String namaBarang;
    private double hargaDasar;
    private double hargaJual;
    private int jumlah;

    public Barang(String kodeBarang, String namaBarang, double hargaDasar, double hargaJual, int jumlah) {
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.hargaDasar = hargaDasar;
        this.hargaJual = hargaJual;
        this.jumlah = jumlah;
    }

    public String getKodeBarang() { 
        return kodeBarang; 
    }

    public String getNamaBarang() { 
        return namaBarang; 
    }

    public double getHargaDasar() { 
        return hargaDasar; 
    }

    public double getHargaJual() { 
        return hargaJual; 
    }

    public int getJumlah() { 
        return jumlah; 
    }

    public void setKodeBarang(String kodeBarang) { 
        this.kodeBarang = kodeBarang; 
    }

    public void setNamaBarang(String namaBarang) { 
        this.namaBarang = namaBarang; 
    }

    public void setHargaDasar(double hargaDasar) { 
        this.hargaDasar = hargaDasar; 
    }

    public void setHargaJual(double hargaJual) { 
        this.hargaJual = hargaJual; 
    }
    
    public void setJumlah(int jumlah) { 
        this.jumlah = jumlah; 
    }

    @Override
    public String toString() {
        return kodeBarang + "|" + namaBarang + "|" + hargaDasar + "|" + hargaJual + "|" + jumlah;
    }

    public static Barang fromString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 5) return null;
        try {
            return new Barang(
                parts[0].trim(),
                parts[1].trim(),
                Double.parseDouble(parts[2].trim()),
                Double.parseDouble(parts[3].trim()),
                Integer.parseInt(parts[4].trim())
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
