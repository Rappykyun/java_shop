package model;

import java.time.LocalDateTime;

public class ActivityLog {
    private final String actorName;
    private final String actionType;
    private final String entityType;
    private final String description;
    private final LocalDateTime createdAt;

    public ActivityLog(String actorName, String actionType, String entityType, String description,
            LocalDateTime createdAt) {
        this.actorName = actorName;
        this.actionType = actionType;
        this.entityType = entityType;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getActorName() {
        return actorName;
    }

    public String getActionType() {
        return actionType;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
