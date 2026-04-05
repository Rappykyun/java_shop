package ui.screens.cashier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import config.AppConfig;
import config.Theme;
import model.ReceiptSummary;
import model.SaleItem;
import ui.components.RoundedButton;
import util.CurrencyUtils;
import util.DateTimeUtils;
import util.ImageUtils;

public final class ReceiptDialog {
    private static final Font RECEIPT_FONT = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font RECEIPT_BOLD = new Font("Monospaced", Font.BOLD, 12);
    private static final Font RECEIPT_HEADER = new Font("Monospaced", Font.BOLD, 16);
    private static final Font RECEIPT_SMALL = new Font("Monospaced", Font.PLAIN, 10);
    private static final int RECEIPT_WIDTH = 380;
    private static final String SEPARATOR = "----------------------------------------------";

    private ReceiptDialog() {
    }

    public static void showReceipt(Component parent, ReceiptSummary receipt) {
        JDialog dialog = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(parent), "Receipt",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 0));
        dialog.getContentPane().setBackground(Theme.CREAM);
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel receiptPanel = buildReceiptPanel(receipt);

        JScrollPane scrollPane = new JScrollPane(receiptPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Color.WHITE);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        buttonPanel.setBackground(Theme.CREAM);
        RoundedButton closeButton = new RoundedButton("Close", Theme.ESPRESSO, Color.WHITE);
        closeButton.addActionListener(event -> dialog.dispose());
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(new Dimension(440, 850));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JPanel buildReceiptPanel(ReceiptSummary receipt) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        addLogo(panel);
        addCenteredBold(panel, AppConfig.STORE_NAME, RECEIPT_HEADER);
        addCentered(panel, AppConfig.STORE_OPERATOR, RECEIPT_FONT);
        addCentered(panel, AppConfig.STORE_ADDRESS, RECEIPT_FONT);
        addCentered(panel, "VAT REG TIN: " + AppConfig.VAT_REG_TIN, RECEIPT_FONT);
        addCentered(panel, "MIN: " + AppConfig.MIN, RECEIPT_FONT);
        addCenteredSeparator(panel);

        addCenteredBold(panel, "SALES INVOICE", RECEIPT_BOLD);
        addCentered(panel, "Invoice #: " + receipt.getReceiptNumber(), RECEIPT_FONT);
        addCentered(panel, "Date: " + DateTimeUtils.format(receipt.getCreatedAt()), RECEIPT_FONT);
        if (receipt.getLoginTime() != null) {
            addCentered(panel, "Login Time: " + DateTimeUtils.format(receipt.getLoginTime()), RECEIPT_FONT);
        }
        addCentered(panel, "Cashier: " + receipt.getCashierName(), RECEIPT_FONT);
        if (receipt.getOrderType() != null && !receipt.getOrderType().isEmpty()) {
            addCentered(panel, "Order Type: " + receipt.getOrderType(), RECEIPT_FONT);
        }
        addCenteredSeparator(panel);

        addCentered(panel, padRight("Item", 24) + padRight("Qty", 5) + padLeft("Amount", 12), RECEIPT_BOLD);
        addCenteredSeparator(panel);

        for (SaleItem item : receipt.getItems()) {
            String name = truncate(item.getProductName(), 24);
            String qty = String.valueOf(item.getQuantity());
            String amount = formatAmount(item.getLineTotal());
            addCentered(panel, padRight(name, 24) + padRight(qty, 5) + padLeft(amount, 12), RECEIPT_FONT);
        }
        addCenteredSeparator(panel);

        BigDecimal total = receipt.getTotal();
        BigDecimal payment = receipt.getPaymentAmount();
        BigDecimal change = receipt.getChangeAmount();

        addCenteredAmountLine(panel, "TOTAL", total);
        addCenteredAmountLine(panel, "CASH", payment);
        addCenteredAmountLine(panel, "CHANGE", change);
        addCenteredSeparator(panel);

        BigDecimal vatRate = BigDecimal.valueOf(AppConfig.VAT_RATE);
        BigDecimal divisor = BigDecimal.ONE.add(vatRate);
        BigDecimal vatableSales = total.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal vatAmount = total.subtract(vatableSales);

        addCenteredAmountLine(panel, "VATABLE SALES", vatableSales);
        addCenteredAmountLine(panel, "VAT (12%)", vatAmount);
        addCenteredAmountLine(panel, "VAT-EXEMPT SALES", BigDecimal.ZERO);
        addCenteredAmountLine(panel, "ZERO-RATED SALES", BigDecimal.ZERO);
        addCenteredSeparator(panel);

        addCentered(panel, "This serves as your OFFICIAL RECEIPT", RECEIPT_SMALL);
        addCentered(panel, "POS Provider: " + AppConfig.POS_PROVIDER, RECEIPT_SMALL);
        addCentered(panel, "Accreditation No: " + AppConfig.ACCREDITATION_NO, RECEIPT_SMALL);
        addCenteredSeparator(panel);

        addVerticalGap(panel, 6);
        addCenteredBold(panel, "WE LOVE TO HEAR YOU", RECEIPT_BOLD);
        addCentered(panel, "Scan the QR Code below or", RECEIPT_FONT);
        addCentered(panel, "Share your feedback at", RECEIPT_FONT);
        addCentered(panel, "cuddlecup.ph", RECEIPT_FONT);
        addVerticalGap(panel, 8);
        addQrCode(panel, receipt);
        addVerticalGap(panel, 8);

        addCenteredSeparator(panel);
        addCenteredBold(panel, "THANK YOU FOR DINING WITH US!", RECEIPT_BOLD);
        addCentered(panel, "Please come again.", RECEIPT_FONT);
        addCentered(panel, "Visit us at: " + AppConfig.STORE_WEBSITE, RECEIPT_FONT);
        addCentered(panel, "Date Issued: " + AppConfig.ACCREDITATION_DATE_ISSUED, RECEIPT_FONT);
        addCentered(panel, "Valid Until: " + AppConfig.ACCREDITATION_VALID_UNTIL, RECEIPT_FONT);
        addCenteredSeparator(panel);

        String buzzer = receipt.getBuzzerNumber() != null && !receipt.getBuzzerNumber().isEmpty()
                ? receipt.getBuzzerNumber() : "-";
        String custName = receipt.getCustomerName() != null && !receipt.getCustomerName().isEmpty()
                ? receipt.getCustomerName() : "-";
        addVerticalGap(panel, 6);
        addCenteredBold(panel, "Serving #" + buzzer, RECEIPT_HEADER);
        addCentered(panel, custName, RECEIPT_BOLD);
        addVerticalGap(panel, 10);

        return panel;
    }

    private static void addLogo(JPanel panel) {
        ImageIcon icon = ImageUtils.loadScaled(AppConfig.IMAGE_LOGO, 80, 80);
        if (icon != null) {
            JLabel logoLabel = new JLabel(icon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(logoLabel);
            addVerticalGap(panel, 8);
        }
    }

    private static void addQrCode(JPanel panel, ReceiptSummary receipt) {
        try {
            String qrContent = AppConfig.STORE_NAME + "\n"
                    + "Invoice: " + receipt.getReceiptNumber() + "\n"
                    + "Date: " + DateTimeUtils.format(receipt.getCreatedAt()) + "\n"
                    + "Total: " + CurrencyUtils.format(receipt.getTotal());

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 120, 120);

            BufferedImage image = new BufferedImage(120, 120, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 120, 120);
            g.setColor(Color.BLACK);
            for (int x = 0; x < 120; x++) {
                for (int y = 0; y < 120; y++) {
                    if (matrix.get(x, y)) {
                        g.fillRect(x, y, 1, 1);
                    }
                }
            }
            g.dispose();

            JLabel qrLabel = new JLabel(new ImageIcon(image));
            qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(qrLabel);
        } catch (Exception ignored) {
        }
    }

    private static void addCenteredBold(JPanel panel, String text, Font font) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(font);
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(RECEIPT_WIDTH, label.getPreferredSize().height + 4));
        panel.add(label);
    }

    private static void addCentered(JPanel panel, String text, Font font) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(font);
        label.setForeground(Color.DARK_GRAY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(RECEIPT_WIDTH, label.getPreferredSize().height + 2));
        panel.add(label);
    }

    private static void addLine(JPanel panel, String text) {
        addFormattedLine(panel, text, RECEIPT_FONT);
    }

    private static void addFormattedLine(JPanel panel, String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(Color.BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(RECEIPT_WIDTH, label.getPreferredSize().height + 2));
        panel.add(label);
    }

    private static void addCenteredAmountLine(JPanel panel, String label, BigDecimal amount) {
        String formatted = padRight(label, 28) + padLeft(formatAmount(amount), 13);
        addCentered(panel, formatted, RECEIPT_FONT);
    }

    private static void addSeparator(JPanel panel) {
        addCentered(panel, SEPARATOR, RECEIPT_FONT);
    }

    private static void addCenteredSeparator(JPanel panel) {
        addCentered(panel, SEPARATOR, RECEIPT_FONT);
    }

    private static void addVerticalGap(JPanel panel, int height) {
        JPanel gap = new JPanel();
        gap.setOpaque(false);
        gap.setPreferredSize(new Dimension(0, height));
        gap.setMaximumSize(new Dimension(RECEIPT_WIDTH, height));
        gap.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(gap);
    }

    private static String formatAmount(BigDecimal value) {
        return String.format("%,.2f", value);
    }

    private static String padRight(String text, int length) {
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }

    private static String padLeft(String text, int length) {
        if (text.length() >= length) {
            return text;
        }
        return " ".repeat(length - text.length()) + text;
    }

    private static String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }
}
