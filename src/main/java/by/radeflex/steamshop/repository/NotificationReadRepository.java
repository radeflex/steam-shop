package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.NotificationRead;
import by.radeflex.steamshop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Integer> {
    Page<NotificationRead> findByUser(User user, Pageable pageable);
    Optional<NotificationRead> findByUserAndNotificationId(User user, Integer id);
}
