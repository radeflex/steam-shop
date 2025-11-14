package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import java.util.List;
public interface AccountRepository extends JpaRepository<Account, Integer> {
    @NativeQuery("SELECT * FROM account a " +
            "WHERE a.product_id = :productId " +
            "AND available = true LIMIT :quantity")
    List<Account> findAccounts(Integer productId, Integer quantity);
}
