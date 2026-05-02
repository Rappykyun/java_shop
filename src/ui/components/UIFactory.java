package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import config.Theme;

public final class UIFactory {
    private UIFactory() {
    }

    public static RoundedPanel createCard() {
        RoundedPanel panel = new RoundedPanel(Color.WHITE, 26);
        panel.setLayout(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        return panel;
    }

    public static JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.TITLE_FONT);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }

    public static JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.SUBTITLE_FONT);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }

    public static JLabel createPageTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.PAGE_TITLE_FONT);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }

    public static JLabel createBodyBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY_BOLD_FONT);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }

    public static JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY_FONT);
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(Theme.BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                new EmptyBorder(8, 10, 8, 10)));
        return field;
    }

    public static JScrollPane createTableScrollPane(JTable table) {
        table.setFont(Theme.BODY_FONT);
        table.setRowHeight(32);
        table.setGridColor(new Color(236, 228, 217));
        table.setSelectionBackground(Theme.LATTE);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.BUTTON_FONT);
        header.setBackground(Theme.ESPRESSO);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new java.awt.Dimension(0, 36));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setOpaque(true);
                label.setBackground(Theme.ESPRESSO);
                label.setForeground(Color.WHITE);
                label.setFont(Theme.BUTTON_FONT);
                label.setBorder(new EmptyBorder(0, 10, 0, 10));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                return label;
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    public static JPanel createFlowPanel(int align) {
        JPanel panel = new JPanel(new FlowLayout(align, 8, 8));
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel createMetricCard(String title, String value) {
        RoundedPanel card = new RoundedPanel(Theme.LATTE, 22);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new GridLayout(1, 1, 0, 4));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.SMALL_BOLD_FONT);
        titleLabel.setForeground(Theme.TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new java.awt.Font("Serif", java.awt.Font.BOLD, 30));
        valueLabel.setForeground(Theme.ESPRESSO);

        header.add(titleLabel);

        card.add(header, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public static JLabel centeredLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(Theme.BODY_FONT);
        label.setForeground(Theme.TEXT_MUTED);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
}
