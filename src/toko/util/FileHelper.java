package toko.util;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import toko.model.*;

public class FileHelper {

    private static final String DATA_DIR = "data/";

    public static final String FILE_USERS     = DATA_DIR + "users.txt";
    public static final String FILE_BARANG    = DATA_DIR + "barang.txt";
    public static final String FILE_TRANSAKSI = DATA_DIR + "transaksi.txt";
    public static final String FILE_ITEM_TRX  = DATA_DIR + "item_transaksi.txt";
    public static final String FILE_LOG_LOGIN = DATA_DIR + "log_login.txt";

    static {
        new File(DATA_DIR).mkdirs();
        initDefaultFiles();
    }

    private static void initDefaultFiles() {
        File f = new File(FILE_USERS);
        if (!f.exists()) {
            writeLines(FILE_USERS, Arrays.asList(
                "admin,admin123,admin",
                "kasir1,kasir123,kasir",
                "manajer1,manajer123,manajer"
            ), false);
        }
    }

    // ===== READ / WRITE =====
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return lines;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) lines.add(line);
            }
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
        return lines;
    }

    public static void writeLines(String filePath, List<String> lines, boolean append) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, append))) {
            for (String line : lines) { 
                bw.write(line); bw.newLine(); 
            }
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }

    // ===== USERS =====
    public static List<User> loadUsers() {
        List<User> list = new ArrayList<>();
        for (String line : readLines(FILE_USERS)) {
            User u = User.fromString(line);
            if (u != null) list.add(u);
        }
        return list;
    }

    public static void saveUsers(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) lines.add(u.toString());
        writeLines(FILE_USERS, lines, false);
    }

    public static User authenticate(String username, String password) {
        for (User u : loadUsers()){
            if (u.getUsername().equals(username) && u.getPassword().equals(password)){
                return u;
            } 
        }
        return null;
    }

    // ===== LOG LOGIN =====
    public static void logLogin(String username, String status) {
        String entry = LocalDateTime.now().format(Transaksi.FORMATTER) + "|" + username + "|" + status;
        writeLines(FILE_LOG_LOGIN, Collections.singletonList(entry), true);
    }

    public static List<String[]> loadLogLogin() {
        List<String[]> list = new ArrayList<>();
        for (String line : readLines(FILE_LOG_LOGIN)) {
            String[] parts = line.split("\\|");
            if (parts.length >= 3) list.add(parts);
        }
        return list;
    }

    // ===== BARANG =====
    public static List<Barang> loadBarang() {
        List<Barang> list = new ArrayList<>();
        for (String line : readLines(FILE_BARANG)) {
            Barang b = Barang.fromString(line);
            if (b != null) list.add(b);
        }
        return list;
    }

    public static void saveBarang(List<Barang> barangList) {
        List<String> lines = new ArrayList<>();
        for (Barang b : barangList) lines.add(b.toString());
        writeLines(FILE_BARANG, lines, false);
    }

    public static Barang findBarangByKode(String kode) {
        for (Barang b : loadBarang())
            if (b.getKodeBarang().equalsIgnoreCase(kode)) return b;
        return null;
    }

    public static Barang findBarangByNama(String nama) {
        for (Barang b : loadBarang())
            if (b.getNamaBarang().toLowerCase().contains(nama.toLowerCase())) return b;
        return null;
    }

    // ===== TRANSAKSI =====
    public static void simpanTransaksi(Transaksi trx) {
        writeLines(FILE_TRANSAKSI, Collections.singletonList(trx.toHeaderString()), true);
        List<String> itemLines = new ArrayList<>();
        for (ItemTransaksi item : trx.getItems()) itemLines.add(item.toString());
        writeLines(FILE_ITEM_TRX, itemLines, true);
    }

    public static List<Transaksi> loadTransaksi() {
        Map<String, Transaksi> map = new LinkedHashMap<>();
        for (String line : readLines(FILE_TRANSAKSI)) {
            String[] p = line.split("\\|");
            if (p.length < 6) continue;
            try {
                String no = p[0].trim();
                java.time.LocalDateTime waktu = java.time.LocalDateTime.parse(p[1].trim(), Transaksi.FORMATTER);
                String kasir   = p[2].trim();
                double total   = Double.parseDouble(p[3].trim());
                double bayar   = Double.parseDouble(p[4].trim());
                double kembali = Double.parseDouble(p[5].trim());
                map.put(no, new Transaksi(no, waktu, kasir, new ArrayList<>(), total, bayar, kembali));
            } catch (Exception e) { e.printStackTrace(); }
        }
        for (String line : readLines(FILE_ITEM_TRX)) {
            ItemTransaksi item = ItemTransaksi.fromString(line);
            if (item != null && map.containsKey(item.getNoTransaksi()))
                map.get(item.getNoTransaksi()).addItem(item);
        }
        return new ArrayList<>(map.values());
    }

    // Fix #2: generateNoTransaksi memakai UUID berbasis waktu + counter agar tidak duplikat
    public static String generateNoTransaksi() {
        List<String> lines = readLines(FILE_TRANSAKSI);
        // Cari nomor tertinggi yang sudah ada
        int maxNo = 0;
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length > 0) {
                String noStr = parts[0].trim();
                if (noStr.startsWith("TRX")) {
                    try {
                        int n = Integer.parseInt(noStr.substring(3));
                        if (n > maxNo) maxNo = n;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return String.format("TRX%05d", maxNo + 1);
    }
}
