package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    @NativeQuery("""
    WITH items AS (
        SELECT product_id, quantity
        FROM payment_item
        WHERE payment_id = :paymentId
    ), ranked AS (
            SELECT a.*,
                   row_number() OVER (
                       PARTITION BY product_id ORDER BY id) as rn
            FROM account a
            WHERE status = :status
        )
    SELECT r.id, username, password, email, email_password, r.product_id, status, created_at, created_by FROM ranked r
    JOIN items i ON r.product_id = i.product_id
    WHERE r.rn <= i.quantity FOR UPDATE SKIP LOCKED
    """)
    List<Account> findByStatus(UUID paymentId, String status);

    boolean existsByProductId(Integer productId);
}
