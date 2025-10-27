package app.web.mapper;

import app.model.ActivitiesNotification;
import app.model.NotificationPreference;
import app.web.dto.ActivitiesNotificationResponse;
import app.web.dto.NotificationPreferenceResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static NotificationPreferenceResponse fromNotificationPreference(NotificationPreference notificationPreference){
        return NotificationPreferenceResponse.builder()
                .id(notificationPreference.getId())
                .userId(notificationPreference.getUserId())
                .reservationEnabled(notificationPreference.isReservationEnabled())
                .productEnabled(notificationPreference.isProductEnabled())
                .contactInfo(notificationPreference.getContactInfo())
                .build();
    }

    public static ActivitiesNotificationResponse fromActivitiesNotification(ActivitiesNotification activitiesNotification){
        return ActivitiesNotificationResponse.builder()
                .subject(activitiesNotification.getSubject())
                .type(activitiesNotification.getType())
                .status(activitiesNotification.getStatus())
                .createdOn(activitiesNotification.getCreatedOn())
                .build();
    }
}
