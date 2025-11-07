package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>,
        QuerydslPredicateExecutor<Product> {
    Optional<User> findByUsername(String username);
}
