package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query("""
            select n from Notification n
            left join NotificationRead nr
                on n = nr.notification
                and nr.user.id = :userId
            where nr.id is null and (n.user.id = :userId or n.user is null)
            """)
    Page<Notification> findAllUnread(Integer userId, Pageable pageable);
    Page<Notification> findAllByUserIdOrUserIdNull(Integer userId, Pageable pageable);
}
