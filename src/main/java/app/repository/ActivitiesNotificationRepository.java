package app.repository;

import app.model.ActivitiesNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivitiesNotificationRepository extends JpaRepository<ActivitiesNotification, UUID> {
    List<ActivitiesNotification> findAllByUserIdAndIsHiddenOrderByCreatedOnDesc(UUID userId, boolean isHidden);

    List<ActivitiesNotification> findAllByUserId(UUID userId);
}
