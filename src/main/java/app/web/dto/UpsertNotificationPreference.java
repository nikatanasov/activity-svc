package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpsertNotificationPreference {//имам нужда потребителя да ми изпрати тази информация за да мога да създам NotificationPreference

    @NotNull
    private UUID userId;

    private boolean reservationEnabled;

    private boolean productEnabled;

    @NotNull
    @NotBlank
    private String contactInfo;
}
