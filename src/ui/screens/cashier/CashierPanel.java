package ui.screens.cashier;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import config.AppConfig;
import config.Theme;
import model.Product;
import model.ReceiptSummary;
import model.SaleItem;
import model.User;
import service.ProductService;
import service.SalesService;
import ui.components.RoundedButton;
import ui.components.RoundedPanel;
import ui.components.UIFactory;
import util.CurrencyUtils;
import util.ImageUtils;

public class CashierPanel extends JPanel {
    private final ProductService productService;
    private final SalesService salesService;
    private final Runnable logoutHandler;

    private final JTextField searchField = UIFactory.createTextField();
    private final JComboBox<String> categoryCombo = new JComboBox<>(
            new String[] { "All", "Iced Coffee", "Milky Series", "Milktea Series", "Hot Coffee", "Frappe" });
    private final JPanel productGrid = new JPanel(new GridLayout(0, 3, 8, 8));
    private final DefaultTableModel cartTableModel;
    private final JTable cartTable;
    private final JLabel cashierLabel = new JLabel();
    private final JLabel subtotalLabel = new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JLabel totalLabel = new JLabel(CurrencyUtils.format(BigDecimal.ZERO));
    private final JTextField paymentField = UIFactory.createTextField();
    private final JComboBox<String> orderTypeCombo = new JComboBox<>(new String[] { "DINE-IN", "TAKE-OUT" });
    private final JTextField buzzerField = UIFactory.createTextField();
    private final JTextField customerNameField = UIFactory.createTextField();

    private final List<Product> products = new ArrayList<>();
    private final List<SaleItem> cartItems = new ArrayList<>();
    private final Map<Integer, Product> availableProductMap = new HashMap<>();
    private User currentUser;
    private LocalDateTime loginTime;

    public CashierPanel(ProductService productService, SalesService salesService, Runnable logoutHandler) {
        this.productService = productService;
        this.salesService = salesService;
        this.logoutHandler = logoutHandler;

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        cartTableModel = new DefaultTableModel(new Object[] { "Product", "Qty", "Unit Price", "Line Total" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartTableModel);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        this.loginTime = LocalDateTime.now();
        this.cashierLabel.setText("Cashier: " + currentUser.getFullName());
        clearCart();
    }

    public void refreshProducts() throws SQLException {
        loadProducts();
    }

    private JPanel buildHeader() {
        JPanel header = UIFactory.createCard();
        header.setLayout(new BorderLayout(12, 12));

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));

