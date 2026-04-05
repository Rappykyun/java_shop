package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import config.DBConnection;
import dao.ActivityLogDao;
import dao.InventoryDao;
import dao.ProductDao;
import model.InventoryMovement;
import model.Product;
import model.User;

public class InventoryService {
    private final ProductDao productDao = new ProductDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public List<Product> listLowStockProducts() throws SQLException {
        return productDao.listLowStock();
    }

    public List<Product> listAllProducts() throws SQLException {
        return productDao.listAll("");
    }

    public List<InventoryMovement> listRecentMovements() throws SQLException {
        return inventoryDao.listRecentMovements();
    }

    public void restockProduct(int productId, int quantityToAdd, String note, User actor) throws SQLException {
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Restock quantity must be greater than zero.");
        }
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Product product = productDao.findByIdForUpdate(connection, productId);
                if (product == null) {
                    throw new IllegalArgumentException("Product not found.");
                }
                int previousQuantity = product.getStockQuantity();
                int newQuantity = previousQuantity + quantityToAdd;

                productDao.updateStock(connection, productId, newQuantity);
                inventoryDao.insertMovement(connection, productId, "RESTOCK", quantityToAdd,
                        previousQuantity, newQuantity, note, actor.getId());
                activityLogDao.log(connection, actor.getId(), "RESTOCK", "PRODUCT", productId,
                        "Restocked " + product.getName() + " by " + quantityToAdd);
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
