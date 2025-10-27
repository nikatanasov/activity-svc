package app;

import app.model.ActivitiesNotification;
import app.model.ActivityType;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.repository.ActivitiesNotificationRepository;
import app.repository.NotificationPreferenceRepository;
import app.service.NotificationService;
import app.web.dto.ActivitiesNotificationRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class SendNotificationITest {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Autowired
    private ActivitiesNotificationRepository activitiesNotificationRepository;

    @Mock
    private MailSender mailSender;

    @Test
    void testSendNotification_happyPath(){
        UUID userId = UUID.randomUUID();

        ActivitiesNotificationRequest activitiesNotificationRequest = ActivitiesNotificationRequest.builder()
                .userId(userId)
                .subject("Water")
                .message("1.00 lv")
                .type(ActivityType.BUYING_PRODUCT)
                .build();

        NotificationPreference notificationPreference = NotificationPreference.builder()
                .userId(userId)
                .reservationEnabled(true)
                .productEnabled(true)
                .contactInfo("viktor@gmail.com")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        notificationPreferenceRepository.save(notificationPreference);

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        ActivitiesNotification activitiesNotification = notificationService.sendNotification(activitiesNotificationRequest);

        assertEquals(activitiesNotification.getStatus(), NotificationStatus.SUCCEEDED);
        List<ActivitiesNotification> notifications = activitiesNotificationRepository.findAllByUserIdAndIsHiddenOrderByCreatedOnDesc(userId, false);
        assertThat(notifications).hasSize(1);
    }
}
