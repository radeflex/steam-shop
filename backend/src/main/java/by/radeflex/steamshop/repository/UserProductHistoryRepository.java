package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserProductHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductHistoryRepository extends JpaRepository<UserProductHistory, Integer> {
    @EntityGraph(attributePaths = {"product"})
    Page<UserProductHistory> findByUser(User user, Pageable pageable);
}
