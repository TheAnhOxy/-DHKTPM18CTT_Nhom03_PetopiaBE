package com.pet.repository;

import com.pet.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    @Query("SELECT n.notificationId FROM Notification n ORDER BY n.notificationId DESC LIMIT 1")
    Optional<String> findLastNotificationId();
}