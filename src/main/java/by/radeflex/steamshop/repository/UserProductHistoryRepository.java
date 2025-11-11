package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.UserProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductHistoryRepository extends JpaRepository<UserProductHistory, Integer> {
}
