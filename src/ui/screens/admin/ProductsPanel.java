package ui.screens.admin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import config.AppConfig;
import config.Theme;
import model.Product;
import model.User;
import service.ProductService;
import ui.components.RoundedButton;
import ui.components.UIFactory;
import util.CurrencyUtils;

public class ProductsPanel extends JPanel {
    private final ProductService productService;
    private final javax.swing.JTextField searchField = UIFactory.createTextField();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<Product> products = new ArrayList<>();
    private User currentUser;

    public ProductsPanel(ProductService productService) {
        this.productService = productService;
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);

        tableModel = new DefaultTableModel(new Object[] {
                "ID", "SKU", "Name", "Category", "Price", "Stock", "Low Stock", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.removeColumn(table.getColumnModel().getColumn(0));

        add(buildTopBar(), BorderLayout.NORTH);
        add(UIFactory.createTableScrollPane(table), BorderLayout.CENTER);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void refreshData() throws SQLException {
        loadProducts(searchField.getText().trim());
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(12, 12));
        top.setOpaque(false);

        JPanel searchPanel = UIFactory.createCard();
        searchPanel.setLayout(new BorderLayout(10, 10));
        searchPanel.add(UIFactory.createSectionTitle("Products"), BorderLayout.NORTH);

        JPanel filters = new JPanel(new BorderLayout(8, 8));
        filters.setOpaque(false);
        filters.add(searchField, BorderLayout.CENTER);

        RoundedButton searchButton = new RoundedButton("Search", Theme.ESPRESSO, java.awt.Color.WHITE);
        searchButton.addActionListener(event -> runLoadProducts());
        filters.add(searchButton, BorderLayout.EAST);
        searchPanel.add(filters, BorderLayout.CENTER);
        top.add(searchPanel, BorderLayout.CENTER);

        JPanel actions = UIFactory.createFlowPanel(java.awt.FlowLayout.RIGHT);
        RoundedButton addButton = new RoundedButton("Add Product", Theme.SUCCESS, java.awt.Color.WHITE);
        RoundedButton editButton = new RoundedButton("Edit", Theme.GOLD, Theme.ESPRESSO_DARK);
        RoundedButton toggleButton = new RoundedButton("Activate / Deactivate", Theme.ESPRESSO, java.awt.Color.WHITE);

        addButton.addActionListener(event -> openProductDialog(null));
        editButton.addActionListener(event -> openProductDialog(getSelectedProduct()));
        toggleButton.addActionListener(event -> toggleSelectedProduct());

        actions.add(addButton);
        actions.add(editButton);
        actions.add(toggleButton);
        top.add(actions, BorderLayout.EAST);
        return top;
    }

    private void runLoadProducts() {
        try {
            loadProducts(searchField.getText().trim());
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void loadProducts(String search) throws SQLException {
        products.clear();
        products.addAll(productService.listProducts(search));
        tableModel.setRowCount(0);
        for (Product product : products) {
            tableModel.addRow(new Object[] {
                    product.getId(),
                    product.getSku(),
                    product.getName(),
                    product.getCategory(),
                    CurrencyUtils.format(product.getPrice()),
                    product.getStockQuantity(),
                    product.getLowStockThreshold(),
                    product.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private Product getSelectedProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a product first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        return products.get(modelRow);
    }

    private void openProductDialog(Product source) {
        if (source == null && currentUser == null) {
            return;
        }
        if (source != null && currentUser == null) {
            return;
        }

        javax.swing.JTextField skuField = UIFactory.createTextField();
        javax.swing.JTextField nameField = UIFactory.createTextField();
        javax.swing.JTextField categoryField = UIFactory.createTextField();
        javax.swing.JTextField priceField = UIFactory.createTextField();
        javax.swing.JTextField stockField = UIFactory.createTextField();
        javax.swing.JTextField thresholdField = UIFactory.createTextField();
        JTextArea descriptionArea = new JTextArea(4, 22);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(Theme.BODY_FONT);
        descriptionArea.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        JCheckBox activeBox = new JCheckBox("Active");
        activeBox.setOpaque(false);
        activeBox.setFont(Theme.BODY_FONT);

        if (source != null) {
            skuField.setText(source.getSku());
            nameField.setText(source.getName());
            categoryField.setText(source.getCategory());
            priceField.setText(source.getPrice().toPlainString());
            stockField.setText(String.valueOf(source.getStockQuantity()));
            thresholdField.setText(String.valueOf(source.getLowStockThreshold()));
            descriptionArea.setText(source.getDescription());
            activeBox.setSelected(source.isActive());
        } else {
            thresholdField.setText(String.valueOf(AppConfig.LOW_STOCK_THRESHOLD));
            activeBox.setSelected(true);
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.add(new JLabel("SKU"));
        form.add(skuField);
        form.add(new JLabel("Name"));
        form.add(nameField);
        form.add(new JLabel("Category"));
        form.add(categoryField);
        form.add(new JLabel("Price"));
        form.add(priceField);
        form.add(new JLabel("Stock Quantity"));
        form.add(stockField);
        form.add(new JLabel("Low Stock Threshold"));
        form.add(thresholdField);
        form.add(new JLabel("Description"));
        form.add(new JScrollPane(descriptionArea));
        form.add(activeBox);

        int result = JOptionPane.showConfirmDialog(this, form,
                source == null ? "Add Product" : "Edit Product", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Product product = source == null ? new Product() : source;
            product.setSku(skuField.getText().trim());
            product.setName(nameField.getText().trim());
            product.setCategory(categoryField.getText().trim());
            product.setPrice(new BigDecimal(priceField.getText().trim()));
            product.setStockQuantity(Integer.parseInt(stockField.getText().trim()));
            product.setLowStockThreshold(Integer.parseInt(thresholdField.getText().trim()));
            product.setDescription(descriptionArea.getText().trim());
            product.setActive(activeBox.isSelected());
            productService.saveProduct(product, currentUser);
            refreshData();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void toggleSelectedProduct() {
        Product product = getSelectedProduct();
        if (product == null) {
            return;
        }
        try {
            productService.setProductActive(product.getId(), !product.isActive(), currentUser);
            refreshData();
        } catch (Exception exception) {
            showError(exception);
        }
    }

    private void showError(Exception exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(), "Products", JOptionPane.ERROR_MESSAGE);
    }
}
