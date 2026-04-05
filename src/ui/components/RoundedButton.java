package ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import config.Theme;

public class RoundedButton extends JButton {
    private final Color backgroundColor;
    private final Color foregroundColor;
    private final int cornerRadius;

    public RoundedButton(String text, Color backgroundColor, Color foregroundColor) {
        this(text, backgroundColor, foregroundColor, 18);
    }

    public RoundedButton(String text, Color backgroundColor, Color foregroundColor, int cornerRadius) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
        this.cornerRadius = cornerRadius;

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(foregroundColor);
        setFont(Theme.BUTTON_FONT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(10, 18, 10, 18));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dimension = super.getPreferredSize();
        return new Dimension(Math.max(120, dimension.width + 10), Math.max(40, dimension.height + 4));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2d = (Graphics2D) graphics.create();
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2d.setColor(isEnabled() ? backgroundColor : backgroundColor.darker());
        graphics2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        graphics2d.dispose();
        super.paintComponent(graphics);
    }

    @Override
    protected void paintBorder(Graphics graphics) {
    }
}
