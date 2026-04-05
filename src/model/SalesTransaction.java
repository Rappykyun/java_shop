package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesTransaction {
    private final int id;
    private final String transactionNumber;
    private final String cashierName;
    private final BigDecimal subtotal;
    private final BigDecimal total;
    private final BigDecimal paymentAmount;
    private final BigDecimal changeAmount;
    private final int itemCount;
    private final LocalDateTime createdAt;
    private final String receiptNumber;

    public SalesTransaction(int id, String transactionNumber, String cashierName, BigDecimal subtotal,
            BigDecimal total, BigDecimal paymentAmount, BigDecimal changeAmount, int itemCount,
            LocalDateTime createdAt, String receiptNumber) {
        this.id = id;
        this.transactionNumber = transactionNumber;
        this.cashierName = cashierName;
        this.subtotal = subtotal;
        this.total = total;
        this.paymentAmount = paymentAmount;
        this.changeAmount = changeAmount;
        this.itemCount = itemCount;
        this.createdAt = createdAt;
        this.receiptNumber = receiptNumber;
    }

    public int getId() {
        return id;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public String getCashierName() {
        return cashierName;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }
}
