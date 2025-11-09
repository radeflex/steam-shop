package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.UserProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductRepository extends JpaRepository<UserProduct, Integer> {
}
