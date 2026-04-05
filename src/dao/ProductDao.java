package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.Product;
import util.DateTimeUtils;

public class ProductDao {
    public List<Product> listAll(String search) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT id, sku, name, category, description, price, stock_quantity, low_stock_threshold, active, updated_at
                FROM products
                """);
        boolean hasSearch = search != null && !search.isBlank();
        if (hasSearch) {
            sql.append(" WHERE name LIKE ? OR category LIKE ? OR sku LIKE ? ");
        }
        sql.append(" ORDER BY active DESC, name ASC ");

        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            if (hasSearch) {
                String pattern = "%" + search.trim() + "%";
                statement.setString(1, pattern);
                statement.setString(2, pattern);
                statement.setString(3, pattern);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }
        }
        return products;
    }

    public List<Product> listAvailableForSale(String search, String sortBy) throws SQLException {
        String orderBy = switch (sortBy == null ? "" : sortBy) {
            case "price" -> "price ASC, name ASC";
            case "stock" -> "stock_quantity DESC, name ASC";
            default -> "name ASC";
        };
        String sql = """
                SELECT id, sku, name, category, description, price, stock_quantity, low_stock_threshold, active, updated_at
                FROM products
                WHERE active = TRUE AND stock_quantity > 0
                  AND (? = '' OR name LIKE ? OR category LIKE ? OR sku LIKE ?)
                ORDER BY
                """ + orderBy;
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String trimmed = search == null ? "" : search.trim();
            String pattern = "%" + trimmed + "%";
            statement.setString(1, trimmed);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }
        }
        return products;
    }

    public Product findById(Connection connection, int id) throws SQLException {
        String sql = """
                SELECT id, sku, name, category, description, price, stock_quantity, low_stock_threshold, active, updated_at
                FROM products
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapProduct(resultSet) : null;
            }
        }
    }

    public Product findByIdForUpdate(Connection connection, int id) throws SQLException {
        String sql = """
                SELECT id, sku, name, category, description, price, stock_quantity, low_stock_threshold, active, updated_at
                FROM products
                WHERE id = ?
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapProduct(resultSet) : null;
            }
        }
    }

    public int insert(Connection connection, Product product) throws SQLException {
        String sql = """
                INSERT INTO products (sku, name, category, description, price, stock_quantity, low_stock_threshold, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bindProduct(statement, product);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Unable to read generated product id.");
            }
        }
    }

    public void update(Connection connection, Product product) throws SQLException {
        String sql = """
                UPDATE products
                SET sku = ?, name = ?, category = ?, description = ?, price = ?, stock_quantity = ?, low_stock_threshold = ?, active = ?
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindProduct(statement, product);
            statement.setInt(9, product.getId());
            statement.executeUpdate();
        }
    }

    public void updateStock(Connection connection, int productId, int newQuantity) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE products SET stock_quantity = ? WHERE id = ?")) {
            statement.setInt(1, newQuantity);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
    }

    public List<Product> listLowStock() throws SQLException {
        String sql = """
                SELECT id, sku, name, category, description, price, stock_quantity, low_stock_threshold, active, updated_at
                FROM products
                WHERE active = TRUE AND stock_quantity <= low_stock_threshold
                ORDER BY stock_quantity ASC, name ASC
                """;
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(mapProduct(resultSet));
            }
        }
        return products;
    }

    public int countLowStock() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE active = TRUE AND stock_quantity <= low_stock_threshold";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private void bindProduct(PreparedStatement statement, Product product) throws SQLException {
        statement.setString(1, product.getSku());
        statement.setString(2, product.getName());
        statement.setString(3, product.getCategory());
        statement.setString(4, product.getDescription());
        statement.setBigDecimal(5, product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());
        statement.setInt(6, product.getStockQuantity());
        statement.setInt(7, product.getLowStockThreshold());
        statement.setBoolean(8, product.isActive());
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("id"),
                resultSet.getString("sku"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("stock_quantity"),
                resultSet.getInt("low_stock_threshold"),
                resultSet.getBoolean("active"),
                DateTimeUtils.fromTimestamp(resultSet.getTimestamp("updated_at")));
    }
}
