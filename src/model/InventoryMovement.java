package model;

import java.time.LocalDateTime;

public class InventoryMovement {
    private final int id;
    private final int productId;
    private final String productName;
    private final String movementType;
    private final int quantityChange;
    private final int previousQuantity;
    private final int newQuantity;
    private final String note;
    private final String performedBy;
    private final LocalDateTime createdAt;

    public InventoryMovement(int id, int productId, String productName, String movementType, int quantityChange,
            int previousQuantity, int newQuantity, String note, String performedBy, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.note = note;
        this.performedBy = performedBy;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getMovementType() {
        return movementType;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public int getPreviousQuantity() {
        return previousQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public String getNote() {
        return note;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
