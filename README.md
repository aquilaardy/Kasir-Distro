====================================================
  APLIKASI TOKO - Java Swing
  Penyimpanan: File Teks (.txt)
====================================================

STRUKTUR PROYEK:
  src/
    toko/
      Main.java               <- Entry point (jalankan ini)
      model/
        User.java             <- Model data user
        Barang.java           <- Model data barang
        Transaksi.java        <- Model transaksi
        ItemTransaksi.java    <- Model item dalam transaksi
      view/
        LoginFrame.java       <- Form Login
        MainFrame.java        <- Jendela utama + menu role
        StokBarangPanel.java  <- Panel manajemen stok
        KasirPanel.java       <- Panel kasir / point of sale
        LaporanPanel.java     <- Panel laporan
        UserManagementPanel.java <- Panel manajemen user (admin)
      util/
        FileHelper.java       <- Baca/tulis file teks
        CurrencyFormatter.java<- Format mata uang Rupiah
        PrintHelper.java      <- Preview & cetak laporan/struk
  data/                       <- Folder data (otomatis terbuat)
    users.txt                 <- Data akun login
    barang.txt                <- Data stok barang
    transaksi.txt             <- Header transaksi
    item_transaksi.txt        <- Item tiap transaksi
    log_login.txt             <- Log aktivitas login

CARA MENJALANKAN:
  1. Menggunakan JAR (sudah dikompilasi):
     java -jar TokoApp.jar

  2. Compile manual (dari folder TokoApp_Final):
     javac -d out src/toko/**/*.java src/toko/*.java
     java -cp out toko.Main

AKUN DEFAULT:
  Username : admin      | Password: admin123   | Role: Admin
  Username : kasir1     | Password: kasir123   | Role: Kasir
  Username : manajer1   | Password: manajer123 | Role: Manajer

HAK AKSES:
  Admin   -> Stok Barang + Kasir + Laporan + Manajemen User
  Manajer -> Stok Barang + Kasir + Laporan
  Kasir   -> Kasir saja

FORMAT FILE DATA:
  users.txt          : username,password,status
  barang.txt         : kode|nama|hargaDasar|hargaJual|jumlah
  transaksi.txt      : noTrx|waktu|kasir|total|bayar|kembalian
  item_transaksi.txt : noTrx|kode|nama|harga|jumlah
  log_login.txt      : waktu|username|status
====================================================
