package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.EmailConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, Integer> {
    Optional<EmailConfirmation> findByToken(UUID token);

    Optional<EmailConfirmation> findByUserId(Integer userId);
}
