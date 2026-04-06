package ui.screens.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import config.Theme;
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

    private final JLabel filterLabel = new JLabel("Showing: All Sales");
    private final JLabel totalSalesLabel = new JLabel("Total Sales: ₱0.00");
    private final JLabel transactionCountLabel = new JLabel("Transactions: 0");
    private final JLabel totalItemsLabel = new JLabel("Items Sold: 0");

    private LocalDate filterFrom;
    private LocalDate filterTo;

    public TransactionsPanel(SalesService salesService) {
        this.salesService = salesService;
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Transaction #", "Receipt #", "Cashier", "Items", "Total", "Payment", "Change",
                        "Created"},
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.removeColumn(table.getColumnModel().getColumn(0));

        add(buildToolbar(), BorderLayout.NORTH);
        add(UIFactory.createTableScrollPane(table), BorderLayout.CENTER);
        add(buildSummaryBar(), BorderLayout.SOUTH);
    }

    public void refreshData() throws SQLException {
        transactions.clear();
        if (filterFrom != null && filterTo != null) {
            transactions.addAll(salesService.listTransactions(filterFrom, filterTo));
        } else {
            transactions.addAll(salesService.listTransactions());
        }
        tableModel.setRowCount(0);
        BigDecimal totalSales = BigDecimal.ZERO;
        int totalItems = 0;
        for (SalesTransaction transaction : transactions) {
            totalSales = totalSales.add(transaction.getTotal());
            totalItems += transaction.getItemCount();
            tableModel.addRow(new Object[]{
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
        totalSalesLabel.setText("Total Sales: " + CurrencyUtils.format(totalSales));
        transactionCountLabel.setText("Transactions: " + transactions.size());
        totalItemsLabel.setText("Items Sold: " + totalItems);
    }

    private JPanel buildToolbar() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JPanel top = UIFactory.createCard();
        top.setLayout(new BorderLayout());
        top.add(UIFactory.createSectionTitle("Transaction History"), BorderLayout.WEST);

        JPanel topActions = UIFactory.createFlowPanel(FlowLayout.RIGHT);
        RoundedButton viewReceiptButton = new RoundedButton("View Receipt", Theme.ESPRESSO, java.awt.Color.WHITE);
        viewReceiptButton.addActionListener(event -> viewReceipt());
        topActions.add(viewReceiptButton);
        top.add(topActions, BorderLayout.EAST);

        JPanel filterRow = UIFactory.createCard();
        filterRow.setLayout(new BorderLayout());

        JPanel filterButtons = UIFactory.createFlowPanel(FlowLayout.LEFT);
        RoundedButton todayBtn = new RoundedButton("Today", Theme.ESPRESSO, java.awt.Color.WHITE);
        RoundedButton weekBtn = new RoundedButton("This Week", Theme.ESPRESSO, java.awt.Color.WHITE);
        RoundedButton monthBtn = new RoundedButton("This Month", Theme.ESPRESSO, java.awt.Color.WHITE);
        RoundedButton allBtn = new RoundedButton("All Sales", Theme.GOLD, Theme.ESPRESSO_DARK);

        todayBtn.addActionListener(event -> applyFilter("Today", LocalDate.now(), LocalDate.now()));
        weekBtn.addActionListener(event -> {
            LocalDate now = LocalDate.now();
            LocalDate startOfWeek = now.with(java.time.DayOfWeek.MONDAY);
            applyFilter("This Week", startOfWeek, now);
        });
        monthBtn.addActionListener(event -> {
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            applyFilter("This Month", startOfMonth, now);
        });
        allBtn.addActionListener(event -> applyFilter("All Sales", null, null));

        filterButtons.add(todayBtn);
        filterButtons.add(weekBtn);
        filterButtons.add(monthBtn);
        filterButtons.add(allBtn);

        filterLabel.setFont(Theme.BODY_BOLD_FONT);
        filterLabel.setForeground(Theme.ESPRESSO);
        filterLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        filterRow.add(filterButtons, BorderLayout.WEST);
        filterRow.add(filterLabel, BorderLayout.EAST);

        wrapper.add(top);
        wrapper.add(Box.createVerticalStrut(6));
        wrapper.add(filterRow);
        return wrapper;
    }

    private JPanel buildSummaryBar() {
        JPanel summary = UIFactory.createCard();
        summary.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 8));

        totalSalesLabel.setFont(Theme.SUBTITLE_FONT);
        totalSalesLabel.setForeground(Theme.ESPRESSO);
        transactionCountLabel.setFont(Theme.BODY_BOLD_FONT);
        transactionCountLabel.setForeground(Theme.TEXT_PRIMARY);
        totalItemsLabel.setFont(Theme.BODY_BOLD_FONT);
        totalItemsLabel.setForeground(Theme.TEXT_PRIMARY);

        summary.add(totalSalesLabel);
        summary.add(transactionCountLabel);
        summary.add(totalItemsLabel);
        return summary;
    }

    private void applyFilter(String label, LocalDate from, LocalDate to) {
        this.filterFrom = from;
        this.filterTo = to;
        this.filterLabel.setText("Showing: " + label);
        try {
            refreshData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Filter Error", JOptionPane.ERROR_MESSAGE);
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
