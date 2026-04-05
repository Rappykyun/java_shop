package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.RoleType;
import model.User;
import util.DateTimeUtils;

public class UserDao {
    public User findByUsername(String username) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findByUsername(connection, username);
        }
    }

    public User findByUsername(Connection connection, String username) throws SQLException {
        String sql = """
                SELECT u.id, u.username, u.full_name, u.password_hash, u.password_salt,
                       r.name AS role_name, u.active, u.created_at
                FROM users u
                JOIN roles r ON r.id = u.role_id
                WHERE u.username = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    public User findById(Connection connection, int id) throws SQLException {
        String sql = """
                SELECT u.id, u.username, u.full_name, u.password_hash, u.password_salt,
                       r.name AS role_name, u.active, u.created_at
                FROM users u
                JOIN roles r ON r.id = u.role_id
                WHERE u.id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    public List<User> listCashiers() throws SQLException {
        String sql = """
                SELECT u.id, u.username, u.full_name, u.password_hash, u.password_salt,
                       r.name AS role_name, u.active, u.created_at
                FROM users u
                JOIN roles r ON r.id = u.role_id
                WHERE r.name = 'CASHIER'
                ORDER BY u.created_at DESC
                """;
        List<User> users = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }
        return users;
    }

    public int insert(Connection connection, User user, int roleId) throws SQLException {
        String sql = """
                INSERT INTO users (username, full_name, password_hash, password_salt, role_id, active)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFullName());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getPasswordSalt());
            statement.setInt(5, roleId);
            statement.setBoolean(6, user.isActive());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Unable to read generated user id.");
            }
        }
    }

    public void updateCashier(Connection connection, User user, boolean updatePassword) throws SQLException {
        String sql = updatePassword
                ? "UPDATE users SET username = ?, full_name = ?, password_hash = ?, password_salt = ?, active = ? WHERE id = ?"
                : "UPDATE users SET username = ?, full_name = ?, active = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getFullName());
            if (updatePassword) {
                statement.setString(3, user.getPasswordHash());
                statement.setString(4, user.getPasswordSalt());
                statement.setBoolean(5, user.isActive());
                statement.setInt(6, user.getId());
            } else {
                statement.setBoolean(3, user.isActive());
                statement.setInt(4, user.getId());
            }
            statement.executeUpdate();
        }
    }

    public boolean hasAnyAdmin(Connection connection) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM users u
                JOIN roles r ON r.id = u.role_id
                WHERE r.name = 'ADMIN'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1) > 0;
        }
    }

    public int countActiveCashiers() throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM users u
                JOIN roles r ON r.id = u.role_id
                WHERE r.name = 'CASHIER' AND u.active = TRUE
                """;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("full_name"),
                resultSet.getString("password_hash"),
                resultSet.getString("password_salt"),
                RoleType.valueOf(resultSet.getString("role_name")),
                resultSet.getBoolean("active"),
                DateTimeUtils.fromTimestamp(resultSet.getTimestamp("created_at")));
    }
}
