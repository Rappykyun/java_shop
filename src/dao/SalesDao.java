package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.ReceiptSummary;
import model.SaleItem;
import model.SalesTransaction;
import util.DateTimeUtils;

public class SalesDao {
    public int insertTransaction(Connection connection, String transactionNumber, int cashierId,
            BigDecimal subtotal, BigDecimal total, BigDecimal paymentAmount,
            BigDecimal changeAmount, int itemCount,
            String orderType, String buzzerNumber, String customerName) throws SQLException {
        String sql = """
                INSERT INTO sales_transactions
                (transaction_number, cashier_id, subtotal, total, payment_amount, change_amount, item_count,
                 order_type, buzzer_number, customer_name)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, transactionNumber);
            statement.setInt(2, cashierId);
            statement.setBigDecimal(3, subtotal);
            statement.setBigDecimal(4, total);
            statement.setBigDecimal(5, paymentAmount);
            statement.setBigDecimal(6, changeAmount);
            statement.setInt(7, itemCount);
            statement.setString(8, orderType);
            statement.setString(9, buzzerNumber);
            statement.setString(10, customerName);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Unable to read generated sales transaction id.");
            }
        }
    }

    public void insertSaleItem(Connection connection, int transactionId, SaleItem item) throws SQLException {
        String sql = """
                INSERT INTO sale_items (transaction_id, product_id, product_name, quantity, unit_price, line_total)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, transactionId);
            statement.setInt(2, item.getProductId());
            statement.setString(3, item.getProductName());
            statement.setInt(4, item.getQuantity());
            statement.setBigDecimal(5, item.getUnitPrice());
            statement.setBigDecimal(6, item.getLineTotal());
            statement.executeUpdate();
        }
    }

    public void insertReceipt(Connection connection, int transactionId, String receiptNumber) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO receipts (transaction_id, receipt_number) VALUES (?, ?)")) {
            statement.setInt(1, transactionId);
            statement.setString(2, receiptNumber);
            statement.executeUpdate();
        }
    }

    public List<SalesTransaction> listTransactions() throws SQLException {
        String sql = """
                SELECT st.id, st.transaction_number, u.full_name AS cashier_name, st.subtotal, st.total,
                       st.payment_amount, st.change_amount, st.item_count, st.created_at, r.receipt_number
                FROM sales_transactions st
                JOIN users u ON u.id = st.cashier_id
                JOIN receipts r ON r.transaction_id = st.id
                ORDER BY st.created_at DESC
                LIMIT 200
                """;
        List<SalesTransaction> transactions = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                transactions.add(new SalesTransaction(
                        resultSet.getInt("id"),
                        resultSet.getString("transaction_number"),
                        resultSet.getString("cashier_name"),
                        resultSet.getBigDecimal("subtotal"),
                        resultSet.getBigDecimal("total"),
                        resultSet.getBigDecimal("payment_amount"),
                        resultSet.getBigDecimal("change_amount"),
                        resultSet.getInt("item_count"),
                        DateTimeUtils.fromTimestamp(resultSet.getTimestamp("created_at")),
                        resultSet.getString("receipt_number")));
            }
        }
        return transactions;
    }

    public ReceiptSummary findReceiptByTransactionId(int transactionId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            String headerSql = """
                    SELECT r.receipt_number, st.transaction_number, u.full_name AS cashier_name, st.created_at,
                           st.subtotal, st.total, st.payment_amount, st.change_amount,
                           st.order_type, st.buzzer_number, st.customer_name
                    FROM receipts r
                    JOIN sales_transactions st ON st.id = r.transaction_id
                    JOIN users u ON u.id = st.cashier_id
                    WHERE st.id = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(headerSql)) {
                statement.setInt(1, transactionId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    List<SaleItem> items = listReceiptItems(connection, transactionId);
                    return new ReceiptSummary(
                            resultSet.getString("receipt_number"),
                            resultSet.getString("transaction_number"),
                            resultSet.getString("cashier_name"),
                            DateTimeUtils.fromTimestamp(resultSet.getTimestamp("created_at")),
                            null,
                            resultSet.getBigDecimal("subtotal"),
                            resultSet.getBigDecimal("total"),
                            resultSet.getBigDecimal("payment_amount"),
                            resultSet.getBigDecimal("change_amount"),
                            items,
                            resultSet.getString("order_type"),
                            resultSet.getString("buzzer_number"),
                            resultSet.getString("customer_name"));
                }
            }
        }
    }

    private List<SaleItem> listReceiptItems(Connection connection, int transactionId) throws SQLException {
        String itemSql = """
                SELECT product_id, product_name, quantity, unit_price
                FROM sale_items
                WHERE transaction_id = ?
                ORDER BY id ASC
                """;
        List<SaleItem> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(itemSql)) {
            statement.setInt(1, transactionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(new SaleItem(
                            resultSet.getInt("product_id"),
                            resultSet.getString("product_name"),
                            resultSet.getInt("quantity"),
                            resultSet.getBigDecimal("unit_price")));
                }
            }
        }
        return items;
    }
}
