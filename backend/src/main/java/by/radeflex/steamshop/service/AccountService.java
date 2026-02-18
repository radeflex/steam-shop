package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.dto.AccountReadDto;
import by.radeflex.steamshop.dto.CsvResponseDto;
import by.radeflex.steamshop.entity.*;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.mapper.AccountMapper;
import by.radeflex.steamshop.repository.AccountRepository;
import by.radeflex.steamshop.repository.PaymentItemRepository;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.utils.CsvUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final ProductRepository productRepository;
    private final AccountMapper accountMapper;

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

    @Transactional
    public Optional<AccountReadDto> create(AccountCreateDto accountCreateDto) {
        return productRepository.findById(accountCreateDto.productId())
                .map(p -> accountMapper.mapFrom(accountCreateDto))
                .map(accountRepository::save)
                .map(accountMapper::mapFrom);
    }

    public CsvResponseDto readCsv(MultipartFile file) {
        int inserted = 0;
        List<Integer> errorRows = new ArrayList<>();
        var accounts = CsvUtils.readAccounts(file, ';').stream()
                .map(accountMapper::mapFrom).toList();
        for (int i = 0; i < accounts.size(); ++i) {
            try {
                accountRepository.save(accounts.get(i));
                inserted++;
            } catch (DataIntegrityViolationException e) {
                errorRows.add(i + 1);
            }
        }
        return new CsvResponseDto(accounts.size(), inserted, errorRows);
    }

    @Transactional(readOnly = true)
    public Page<AccountReadDto> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(accountMapper::mapFrom);
    }
}
