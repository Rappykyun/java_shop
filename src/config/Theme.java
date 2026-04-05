package config;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public final class Theme {
    public static final Color ESPRESSO = new Color(92, 59, 39);
    public static final Color ESPRESSO_DARK = new Color(63, 39, 25);
    public static final Color LATTE = new Color(241, 230, 214);
    public static final Color CREAM = new Color(252, 248, 242);
    public static final Color MOCHA = new Color(199, 165, 121);
    public static final Color GOLD = new Color(214, 179, 93);
    public static final Color SUCCESS = new Color(72, 139, 77);
    public static final Color DANGER = new Color(173, 74, 74);
    public static final Color TEXT_PRIMARY = new Color(40, 31, 26);
    public static final Color TEXT_MUTED = new Color(106, 92, 81);
    public static final Color BORDER = new Color(220, 210, 196);

    public static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 24);
    public static final Font PAGE_TITLE_FONT = new Font("Serif", Font.BOLD, 34);
    public static final Font SUBTITLE_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font LABEL_BOLD_FONT = new Font("SansSerif", Font.BOLD, 14);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BODY_BOLD_FONT = new Font("SansSerif", Font.BOLD, 15);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font SMALL_BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);

    private Theme() {
    }

    public static Border paddedBorder() {
        return BorderFactory.createEmptyBorder(12, 12, 12, 12);
    }
}
