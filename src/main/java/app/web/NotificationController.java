package app.web;

import app.model.ActivitiesNotification;
import app.model.NotificationPreference;
import app.service.NotificationService;
import app.web.dto.ActivitiesNotificationRequest;
import app.web.dto.ActivitiesNotificationResponse;
import app.web.dto.NotificationPreferenceResponse;
import app.web.dto.UpsertNotificationPreference;
import app.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> upsertNotificationPreference(@RequestBody UpsertNotificationPreference upsertNotificationPreference){
        NotificationPreference notificationPreference = notificationService.upsertPreference(upsertNotificationPreference);
        //entity -> dto
        NotificationPreferenceResponse notificationPreferenceResponse = DtoMapper.fromNotificationPreference(notificationPreference);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificationPreferenceResponse);
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getUserNotificationPreference(@RequestParam(name = "userId") UUID userId){
        NotificationPreference notificationPreference = notificationService.getNotificationPreference(userId);
        NotificationPreferenceResponse notificationPreferenceResponse = DtoMapper.fromNotificationPreference(notificationPreference);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationPreferenceResponse);
    }

    @PostMapping
    public ResponseEntity<ActivitiesNotificationResponse> sendNotification(@RequestBody ActivitiesNotificationRequest activitiesNotificationRequest){
        ActivitiesNotification activitiesNotification = notificationService.sendNotification(activitiesNotificationRequest);
        ActivitiesNotificationResponse activitiesNotificationResponse = DtoMapper.fromActivitiesNotification(activitiesNotification);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(activitiesNotificationResponse);
    }

    @GetMapping
    public ResponseEntity<List<ActivitiesNotificationResponse>> getActivitiesNotificationHistory(@RequestParam(name = "userId") UUID userId){
        List<ActivitiesNotificationResponse> notificationHistory = notificationService.getHistory(userId).stream().map(DtoMapper::fromActivitiesNotification).collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notificationHistory);
    }

    @PutMapping("/clear")
    public ResponseEntity<Void> clearAllNotifications(@RequestParam(name = "userId") UUID userId){
        notificationService.setAllNotificationsIsHiddenToTrue(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }

    @PutMapping("/show")
    public ResponseEntity<Void> showAllNotifications(@RequestParam(name = "userId") UUID userId){
        notificationService.setAllNotificationsIsHiddenToFalse(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }

}
