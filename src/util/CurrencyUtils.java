package util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyUtils {
    private static final Locale PH_LOCALE = Locale.forLanguageTag("en-PH");

    private CurrencyUtils() {
    }

    public static String format(BigDecimal value) {
        NumberFormat format = NumberFormat.getCurrencyInstance(PH_LOCALE);
        return format.format(value == null ? BigDecimal.ZERO : value);
    }
}