        javax.swing.ImageIcon logoIcon = ImageUtils.loadScaled(AppConfig.IMAGE_LOGO, 50, 50);
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            leftPanel.add(logoLabel);
            leftPanel.add(Box.createHorizontalStrut(14));
        }

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JLabel title = UIFactory.createTitle("Cashier POS");
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(6));
        cashierLabel.setFont(Theme.BODY_FONT);
        cashierLabel.setForeground(Theme.TEXT_MUTED);
        textPanel.add(cashierLabel);
        leftPanel.add(textPanel);

        header.add(leftPanel, BorderLayout.WEST);

        RoundedButton logoutButton = new RoundedButton("Logout", Theme.GOLD, Theme.ESPRESSO_DARK);
        logoutButton.addActionListener(event -> logoutHandler.run());
        header.add(logoutButton, BorderLayout.EAST);
        return header;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setOpaque(false);

        JPanel menuColumn = buildMenuImagePanel();

        JPanel productPanel = UIFactory.createCard();
        productPanel.setLayout(new BorderLayout(6, 6));
        productPanel.add(buildProductToolbar(), BorderLayout.NORTH);

        productGrid.setOpaque(false);
        JScrollPane productScroll = new JScrollPane(productGrid);
        productScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        productScroll.getVerticalScrollBar().setUnitIncrement(16);
        productPanel.add(productScroll, BorderLayout.CENTER);

        JPanel cartPanel = UIFactory.createCard();
        cartPanel.setLayout(new BorderLayout(6, 6));
        cartPanel.add(UIFactory.createSectionTitle("Current Order"), BorderLayout.NORTH);
        cartPanel.add(UIFactory.createTableScrollPane(cartTable), BorderLayout.CENTER);
        cartPanel.add(buildCartFooter(), BorderLayout.SOUTH);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, productPanel, cartPanel);
        rightSplit.setResizeWeight(0.22);
        rightSplit.setDividerSize(5);
        rightSplit.setBorder(null);
        rightSplit.setOpaque(false);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuColumn, rightSplit);
        mainSplit.setResizeWeight(0.30);
        mainSplit.setDividerSize(5);
        mainSplit.setBorder(null);
        mainSplit.setOpaque(false);

        content.add(mainSplit, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildMenuImagePanel() {
        RoundedPanel menuPanel = new RoundedPanel(Theme.ESPRESSO, 24);
        menuPanel.setLayout(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        File menuFile = new File(AppConfig.IMAGE_MENU);
        if (menuFile.exists()) {
            ImageIcon rawIcon = new ImageIcon(menuFile.getAbsolutePath());
            int origW = rawIcon.getIconWidth();
            int origH = rawIcon.getIconHeight();

            JLabel menuLabel = new JLabel() {
                @Override
                public void setBounds(int x, int y, int width, int height) {
                    super.setBounds(x, y, width, height);
                    if (width > 0 && height > 0) {
                        double scale = Math.min((double) width / origW, (double) height / origH);
                        int scaledW = (int) (origW * scale);
                        int scaledH = (int) (origH * scale);
                        Image scaled = rawIcon.getImage().getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
                        setIcon(new ImageIcon(scaled));
                    }
                }
            };
            menuLabel.setHorizontalAlignment(SwingConstants.CENTER);
            menuLabel.setVerticalAlignment(SwingConstants.TOP);
            menuPanel.add(menuLabel, BorderLayout.CENTER);
        } else {
            JLabel placeholder = new JLabel("Menu image not found", SwingConstants.CENTER);
            placeholder.setForeground(java.awt.Color.WHITE);
            placeholder.setFont(Theme.SUBTITLE_FONT);
            menuPanel.add(placeholder, BorderLayout.CENTER);
        }

        return menuPanel;
    }

    private JPanel buildProductToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 8));
        toolbar.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setOpaque(false);
        searchField.setPreferredSize(new Dimension(150, 36));
        left.add(searchField, BorderLayout.CENTER);
        categoryCombo.setFont(Theme.BODY_FONT);
        left.add(categoryCombo, BorderLayout.EAST);
        toolbar.add(left, BorderLayout.CENTER);

        JPanel actions = UIFactory.createFlowPanel(java.awt.FlowLayout.RIGHT);
        RoundedButton refreshButton = new RoundedButton("Refresh", Theme.ESPRESSO, java.awt.Color.WHITE);
        refreshButton.addActionListener(event -> {
            try {
                loadProducts();
            } catch (SQLException exception) {
                showError(exception);
            }
        });
        actions.add(refreshButton);
        toolbar.add(actions, BorderLayout.EAST);

        categoryCombo.addActionListener(event -> {
            try {
                loadProducts();
            } catch (SQLException exception) {
                showError(exception);
            }
        });

        return toolbar;
    }

    private JPanel buildCartFooter() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JPanel editRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 2));
        editRow.setOpaque(false);
        RoundedButton addQtyButton = new RoundedButton("+1", Theme.SUCCESS, java.awt.Color.WHITE);
        RoundedButton minusQtyButton = new RoundedButton("-1", Theme.GOLD, Theme.ESPRESSO_DARK);
        RoundedButton removeButton = new RoundedButton("Remove", Theme.DANGER, java.awt.Color.WHITE);
        addQtyButton.addActionListener(event -> adjustSelectedItem(1));
        minusQtyButton.addActionListener(event -> adjustSelectedItem(-1));
        removeButton.addActionListener(event -> removeSelectedItem());
        editRow.add(addQtyButton);
        editRow.add(minusQtyButton);
        editRow.add(removeButton);
        footer.add(editRow);

        JPanel orderRow = new JPanel(new GridLayout(1, 6, 4, 0));
        orderRow.setOpaque(false);
        orderTypeCombo.setFont(Theme.SMALL_FONT);
        buzzerField.setFont(Theme.SMALL_FONT);
        customerNameField.setFont(Theme.SMALL_FONT);
        orderRow.add(smallLabel("Type"));
        orderRow.add(orderTypeCombo);
        orderRow.add(smallLabel("Buzzer#"));
        orderRow.add(buzzerField);
        orderRow.add(smallLabel("Customer"));
        orderRow.add(customerNameField);
        footer.add(orderRow);

        JPanel totalsRow = new JPanel(new GridLayout(1, 6, 4, 0));
        totalsRow.setOpaque(false);
        totalsRow.add(smallLabel("Subtotal"));
        subtotalLabel.setFont(Theme.BODY_BOLD_FONT);
        subtotalLabel.setForeground(Theme.ESPRESSO);
        totalsRow.add(subtotalLabel);
        totalsRow.add(smallLabel("Total"));
        totalLabel.setFont(Theme.BODY_BOLD_FONT);
        totalLabel.setForeground(Theme.ESPRESSO);
        totalsRow.add(totalLabel);
        totalsRow.add(smallLabel("Payment"));
        totalsRow.add(paymentField);
        footer.add(totalsRow);

        JPanel actionRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 4));
        actionRow.setOpaque(false);
        RoundedButton clearButton = new RoundedButton("Clear Cart", Theme.GOLD, Theme.ESPRESSO_DARK);
        RoundedButton checkoutButton = new RoundedButton("Complete Sale", Theme.ESPRESSO, java.awt.Color.WHITE);
        clearButton.addActionListener(event -> clearCart());
        checkoutButton.addActionListener(event -> completeSale());
        actionRow.add(clearButton);
        actionRow.add(checkoutButton);
        footer.add(actionRow);

        return footer;
    }

    private static JLabel smallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.SMALL_BOLD_FONT);
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }

    private void loadProducts() throws SQLException {
        String search = searchField.getText().trim();
        String selectedCategory = (String) categoryCombo.getSelectedItem();

        products.clear();
        List<Product> all = productService.listAvailableProducts(search, "name");

        for (Product p : all) {
            if ("All".equals(selectedCategory) || p.getCategory().equals(selectedCategory)) {
                products.add(p);
            }
        }

        availableProductMap.clear();
        productGrid.removeAll();

        for (Product product : products) {
            availableProductMap.put(product.getId(), product);
            productGrid.add(createProductCard(product));
        }

        if (products.isEmpty()) {
            productGrid.add(UIFactory.centeredLabel("No products found."));
        }

        productGrid.revalidate();
        productGrid.repaint();
    }

    private JPanel createProductCard(Product product) {
        RoundedPanel card = new RoundedPanel(Theme.LATTE, 16);
        card.setLayout(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setPreferredSize(new Dimension(150, 80));

        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(Theme.SMALL_BOLD_FONT);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel priceLabel = new JLabel(CurrencyUtils.format(product.getPrice()));
        priceLabel.setFont(Theme.BODY_BOLD_FONT);
        priceLabel.setForeground(Theme.ESPRESSO);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(nameLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(priceLabel);

        card.add(info, BorderLayout.CENTER);

        card.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                addToCart(product);
            }
        });

        return card;
    }

    private void addToCart(Product product) {
        SaleItem existing = findCartItem(product.getId());
        if (existing != null) {
            int maxStock = availableProductMap.get(product.getId()).getStockQuantity();
            if (existing.getQuantity() >= maxStock) {
                JOptionPane.showMessageDialog(this, "Stock limit reached for " + product.getName() + ".",
                        "Stock", JOptionPane.WARNING_MESSAGE);
                return;
            }
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            cartItems.add(new SaleItem(product.getId(), product.getName(), 1, product.getPrice()));
        }
        refreshCartTable();
    }

    private void adjustSelectedItem(int delta) {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item in the cart first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        SaleItem item = cartItems.get(cartTable.convertRowIndexToModel(row));
        int newQuantity = item.getQuantity() + delta;
        int maxStock = availableProductMap.containsKey(item.getProductId())
                ? availableProductMap.get(item.getProductId()).getStockQuantity()
                : item.getQuantity();
        if (newQuantity <= 0) {
            cartItems.remove(item);
        } else if (newQuantity > maxStock) {
            JOptionPane.showMessageDialog(this, "Cannot exceed current stock.", "Stock",
                    JOptionPane.WARNING_MESSAGE);
            return;
        } else {
            item.setQuantity(newQuantity);
        }
        refreshCartTable();
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item in the cart first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        cartItems.remove(cartTable.convertRowIndexToModel(row));
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleItem item : cartItems) {
            subtotal = subtotal.add(item.getLineTotal());
            cartTableModel.addRow(new Object[] {
                    item.getProductName(),
                    item.getQuantity(),
                    CurrencyUtils.format(item.getUnitPrice()),
                    CurrencyUtils.format(item.getLineTotal())
            });
        }
        subtotalLabel.setText(CurrencyUtils.format(subtotal));
        totalLabel.setText(CurrencyUtils.format(subtotal));
    }

    private void completeSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one item to the cart.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String paymentText = paymentField.getText().trim();
        if (paymentText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the payment amount.",
                    "Payment Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal payment;
        try {
            payment = new BigDecimal(paymentText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid payment amount.",
                    "Invalid Payment", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String orderType = (String) orderTypeCombo.getSelectedItem();
        String buzzer = buzzerField.getText().trim();
        String custName = customerNameField.getText().trim();
        try {
            ReceiptSummary receipt = salesService.processSale(currentUser, new ArrayList<>(cartItems), payment,
                    loginTime, orderType, buzzer, custName);
            ReceiptDialog.showReceipt(this, receipt);
            clearCart();
            loadProducts();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private SaleItem findCartItem(int productId) {
        for (SaleItem item : cartItems) {
            if (item.getProductId() == productId) {
                return item;
            }
        }
        return null;
    }

    private void clearCart() {
        cartItems.clear();
        paymentField.setText("");
        buzzerField.setText("");
        customerNameField.setText("");
        orderTypeCombo.setSelectedIndex(0);
        refreshCartTable();
    }

    private void showError(Exception exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Cashier POS", JOptionPane.ERROR_MESSAGE);
    }
}
