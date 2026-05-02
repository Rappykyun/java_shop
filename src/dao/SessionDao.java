package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.RoleType;
import util.DateTimeUtils;

public class SessionDao {

    public int clockIn(Connection connection, int cashierId) throws SQLException {
        String sql = "INSERT INTO cashier_sessions (cashier_id, time_in) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, cashierId);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Failed to retrieve session id.");
            }
        }
    }

    public void clockOut(Connection connection, int sessionId) throws SQLException {
        String sql = "UPDATE cashier_sessions SET time_out = ? WHERE id = ? AND time_out IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, sessionId);
            stmt.executeUpdate();
        }
    }

    public void clockOutOpenSessions(Connection connection, int cashierId) throws SQLException {
        String sql = "UPDATE cashier_sessions SET time_out = ? WHERE cashier_id = ? AND time_out IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, cashierId);
            stmt.executeUpdate();
        }
    }

    public List<Object[]> listSessions() throws SQLException {
        return listSessions(null);
    }

    public List<Object[]> listSessions(RoleType roleType) throws SQLException {
        String sql = """
                SELECT cs.id, u.full_name, cs.time_in, cs.time_out
                FROM cashier_sessions cs
                JOIN users u ON u.id = cs.cashier_id
                JOIN roles r ON r.id = u.role_id
                WHERE (? IS NULL OR r.name = ?)
                ORDER BY cs.time_in DESC
                """;
        List<Object[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            String roleName = roleType == null ? null : roleType.name();
            stmt.setString(1, roleName);
            stmt.setString(2, roleName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime timeIn = DateTimeUtils.fromTimestamp(rs.getTimestamp("time_in"));
                    LocalDateTime timeOut = DateTimeUtils.fromTimestamp(rs.getTimestamp("time_out"));
                    String duration = "-";
                    if (timeIn != null && timeOut != null) {
                        long minutes = java.time.Duration.between(timeIn, timeOut).toMinutes();
                        duration = (minutes / 60) + "h " + (minutes % 60) + "m";
                    }
                    rows.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            DateTimeUtils.format(timeIn),
                            timeOut == null ? "Still clocked in" : DateTimeUtils.format(timeOut),
                            duration
                    });
                }
            }
        }
        return rows;
    }
}
