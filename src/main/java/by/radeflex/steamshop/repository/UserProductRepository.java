package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct, Integer> {
    Page<UserProduct> findAllByUser(User user, Pageable pageable);
}
