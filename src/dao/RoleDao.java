package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.RoleType;

public class RoleDao {
    public Integer findRoleId(Connection connection, RoleType roleType) throws SQLException {
        String sql = "SELECT id FROM roles WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roleType.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
                return null;
            }
        }
    }
}
