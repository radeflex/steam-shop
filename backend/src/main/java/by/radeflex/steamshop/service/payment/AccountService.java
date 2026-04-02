package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.dto.AccountReadDto;
import by.radeflex.steamshop.dto.response.CsvResponse;
import by.radeflex.steamshop.entity.Account;
import by.radeflex.steamshop.entity.AccountStatus;
import by.radeflex.steamshop.entity.Payment;
import by.radeflex.steamshop.entity.PaymentItem;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.mapper.AccountMapper;
import by.radeflex.steamshop.repository.AccountRepository;
import by.radeflex.steamshop.repository.PaymentItemRepository;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import by.radeflex.steamshop.utils.CsvUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
    private final CurrentUserService currentUserService;

    public boolean exists(Integer productId) {
        return accountRepository.existsByProductId(productId);
    }

    private void changeStatus(Payment p, AccountStatus f, AccountStatus t) {
        getAccounts(p, f)
                .forEach(a -> {
                    a.setStatus(t);
                    accountRepository.save(a);
                });
    }

    void reserve(Payment p) throws AccountLackException {
        changeStatus(p, AccountStatus.AVAILABLE, AccountStatus.RESERVED);
    }

     void unreserve(Payment p) throws AccountLackException {
        changeStatus(p, AccountStatus.RESERVED, AccountStatus.AVAILABLE);
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

    @Caching(evict = {
            @CacheEvict(
                    value = "products",
                    allEntries = true,
                    condition = "#result.isPresent()"),
            @CacheEvict(
                    value = "cart",
                    allEntries = true,
                    condition = "#result.isPresent()")})
    @Transactional
    public Optional<AccountReadDto> create(AccountCreateDto accountCreateDto) {
        var user = currentUserService.getCurrentUserEntity();
        return productRepository.findById(accountCreateDto.productId())
                .map(p -> accountMapper.mapFrom(accountCreateDto, user))
                .map(accountRepository::save)
                .map(accountMapper::mapFrom);
    }

    @Caching(evict = {
            @CacheEvict(
                    value = "products",
                    allEntries = true,
                    condition = "#result != null"),
            @CacheEvict(
                    value = "cart",
                    allEntries = true,
                    condition = "#result != null"
            )})
    public CsvResponse readCsv(MultipartFile file) {
        var user = currentUserService.getCurrentUserEntity();
        int inserted = 0;
        List<Integer> errorRows = new ArrayList<>();
        var accounts = CsvUtils.readAccounts(file, ';').stream()
                .map(a -> accountMapper.mapFrom(a, user)).toList();
        for (int i = 0; i < accounts.size(); ++i) {
            try {
                accountRepository.save(accounts.get(i));
                inserted++;
            } catch (DataIntegrityViolationException e) {
                errorRows.add(i + 1);
            }
        }
        return new CsvResponse(accounts.size(), inserted, errorRows);
    }

    @Transactional(readOnly = true)
    public Page<AccountReadDto> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(accountMapper::mapFrom);
    }
}
