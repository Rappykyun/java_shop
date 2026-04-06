package service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import config.DBConnection;
import dao.ActivityLogDao;
import dao.InventoryDao;
import dao.ProductDao;
import dao.SalesDao;
import dao.UserDao;
import model.Product;
import model.ReceiptSummary;
import model.SaleItem;
import model.RoleType;
import model.SalesTransaction;
import model.User;

public class SalesService {
    private static final DateTimeFormatter SERIAL_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SalesDao salesDao = new SalesDao();
    private final ProductDao productDao = new ProductDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public ReceiptSummary processSale(User cashier, List<SaleItem> items, BigDecimal paymentAmount,
            LocalDateTime loginTime, String orderType, String buzzerNumber, String customerName) throws SQLException {
        if (cashier == null || cashier.getRole() != RoleType.CASHIER || !cashier.isActive()) {
            throw new IllegalArgumentException("Only an active cashier can process sales.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Add at least one item to the cart.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;
        for (SaleItem item : items) {
            subtotal = subtotal.add(item.getLineTotal());
            itemCount += item.getQuantity();
        }
        if (paymentAmount == null || paymentAmount.compareTo(subtotal) < 0) {
            throw new IllegalArgumentException("Payment must cover the total amount.");
        }

        BigDecimal changeAmount = paymentAmount.subtract(subtotal);
        String serial = LocalDateTime.now().format(SERIAL_FORMAT);
        String transactionNumber = "TXN-" + serial;
        String receiptNumber = "RCT-" + serial;

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                User freshCashier = userDao.findById(connection, cashier.getId());
                if (freshCashier == null || !freshCashier.isActive()) {
                    throw new IllegalArgumentException("Cashier account is inactive.");
                }

                for (SaleItem item : items) {
                    Product product = productDao.findByIdForUpdate(connection, item.getProductId());
                    if (product == null || !product.isActive()) {
                        throw new IllegalArgumentException(item.getProductName() + " is not available.");
                    }
                    if (product.getStockQuantity() < item.getQuantity()) {
                        throw new IllegalArgumentException("Insufficient stock for " + product.getName() + ".");
                    }
                }

                int transactionId = salesDao.insertTransaction(connection, transactionNumber, cashier.getId(),
                        subtotal, subtotal, paymentAmount, changeAmount, itemCount,
                        orderType, buzzerNumber, customerName);

                for (SaleItem item : items) {
                    Product product = productDao.findByIdForUpdate(connection, item.getProductId());
                    int previousQuantity = product.getStockQuantity();
                    int newQuantity = previousQuantity - item.getQuantity();

                    salesDao.insertSaleItem(connection, transactionId, item);
                    productDao.updateStock(connection, product.getId(), newQuantity);
                    inventoryDao.insertMovement(connection, product.getId(), "SALE", -item.getQuantity(),
                            previousQuantity, newQuantity, transactionNumber, cashier.getId());
                }

                salesDao.insertReceipt(connection, transactionId, receiptNumber);
                activityLogDao.log(connection, cashier.getId(), "SALE", "TRANSACTION", transactionId,
                        "Completed sale " + transactionNumber + " with " + itemCount + " items");
                connection.commit();
                return new ReceiptSummary(receiptNumber, transactionNumber, cashier.getFullName(),
                        LocalDateTime.now(), loginTime, subtotal, subtotal, paymentAmount, changeAmount, items,
                        orderType, buzzerNumber, customerName);
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<SalesTransaction> listTransactions() throws SQLException {
        return salesDao.listTransactions();
    }

    public List<SalesTransaction> listTransactions(java.time.LocalDate from, java.time.LocalDate to) throws SQLException {
        return salesDao.listTransactions(from, to);
    }

    public ReceiptSummary findReceipt(int transactionId) throws SQLException {
        return salesDao.findReceiptByTransactionId(transactionId);
    }
}
