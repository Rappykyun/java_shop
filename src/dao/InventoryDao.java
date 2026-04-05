package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.InventoryMovement;
import util.DateTimeUtils;

public class InventoryDao {
    public void insertMovement(Connection connection, int productId, String movementType, int quantityChange,
            int previousQuantity, int newQuantity, String note, Integer performedBy) throws SQLException {
        String sql = """
                INSERT INTO inventory_movements
                (product_id, movement_type, quantity_change, previous_quantity, new_quantity, note, performed_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setString(2, movementType);
            statement.setInt(3, quantityChange);
            statement.setInt(4, previousQuantity);
            statement.setInt(5, newQuantity);
            statement.setString(6, note);
            if (performedBy == null) {
                statement.setNull(7, java.sql.Types.INTEGER);
            } else {
                statement.setInt(7, performedBy);
            }
            statement.executeUpdate();
        }
    }

    public List<InventoryMovement> listRecentMovements() throws SQLException {
        String sql = """
                SELECT im.id, im.product_id, p.name AS product_name, im.movement_type, im.quantity_change,
                       im.previous_quantity, im.new_quantity, im.note, u.full_name AS performed_by, im.created_at
                FROM inventory_movements im
                JOIN products p ON p.id = im.product_id
                LEFT JOIN users u ON u.id = im.performed_by
                ORDER BY im.created_at DESC
                LIMIT 100
                """;
        List<InventoryMovement> movements = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                movements.add(new InventoryMovement(
                        resultSet.getInt("id"),
                        resultSet.getInt("product_id"),
                        resultSet.getString("product_name"),
                        resultSet.getString("movement_type"),
                        resultSet.getInt("quantity_change"),
                        resultSet.getInt("previous_quantity"),
                        resultSet.getInt("new_quantity"),
                        resultSet.getString("note"),
                        resultSet.getString("performed_by"),
                        DateTimeUtils.fromTimestamp(resultSet.getTimestamp("created_at"))));
            }
        }
        return movements;
    }
}
