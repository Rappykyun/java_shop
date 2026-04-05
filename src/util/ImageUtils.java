package util;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;

public final class ImageUtils {
    private ImageUtils() {
    }

    public static ImageIcon loadScaled(String path, int width, int height) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        Image image = new ImageIcon(path).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }
}
