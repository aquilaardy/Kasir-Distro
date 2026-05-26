package toko.util;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Menghasilkan gambar barcode format Code128B secara murni (tanpa library eksternal).
 * Code128B mendukung semua karakter ASCII 32-126.
 *
 * Parameter generate(text, width, height):
 *   - width  = lebar TOTAL gambar output (px) — sama seperti versi asli
 *   - height = tinggi gambar output (px)
 *
 * Perbaikan dari versi asli:
 *   1. Checksum seed pakai START_B (104), bukan CODE_B (100)
 *   2. STOP pattern 13-bit yang benar
 *   3. moduleWidth minimal 2px agar scanner HP bisa membaca
 *   4. Quiet zone minimal 10px
 */
public class BarcodeGenerator {

    private static final int[] CODE128B_PATTERNS = {
        0b11011001100, // 0  SPACE
        0b11001101100, // 1  !
        0b11001100110, // 2  "
        0b10010011000, // 3  #
        0b10010001100, // 4  $
        0b10001001100, // 5  %
        0b10011001000, // 6  &
        0b10011000100, // 7  '
        0b10001100100, // 8  (
        0b11001001000, // 9  )
        0b11001000100, // 10 *
        0b11000100100, // 11 +
        0b10110011100, // 12 ,
        0b10011011100, // 13 -
        0b10011001110, // 14 .
        0b10111001100, // 15 /
        0b10011101100, // 16 0
        0b10011100110, // 17 1
        0b11001110010, // 18 2
        0b11001011100, // 19 3
        0b11001001110, // 20 4
        0b11011100100, // 21 5
        0b11001110100, // 22 6
        0b11101101110, // 23 7
        0b11101001100, // 24 8
        0b11100101100, // 25 9
        0b11100100110, // 26 :
        0b11101100100, // 27 ;
        0b11100110100, // 28 <
        0b11100110010, // 29 =
        0b11011011000, // 30 >
        0b11011000110, // 31 ?
        0b11000110110, // 32 @
        0b10100011000, // 33 A
        0b10001011000, // 34 B
        0b10001000110, // 35 C
        0b10110001000, // 36 D
        0b10001101000, // 37 E
        0b10001100010, // 38 F
        0b11010001000, // 39 G
        0b11000101000, // 40 H
        0b11000100010, // 41 I
        0b10110111000, // 42 J
        0b10110001110, // 43 K
        0b10001101110, // 44 L
        0b10111011000, // 45 M
        0b10111000110, // 46 N
        0b10001110110, // 47 O
        0b11101110110, // 48 P
        0b11010001110, // 49 Q
        0b11000101110, // 50 R
        0b11011101000, // 51 S
        0b11011100010, // 52 T
        0b11011101110, // 53 U
        0b11101011000, // 54 V
        0b11101000110, // 55 W
        0b11100010110, // 56 X
        0b11101101000, // 57 Y
        0b11101100010, // 58 Z
        0b11100011010, // 59 [
        0b11101111010, // 60 \
        0b11001000010, // 61 ]
        0b11110001010, // 62 ^
        0b10100110000, // 63 _
        0b10100001100, // 64 `
        0b10010110000, // 65 a
        0b10010000110, // 66 b
        0b10000101100, // 67 c
        0b10000100110, // 68 d
        0b10110010000, // 69 e
        0b10110000100, // 70 f
        0b10011010000, // 71 g
        0b10011000010, // 72 h
        0b10000110100, // 73 i
        0b10000110010, // 74 j
        0b11000010010, // 75 k
        0b11001010000, // 76 l
        0b11110111010, // 77 m
        0b11000010100, // 78 n
        0b10001111010, // 79 o
        0b10100111100, // 80 p
        0b10010111100, // 81 q
        0b10010011110, // 82 r
        0b10111100100, // 83 s
        0b10011110100, // 84 t
        0b10011110010, // 85 u
        0b11110100100, // 86 v
        0b11110010100, // 87 w
        0b11110010010, // 88 x
        0b11011011110, // 89 y
        0b11011110110, // 90 z
        0b11110110110, // 91 {
        0b10101111000, // 92 |
        0b10100011110, // 93 }
        0b10001011110, // 94 ~
        0b10111101000, // 95 DEL
        0b10111100010, // 96 FNC3
        0b11110101000, // 97 FNC2
        0b11110100010, // 98 SHIFT
        0b10011110110, // 99 CODE C
        0b10011101110, // 100 CODE B
        0b11001011110, // 101 CODE A
        0b11110101110, // 102 FNC1
        0b11010000100, // 103 START A
        0b11010010000, // 104 START B
        0b11010011100, // 105 START C
        0b11000111010, // 106 STOP
    };

    private static final int START_B = 104;
    private static final int STOP    = 106;

    private static int charToCode128B(char c) {
        return c - 32;
    }

    /**
     * Generate barcode Code128B sebagai BufferedImage.
     *
     * @param text   teks yang di-encode (kode barang)
     * @param width  lebar TOTAL gambar output dalam piksel (sama seperti versi asli)
     * @param height tinggi gambar output dalam piksel
     */
    public static BufferedImage generate(String text, int width, int height) {
        if (text == null || text.isEmpty()) text = "000000";

        // Sanitasi: hanya ASCII 32-126
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 32 && c <= 126) sb.append(c);
            else sb.append('?');
        }
        text = sb.toString();

        // Bangun simbol: START_B + data + checksum + STOP
        int[] symbols = new int[text.length() + 3];
        int idx = 0;
        symbols[idx++] = START_B;

        // FIX 1: seed checksum = START_B (104), bukan CODE_B (100)
        int checksum = START_B;
        for (int i = 0; i < text.length(); i++) {
            int code = charToCode128B(text.charAt(i));
            symbols[idx++] = code;
            checksum += code * (i + 1);
        }
        symbols[idx++] = checksum % 103;
        symbols[idx++] = STOP;

        // Total bit: semua simbol 11 bit, STOP 13 bit
        int totalBits = (symbols.length - 1) * 11 + 13;

        // Render modul (1 unit = 1 elemen boolean)
        boolean[] modules = new boolean[totalBits];
        int bitPos = 0;
        for (int si = 0; si < symbols.length; si++) {
            if (si == symbols.length - 1) {
                // FIX 2: STOP pattern 13-bit yang benar
                int pat13 = 0b1100011101011;
                for (int b = 12; b >= 0; b--) {
                    modules[bitPos++] = ((pat13 >> b) & 1) == 1;
                }
            } else {
                int pattern = CODE128B_PATTERNS[symbols[si]];
                for (int b = 10; b >= 0; b--) {
                    modules[bitPos++] = ((pattern >> b) & 1) == 1;
                }
            }
        }

        // FIX 3: hitung moduleWidth dari lebar total, minimal 2px agar scanner bisa baca
        int quietZone  = 10; // px tetap
        int usableWidth = width - quietZone * 2;
        int moduleWidth = Math.max(2, usableWidth / totalBits);

        // Hitung ulang lebar aktual setelah pembulatan
        int actualWidth = totalBits * moduleWidth + quietZone * 2;

        BufferedImage img = new BufferedImage(actualWidth, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, actualWidth, height);

        g2.setColor(Color.BLACK);
        int x = quietZone;
        for (boolean bar : modules) {
            if (bar) g2.fillRect(x, 0, moduleWidth, height);
            x += moduleWidth;
        }
        g2.dispose();
        return img;
    }

    /**
     * Generate barcode dengan ukuran default.
     */
    public static BufferedImage generate(String text) {
        return generate(text, 300, 80);
    }
}