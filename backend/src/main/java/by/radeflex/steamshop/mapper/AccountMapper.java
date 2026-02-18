package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.dto.AccountReadDto;
import by.radeflex.steamshop.entity.Account;
import by.radeflex.steamshop.entity.AccountStatus;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountMapper {
    private Account buildAccount(Account account, AccountCreateDto accountCreateDto) {
        account.setUsername(accountCreateDto.username());
        account.setPassword(accountCreateDto.password());
        account.setEmail(accountCreateDto.email());
        account.setEmailPassword(accountCreateDto.emailPassword());
        account.setStatus(AccountStatus.AVAILABLE);
        account.setProduct(Product.builder().id(accountCreateDto.productId()).build());
        account.setCreatedBy(AuthService.getCurrentUser());
        return account;
    }
    public AccountReadDto mapFrom(Account account) {
        return AccountReadDto.builder()
                .id(account.getId())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .createdById(account.getCreatedBy().getId())
                .productId(account.getProduct().getId())
                .build();
    }

    public Account mapFrom(AccountCreateDto dto) {
        return buildAccount(new Account(), dto);
    }
}
