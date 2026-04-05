package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.DBConnection;
import model.ActivityLog;
import model.DashboardSummary;
import util.DateTimeUtils;

public class DashboardDao {
    public DashboardSummary loadSummary(int lowStockCount, int activeCashiers) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            BigDecimal todaySales = BigDecimal.ZERO;
            int todayTransactions = 0;

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COALESCE(SUM(total), 0), COUNT(*) FROM sales_transactions WHERE DATE(created_at) = CURDATE()");
                    ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    todaySales = resultSet.getBigDecimal(1);
                    todayTransactions = resultSet.getInt(2);
                }
            }

            List<ActivityLog> recentActivities = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT COALESCE(u.full_name, 'System') AS actor_name, action_type, entity_type, description,
                           al.created_at AS activity_created_at
                    FROM activity_logs al
                    LEFT JOIN users u ON u.id = al.actor_user_id
                    ORDER BY al.created_at DESC
                    LIMIT 12
                    """);
                    ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    recentActivities.add(new ActivityLog(
                            resultSet.getString("actor_name"),
                            resultSet.getString("action_type"),
                            resultSet.getString("entity_type"),
                            resultSet.getString("description"),
                            DateTimeUtils.fromTimestamp(resultSet.getTimestamp("activity_created_at"))));
                }
            }

            return new DashboardSummary(todaySales, todayTransactions, lowStockCount, activeCashiers, recentActivities);
        }
    }
}
