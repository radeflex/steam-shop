package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct, Integer> {
    Page<UserProduct> findPageByUser(User user, Pageable pageable);
    List<UserProduct> findAllByUser(User user);

    void deleteAllByUser(User user);
}
