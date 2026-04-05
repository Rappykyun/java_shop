package ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class RoundedPanel extends JPanel {
    private final Color fillColor;
    private final int cornerRadius;

    public RoundedPanel(Color fillColor, int cornerRadius) {
        this.fillColor = fillColor;
        this.cornerRadius = cornerRadius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2d.setColor(fillColor);
        graphics2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        graphics2d.dispose();
        super.paintComponent(graphics);
    }
}
