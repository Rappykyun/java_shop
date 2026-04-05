package ui.screens.admin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import model.InventoryMovement;
import model.Product;
import model.User;
import service.InventoryService;
import ui.components.RoundedButton;
import ui.components.UIFactory;
import util.DateTimeUtils;

public class InventoryPanel extends JPanel {
    private final InventoryService inventoryService;
    private final DefaultTableModel stockModel;
    private final DefaultTableModel movementModel;
    private final JTable stockTable;
    private final JTable movementTable;
    private final List<Product> products = new ArrayList<>();
    private User currentUser;

    public InventoryPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);

        stockModel = new DefaultTableModel(
                new Object[] { "ID", "SKU", "Name", "Stock", "Threshold", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable = new JTable(stockModel);
        stockTable.removeColumn(stockTable.getColumnModel().getColumn(0));

        movementModel = new DefaultTableModel(
                new Object[] { "Product", "Type", "Change", "Previous", "New", "Note", "By", "Created" }, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
        movementTable = new JTable(movementModel);

        add(buildTopBar(), BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 16));
        tables.setOpaque(false);

        JPanel lowStockCard = UIFactory.createCard();
        lowStockCard.add(UIFactory.createSectionTitle("Stock Levels"), BorderLayout.NORTH);
        lowStockCard.add(UIFactory.createTableScrollPane(stockTable), BorderLayout.CENTER);

        JPanel movementCard = UIFactory.createCard();
        movementCard.add(UIFactory.createSectionTitle("Recent Inventory Activity"), BorderLayout.NORTH);
        movementCard.add(UIFactory.createTableScrollPane(movementTable), BorderLayout.CENTER);

        tables.add(lowStockCard);
        tables.add(movementCard);
        add(tables, BorderLayout.CENTER);
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void refreshData() throws SQLException {
        loadProducts();
        loadMovements();
    }

    private JPanel buildTopBar() {
        JPanel panel = UIFactory.createCard();
        panel.setLayout(new BorderLayout());
        panel.add(UIFactory.createSectionTitle("Inventory Monitoring"), BorderLayout.WEST);

        RoundedButton restockButton = new RoundedButton("Restock Selected", config.Theme.SUCCESS, java.awt.Color.WHITE);
        restockButton.addActionListener(event -> restockSelected());
        panel.add(restockButton, BorderLayout.EAST);
        return panel;
    }

    private void loadProducts() throws SQLException {
        products.clear();
        products.addAll(inventoryService.listAllProducts());
        stockModel.setRowCount(0);
        for (Product product : products) {
            stockModel.addRow(new Object[] {
                    product.getId(),
                    product.getSku(),
                    product.getName(),
                    product.getStockQuantity(),
                    product.getLowStockThreshold(),
                    product.getStockQuantity() <= product.getLowStockThreshold() ? "Low Stock" : "Healthy"
            });
        }
    }

    private void loadMovements() throws SQLException {
        movementModel.setRowCount(0);
        for (InventoryMovement movement : inventoryService.listRecentMovements()) {
            movementModel.addRow(new Object[] {
                    movement.getProductName(),
                    movement.getMovementType(),
                    movement.getQuantityChange(),
                    movement.getPreviousQuantity(),
                    movement.getNewQuantity(),
                    movement.getNote(),
                    movement.getPerformedBy() == null ? "System" : movement.getPerformedBy(),
                    DateTimeUtils.format(movement.getCreatedAt())
            });
        }
    }

    private void restockSelected() {
        int row = stockTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to restock.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Product product = products.get(stockTable.convertRowIndexToModel(row));

        javax.swing.JTextField quantityField = UIFactory.createTextField();
        javax.swing.JTextField noteField = UIFactory.createTextField();

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.add(new JLabel("Product"));
        form.add(new JLabel(product.getName()));
        form.add(new JLabel("Quantity to Add"));
        form.add(quantityField);
        form.add(new JLabel("Note"));
        form.add(noteField);

        int result = JOptionPane.showConfirmDialog(this, form, "Restock Product",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            inventoryService.restockProduct(product.getId(), Integer.parseInt(quantityField.getText().trim()),
                    noteField.getText().trim(), currentUser);
            refreshData();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Restock Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
