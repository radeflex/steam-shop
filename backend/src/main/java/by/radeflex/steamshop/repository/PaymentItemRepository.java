package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.Payment;
import by.radeflex.steamshop.entity.PaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentItemRepository extends JpaRepository<PaymentItem, Integer> {
    List<PaymentItem> findAllByPayment(Payment payment);
}
