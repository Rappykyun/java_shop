package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import config.DBConnection;
import dao.ActivityLogDao;
import dao.InventoryDao;
import dao.ProductDao;
import model.Product;
import model.User;

public class ProductService {
    private final ProductDao productDao = new ProductDao();
    private final InventoryDao inventoryDao = new InventoryDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public List<Product> listProducts(String search) throws SQLException {
        return productDao.listAll(search);
    }

    public List<Product> listAvailableProducts(String search, String sortBy) throws SQLException {
        return productDao.listAvailableForSale(search, sortBy);
    }

    public void saveProduct(Product product, User actor) throws SQLException {
        validate(product);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (product.getId() == 0) {
                    int productId = productDao.insert(connection, product);
                    inventoryDao.insertMovement(connection, productId, "CREATE", product.getStockQuantity(),
                            0, product.getStockQuantity(), "Product created", actor.getId());
                    activityLogDao.log(connection, actor.getId(), "CREATE", "PRODUCT", productId,
                            "Added product " + product.getName());
                } else {
                    Product existing = productDao.findById(connection, product.getId());
                    if (existing == null) {
                        throw new IllegalArgumentException("Product not found.");
                    }
                    productDao.update(connection, product);
                    if (existing.getStockQuantity() != product.getStockQuantity()) {
                        inventoryDao.insertMovement(connection, product.getId(), "ADJUSTMENT",
                                product.getStockQuantity() - existing.getStockQuantity(),
                                existing.getStockQuantity(), product.getStockQuantity(),
                                "Stock adjusted from product editor", actor.getId());
                    }
                    if (existing.getPrice().compareTo(product.getPrice()) != 0) {
                        activityLogDao.log(connection, actor.getId(), "UPDATE", "PRODUCT", product.getId(),
                                "Updated price for " + product.getName());
                    }
                    activityLogDao.log(connection, actor.getId(), "UPDATE", "PRODUCT", product.getId(),
                            "Updated product " + product.getName());
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void setProductActive(int productId, boolean active, User actor) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Product product = productDao.findById(connection, productId);
                if (product == null) {
                    throw new IllegalArgumentException("Product not found.");
                }
                product.setActive(active);
                productDao.update(connection, product);
                activityLogDao.log(connection, actor.getId(), active ? "ACTIVATE" : "DEACTIVATE", "PRODUCT", productId,
                        (active ? "Activated " : "Deactivated ") + product.getName());
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void validate(Product product) {
        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new IllegalArgumentException("SKU is required.");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Price must be zero or higher.");
        }
        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        if (product.getLowStockThreshold() < 0) {
            throw new IllegalArgumentException("Low-stock threshold cannot be negative.");
        }
    }
}
