package app.service;

import app.model.ActivitiesNotification;
import app.model.ActivityType;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.repository.ActivitiesNotificationRepository;
import app.repository.NotificationPreferenceRepository;
import app.web.dto.ActivitiesNotificationRequest;
import app.web.dto.UpsertNotificationPreference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final ActivitiesNotificationRepository activitiesNotificationRepository;
    private final MailSender mailSender;

    @Autowired
    public NotificationService(NotificationPreferenceRepository notificationPreferenceRepository, ActivitiesNotificationRepository activitiesNotificationRepository, MailSender mailSender) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.activitiesNotificationRepository = activitiesNotificationRepository;
        this.mailSender = mailSender;
    }

    public NotificationPreference upsertPreference(UpsertNotificationPreference upsertNotificationPreference) {
        Optional<NotificationPreference> optional = notificationPreferenceRepository.findByUserId(upsertNotificationPreference.getUserId());

        if(optional.isPresent()){
            NotificationPreference notificationPreference = optional.get();
            notificationPreference.setUserId(upsertNotificationPreference.getUserId());
            notificationPreference.setReservationEnabled(upsertNotificationPreference.isReservationEnabled());
            notificationPreference.setProductEnabled(upsertNotificationPreference.isProductEnabled());
            notificationPreference.setContactInfo(upsertNotificationPreference.getContactInfo());
            notificationPreference.setUpdatedOn(LocalDateTime.now());
            return notificationPreferenceRepository.save(notificationPreference);
        }

        NotificationPreference notificationPreference = NotificationPreference.builder()
                .userId(upsertNotificationPreference.getUserId())
                .reservationEnabled(upsertNotificationPreference.isReservationEnabled())
                .productEnabled(upsertNotificationPreference.isProductEnabled())
                .contactInfo(upsertNotificationPreference.getContactInfo())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        return notificationPreferenceRepository.save(notificationPreference);
    }

    public NotificationPreference getNotificationPreference(UUID userId) {
        return notificationPreferenceRepository.findByUserId(userId).orElseThrow(() -> new NullPointerException("Notification preference for user with id "+userId+" does not exist!"));
    }

    public ActivitiesNotification sendNotification(ActivitiesNotificationRequest request){
        UUID userId = request.getUserId();
        NotificationPreference notificationPreference = getNotificationPreference(userId);

        if(!notificationPreference.isProductEnabled() && !notificationPreference.isReservationEnabled()){
            throw new IllegalArgumentException("User with id "+userId+" does not allow notifications!");
            //return null;
        }

        if(request.getType() == ActivityType.RESERVATION && !notificationPreference.isReservationEnabled()){
            //return null;
            throw new IllegalArgumentException("User with id "+userId+" does not allow notifications for reservations!");
        }

        if(request.getType() == ActivityType.BUYING_PRODUCT && !notificationPreference.isProductEnabled()){
            throw new IllegalArgumentException("User with id "+userId+" does not allow notifications for buying products!");
            //return null;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notificationPreference.getContactInfo());
        message.setSubject(request.getSubject());
        message.setText(request.getMessage());

        ActivitiesNotification notification = ActivitiesNotification.builder()
                .userId(userId)
                .type(request.getType())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(NotificationStatus.SUCCEEDED)
                .createdOn(LocalDateTime.now())
                .isHidden(false)
                .build();

        try {
            mailSender.send(message);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("There was an issue sending email to "+notificationPreference.getContactInfo()+" due to "+e.getMessage()+"!");
        }
        return activitiesNotificationRepository.save(notification);
    }

    public List<ActivitiesNotification> getHistory(UUID userId) {
        return this.activitiesNotificationRepository.findAllByUserIdAndIsHiddenOrderByCreatedOnDesc(userId, false);
    }

    public void setAllNotificationsIsHiddenToTrue(UUID userId) {
        for(ActivitiesNotification notification : activitiesNotificationRepository.findAllByUserId(userId)){
            notification.setHidden(true);
            activitiesNotificationRepository.save(notification);
        }
    }

    public void setAllNotificationsIsHiddenToFalse(UUID userId) {
        for(ActivitiesNotification notification : activitiesNotificationRepository.findAllByUserId(userId)){
            notification.setHidden(false);
            activitiesNotificationRepository.save(notification);
        }
    }
}
