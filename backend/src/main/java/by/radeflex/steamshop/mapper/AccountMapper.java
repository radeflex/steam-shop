package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.dto.AccountReadDto;
import by.radeflex.steamshop.entity.Account;
import by.radeflex.steamshop.entity.AccountStatus;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    private Account buildAccount(Account account, AccountCreateDto accountCreateDto, User cur) {
        account.setUsername(accountCreateDto.username());
        account.setPassword(accountCreateDto.password());
        account.setEmail(accountCreateDto.email());
        account.setEmailPassword(accountCreateDto.emailPassword());
        account.setStatus(AccountStatus.AVAILABLE);
        account.setProduct(Product.builder().id(accountCreateDto.productId()).build());
        account.setCreatedBy(cur);
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

    public Account mapFrom(AccountCreateDto dto, User cur) {
        return buildAccount(new Account(), dto, cur);
    }
}
