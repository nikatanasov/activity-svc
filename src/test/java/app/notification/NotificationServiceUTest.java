package app.notification;

import app.model.ActivitiesNotification;
import app.model.ActivityType;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.repository.ActivitiesNotificationRepository;
import app.repository.NotificationPreferenceRepository;
import app.service.NotificationService;
import app.web.dto.UpsertNotificationPreference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {
    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private ActivitiesNotificationRepository activitiesNotificationRepository;

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void upsertPreference_happyPath(){
        UpsertNotificationPreference upsertNotificationPreference = UpsertNotificationPreference.builder()
                .userId(UUID.randomUUID())
                .reservationEnabled(true)
                .productEnabled(true)
                .contactInfo("viktor@gmail.com")
                .build();

        when(notificationPreferenceRepository.findByUserId(any())).thenReturn(Optional.empty());
        when(notificationPreferenceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPreference preference = notificationService.upsertPreference(upsertNotificationPreference);
        assertEquals(upsertNotificationPreference.getUserId(), preference.getUserId());
        assertEquals(upsertNotificationPreference.isReservationEnabled(), preference.isReservationEnabled());
        assertEquals(upsertNotificationPreference.isProductEnabled(), preference.isProductEnabled());
        assertEquals(upsertNotificationPreference.getContactInfo(), preference.getContactInfo());
        verify(notificationPreferenceRepository, times(1)).findByUserId(any());
        verify(notificationPreferenceRepository, times(1)).save(any());
    }

    @Test
    void upsertPreference_withPreferenceExisting(){
        UUID userId = UUID.randomUUID();

        UpsertNotificationPreference upsertNotificationPreference = UpsertNotificationPreference.builder()
                .userId(userId)
                .reservationEnabled(true)
                .productEnabled(true)
                .contactInfo("viktor@gmail.com")
                .build();

        NotificationPreference notificationPreference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .reservationEnabled(true)
                .productEnabled(true)
                .contactInfo("viktor@gmail.com")
                .createdOn(LocalDateTime.now().minusDays(1))
                .updatedOn(LocalDateTime.now().minusDays(1))
                .build();

        when(notificationPreferenceRepository.findByUserId(any())).thenReturn(Optional.of(notificationPreference));
        when(notificationPreferenceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPreference preference = notificationService.upsertPreference(upsertNotificationPreference);
        assertEquals(upsertNotificationPreference.getUserId(), preference.getUserId());
        assertEquals(upsertNotificationPreference.isReservationEnabled(), preference.isReservationEnabled());
        assertEquals(upsertNotificationPreference.isProductEnabled(), preference.isProductEnabled());
        assertEquals(upsertNotificationPreference.getContactInfo(), preference.getContactInfo());
        assertTrue(preference.getUpdatedOn().isAfter(preference.getCreatedOn()));

        verify(notificationPreferenceRepository, times(1)).findByUserId(any());
        verify(notificationPreferenceRepository, times(1)).save(any());
    }

    @Test
    void setAllNotificationsIsHiddenToTrue_happyPath(){
        ActivitiesNotification notification1 = ActivitiesNotification.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type(ActivityType.BUYING_PRODUCT)
                .subject("Buying product")
                .status(NotificationStatus.SUCCEEDED)
                .createdOn(LocalDateTime.now())
                .build();

        ActivitiesNotification notification2 = ActivitiesNotification.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type(ActivityType.RESERVATION)
                .subject("Creating reservation")
                .status(NotificationStatus.SUCCEEDED)
                .createdOn(LocalDateTime.now())
                .build();

        when(activitiesNotificationRepository.findAllByUserId(any())).thenReturn(List.of(notification1, notification2));
        notificationService.setAllNotificationsIsHiddenToTrue(any());
        assertTrue(notification1.isHidden());
        assertTrue(notification2.isHidden());

        verify(activitiesNotificationRepository, times(2)).save(any());
    }

    @Test
    void setAllNotificationsIsHiddenToFalse_happyPath(){
        ActivitiesNotification notification1 = ActivitiesNotification.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type(ActivityType.BUYING_PRODUCT)
                .subject("Buying product")
                .status(NotificationStatus.SUCCEEDED)
                .createdOn(LocalDateTime.now())
                .build();

        ActivitiesNotification notification2 = ActivitiesNotification.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type(ActivityType.RESERVATION)
                .subject("Creating reservation")
                .status(NotificationStatus.SUCCEEDED)
                .createdOn(LocalDateTime.now())
                .build();

        when(activitiesNotificationRepository.findAllByUserId(any())).thenReturn(List.of(notification1, notification2));
        notificationService.setAllNotificationsIsHiddenToFalse(any());
        assertFalse(notification1.isHidden());
        assertFalse(notification2.isHidden());

        verify(activitiesNotificationRepository, times(2)).save(any());
    }

    @Test
    void getNotificationPreference_happyPath(){
        UUID userId = UUID.randomUUID();
        NotificationPreference notificationPreference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .reservationEnabled(true)
                .productEnabled(true)
                .build();

        when(notificationPreferenceRepository.findByUserId(any())).thenReturn(Optional.of(notificationPreference));
        NotificationPreference preference = notificationService.getNotificationPreference(userId);
        assertEquals(notificationPreference.getContactInfo(), preference.getContactInfo());
        assertEquals(notificationPreference.isReservationEnabled(), preference.isReservationEnabled());
        verify(notificationPreferenceRepository, times(1)).findByUserId(any());
    }
}
