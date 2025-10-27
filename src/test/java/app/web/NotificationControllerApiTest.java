package app.web;

import app.model.ActivitiesNotification;
import app.model.ActivityType;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.service.NotificationService;
import app.web.dto.ActivitiesNotificationRequest;
import app.web.dto.ActivitiesNotificationResponse;
import app.web.dto.UpsertNotificationPreference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToGetUserNotificationPreference_ShouldReturnCorrectData() throws Exception {
        when(notificationService.getNotificationPreference(any())).thenReturn(NotificationPreference.builder()
                        .id(UUID.randomUUID())
                        .userId(UUID.randomUUID())
                        .reservationEnabled(true)
                        .productEnabled(true)
                        .contactInfo("viktor@gmail.com")
                        .createdOn(LocalDateTime.now().minusDays(1))
                        .updatedOn(LocalDateTime.now().minusDays(1))
                        .build());

        MockHttpServletRequestBuilder request = get("/api/v1/notifications/preferences")
                .param("userId", UUID.randomUUID().toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("userId").isNotEmpty())
                .andExpect(jsonPath("reservationEnabled").isNotEmpty())
                .andExpect(jsonPath("productEnabled").isNotEmpty())
                .andExpect(jsonPath("contactInfo").isNotEmpty());
    }

    @Test
    void postRequestToUpsertNotificationPreference_happyPath() throws Exception {
        UpsertNotificationPreference upsertNotificationPreference = UpsertNotificationPreference.builder()
                .userId(UUID.randomUUID())
                .reservationEnabled(true)
                .productEnabled(true)
                .contactInfo("viktor@gmail.com")
                .build();

        when(notificationService.upsertPreference(any())).thenReturn(NotificationPreference.builder()
                        .id(UUID.randomUUID())
                        .userId(UUID.randomUUID())
                        .reservationEnabled(true)
                        .productEnabled(true)
                        .contactInfo("viktor@gmail.com")
                        .createdOn(LocalDateTime.now())
                        .updatedOn(LocalDateTime.now())
                        .build());

        MockHttpServletRequestBuilder request = post("/api/v1/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(upsertNotificationPreference));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("userId").isNotEmpty())
                .andExpect(jsonPath("reservationEnabled").isNotEmpty())
                .andExpect(jsonPath("productEnabled").isNotEmpty())
                .andExpect(jsonPath("contactInfo").isNotEmpty());
    }

    @Test
    void postRequestToSendNotification_happyPath() throws Exception {
        ActivitiesNotificationRequest activitiesNotificationRequest = ActivitiesNotificationRequest.builder()
                .userId(UUID.randomUUID())
                .subject("Buying product")
                .message("Water for 1 lv.")
                .type(ActivityType.BUYING_PRODUCT)
                .build();

        when(notificationService.sendNotification(activitiesNotificationRequest))
                .thenReturn(ActivitiesNotification.builder()
                        .id(UUID.randomUUID())
                        .userId(UUID.randomUUID())
                        .type(ActivityType.BUYING_PRODUCT)
                        .subject(activitiesNotificationRequest.getSubject())
                        .message(activitiesNotificationRequest.getMessage())
                        .status(NotificationStatus.SUCCEEDED)
                        .createdOn(LocalDateTime.now())
                        .isHidden(false)
                        .build());

        MockHttpServletRequestBuilder request = post("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(activitiesNotificationRequest));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("subject").isNotEmpty())
                .andExpect(jsonPath("type").isNotEmpty())
                .andExpect(jsonPath("status").isNotEmpty())
                .andExpect(jsonPath("createdOn").isNotEmpty());
    }

    @Test
    void getRequestToGetActivitiesNotificationHistory_happyPath() throws Exception {
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

        when(notificationService.getHistory(any())).thenReturn(List.of(notification1, notification2));

        MockHttpServletRequestBuilder request = get("/api/v1/notifications")
                .param("userId", UUID.randomUUID().toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].subject").isNotEmpty())
                .andExpect(jsonPath("$[0].type").isNotEmpty())
                .andExpect(jsonPath("$[0].status").isNotEmpty())
                .andExpect(jsonPath("$[0].createdOn").isNotEmpty())
                .andExpect(jsonPath("$[1].subject").isNotEmpty())
                .andExpect(jsonPath("$[1].type").isNotEmpty())
                .andExpect(jsonPath("$[1].status").isNotEmpty())
                .andExpect(jsonPath("$[1].createdOn").isNotEmpty());
    }

    @Test
    void putRequestToClearAllNotifications_happyPath() throws Exception {

        MockHttpServletRequestBuilder request = put("/api/v1/notifications/clear")
                .param("userId", UUID.randomUUID().toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(""));

    }

    @Test
    void putRequestToShowAllNotifications_happyPath() throws Exception {

        MockHttpServletRequestBuilder request = put("/api/v1/notifications/show")
                .param("userId", UUID.randomUUID().toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(""));

    }
}
