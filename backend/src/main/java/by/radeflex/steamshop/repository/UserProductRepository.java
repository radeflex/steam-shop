package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct, Integer> {
    @EntityGraph(attributePaths = "product")
    Page<UserProduct> findPageByUser(User user, Pageable pageable);
    @EntityGraph(attributePaths = "product")
    List<UserProduct> findAllByUser(User user);

    void deleteAllByUser(User user);

    @Override
    @EntityGraph(attributePaths = "product")
    Optional<UserProduct> findById(Integer integer);
}
