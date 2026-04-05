package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReceiptSummary {
    private final String receiptNumber;
    private final String transactionNumber;
    private final String cashierName;
    private final LocalDateTime createdAt;
    private final LocalDateTime loginTime;
    private final BigDecimal subtotal;
    private final BigDecimal total;
    private final BigDecimal paymentAmount;
    private final BigDecimal changeAmount;
    private final List<SaleItem> items;
    private final String orderType;
    private final String buzzerNumber;
    private final String customerName;

    public ReceiptSummary(String receiptNumber, String transactionNumber, String cashierName, LocalDateTime createdAt,
            LocalDateTime loginTime, BigDecimal subtotal, BigDecimal total, BigDecimal paymentAmount,
            BigDecimal changeAmount, List<SaleItem> items, String orderType, String buzzerNumber,
            String customerName) {
        this.receiptNumber = receiptNumber;
        this.transactionNumber = transactionNumber;
        this.cashierName = cashierName;
        this.createdAt = createdAt;
        this.loginTime = loginTime;
        this.subtotal = subtotal;
        this.total = total;
        this.paymentAmount = paymentAmount;
        this.changeAmount = changeAmount;
        this.items = items;
        this.orderType = orderType;
        this.buzzerNumber = buzzerNumber;
        this.customerName = customerName;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public String getCashierName() {
        return cashierName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
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

    public List<SaleItem> getItems() {
        return items;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getBuzzerNumber() {
        return buzzerNumber;
    }

    public String getCustomerName() {
        return customerName;
    }
}
