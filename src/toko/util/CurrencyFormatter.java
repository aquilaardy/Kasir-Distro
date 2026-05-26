package toko.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {
    private static final DecimalFormat fmt;

    static {
        // Fix #7: ganti deprecated new Locale("id","ID") dengan Locale.of()
        Locale localeId;
        try {
            localeId = Locale.of("id", "ID"); // Java 21+
        } catch (NoSuchMethodError e) {
            localeId = new Locale("id", "ID"); // fallback Java 8-17
        }
        DecimalFormatSymbols sym = new DecimalFormatSymbols(localeId);
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        fmt = new DecimalFormat("#,###", sym);
    }

    public static String format(double amount) {
        return "Rp " + fmt.format(amount);
    }

    public static String formatPlain(double amount) {
        return fmt.format(amount);
    }
}
