package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ActivityLogDao {
    public void log(Connection connection, Integer actorUserId, String actionType, String entityType, Integer entityId,
            String description) throws SQLException {
        String sql = """
                INSERT INTO activity_logs (actor_user_id, action_type, entity_type, entity_id, description)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (actorUserId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, actorUserId);
            }
            statement.setString(2, actionType);
            statement.setString(3, entityType);
            if (entityId == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, entityId);
            }
            statement.setString(5, description);
            statement.executeUpdate();
        }
    }
}
