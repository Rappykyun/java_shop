package ui.screens.admin;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import model.ReceiptSummary;
import model.SalesTransaction;
import service.SalesService;
import ui.components.RoundedButton;
import ui.components.UIFactory;
import ui.screens.cashier.ReceiptDialog;
import util.CurrencyUtils;
import util.DateTimeUtils;

public class TransactionsPanel extends JPanel {
    private final SalesService salesService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<SalesTransaction> transactions = new ArrayList<>();

    public TransactionsPanel(SalesService salesService) {
        this.salesService = salesService;
        setLayout(new BorderLayout(16, 16));
        setOpaque(false);

        tableModel = new DefaultTableModel(
                new Object[] { "ID", "Transaction #", "Receipt #", "Cashier", "Items", "Total", "Payment", "Change",
                        "Created" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.removeColumn(table.getColumnModel().getColumn(0));

        JPanel top = UIFactory.createCard();
        top.setLayout(new BorderLayout());
        top.add(UIFactory.createSectionTitle("Transaction History"), BorderLayout.WEST);
        RoundedButton viewReceiptButton = new RoundedButton("View Receipt", config.Theme.ESPRESSO,
                java.awt.Color.WHITE);
        viewReceiptButton.addActionListener(event -> viewReceipt());
        top.add(viewReceiptButton, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(UIFactory.createTableScrollPane(table), BorderLayout.CENTER);
    }

    public void refreshData() throws SQLException {
        transactions.clear();
        transactions.addAll(salesService.listTransactions());
        tableModel.setRowCount(0);
        for (SalesTransaction transaction : transactions) {
            tableModel.addRow(new Object[] {
                    transaction.getId(),
                    transaction.getTransactionNumber(),
                    transaction.getReceiptNumber(),
                    transaction.getCashierName(),
                    transaction.getItemCount(),
                    CurrencyUtils.format(transaction.getTotal()),
                    CurrencyUtils.format(transaction.getPaymentAmount()),
                    CurrencyUtils.format(transaction.getChangeAmount()),
                    DateTimeUtils.format(transaction.getCreatedAt())
            });
        }
    }

    private void viewReceipt() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a transaction first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        SalesTransaction transaction = transactions.get(table.convertRowIndexToModel(row));
        try {
            ReceiptSummary receipt = salesService.findReceipt(transaction.getId());
            if (receipt == null) {
                throw new IllegalArgumentException("Receipt not found.");
            }
            ReceiptDialog.showReceipt(this, receipt);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Receipt", JOptionPane.ERROR_MESSAGE);
        }
    }
}
