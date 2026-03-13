package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserProduct;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct, Integer> {
    @Query("""
        select up as userProduct,
            count(a) >= up.quantity as isEnough from UserProduct up
        left join Account a on up.product = a.product and a.status = 'AVAILABLE'
        join fetch up.product p
        where up.user = :user
        group by up, p
    """)
    Page<Tuple> findPageByUser(User user, Pageable pageable);

    @Query("""
        select up from UserProduct up
        join Account a on up.product = a.product and a.status = 'AVAILABLE'
        join fetch up.product p
        where up.user = :user
        group by up, p
        having count(a) >= up.quantity
    """)
    List<UserProduct> findAvailableByUser(User user);

    void deleteAllByUser(User user);

    @Query("""
        select count(a) >= :quantity
        from Account a
        where a.status = 'AVAILABLE' and a.product = :product
        group by a
    """)
    boolean hasEnoughAccounts(Product product, Integer quantity);
}
