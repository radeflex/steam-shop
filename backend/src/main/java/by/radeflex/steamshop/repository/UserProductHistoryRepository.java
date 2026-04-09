package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserProductHistory;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductHistoryRepository extends JpaRepository<UserProductHistory, Integer> {
    @Query("""
    select  uph.title as title,
            uph.price as price,
            uph.product.id as productId,
            sum(uph.quantity) as quantity,
            uph.user.id as userId
    from UserProductHistory uph
    where uph.user = :user
    group by uph.product.id, uph.title, uph.user.id, uph.price
    """)
    Page<Tuple> findGroupedByUser(User user, Pageable pageable);
}
