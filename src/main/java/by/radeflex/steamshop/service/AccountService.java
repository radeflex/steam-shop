package by.radeflex.steamshop.service;

import by.radeflex.steamshop.entity.Account;
import by.radeflex.steamshop.entity.AccountStatus;
import by.radeflex.steamshop.entity.Payment;
import by.radeflex.steamshop.entity.PaymentItem;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.repository.AccountRepository;
import by.radeflex.steamshop.repository.PaymentItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final PaymentItemRepository paymentItemRepository;

    public boolean exists(Integer productId) {
        return accountRepository.existsByProductId(productId);
    }

    void reserve(Payment p) throws AccountLackException {
        getAccounts(p, AccountStatus.AVAILABLE)
                .forEach(a -> {
                    a.setStatus(AccountStatus.RESERVED);
                    accountRepository.save(a);
                });
    }

     void unreserve(Payment p) throws AccountLackException {
        getAccounts(p, AccountStatus.RESERVED)
                .forEach(a -> {
                    a.setStatus(AccountStatus.AVAILABLE);
                    accountRepository.save(a);
                });
    }

     private List<Account> getAccounts(Payment p, AccountStatus status)
     throws AccountLackException {
        var paymentItems = paymentItemRepository.findAllByPayment(p);
        int quantity = paymentItems.stream().mapToInt(PaymentItem::getQuantity).sum();
        var accounts = paymentItems.stream()
                .flatMap(item -> accountRepository.findByProductIdAndStatus(
                        item.getProduct().getId(),
                        status,
                        Limit.of(item.getQuantity())).stream())
                .toList();
        if (accounts.size() < quantity) throw new AccountLackException();
        return accounts;
    }

    Map<String, List<Account>> sellAccounts(Payment p)
    throws AccountLackException {
        return getAccounts(p, AccountStatus.RESERVED).stream()
                .peek(a -> {
                    a.setStatus(AccountStatus.SOLD);
                    accountRepository.save(a);
                })
                .collect(Collectors.groupingBy(a -> a.getProduct().getTitle()));
    }
}
