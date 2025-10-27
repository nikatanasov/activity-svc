package app.web.dto;

import app.model.ActivityType;
import app.model.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivitiesNotificationResponse {
    private String subject;

    private ActivityType type;

    private NotificationStatus status;

    private LocalDateTime createdOn;
}
