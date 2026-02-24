package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.Notification;
import by.radeflex.steamshop.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Integer> {
    void deleteByNotification(Notification notification);

    boolean existsByNotification(Notification notification);
}
