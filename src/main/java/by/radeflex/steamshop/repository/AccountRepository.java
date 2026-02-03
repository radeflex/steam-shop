package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.Account;
import by.radeflex.steamshop.entity.AccountStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findByProductIdAndStatus(Integer productId, AccountStatus status, Limit limit);

    boolean existsByProductId(Integer productId);
}
