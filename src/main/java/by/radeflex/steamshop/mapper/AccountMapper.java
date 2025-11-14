package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.AccountReadDto;
import by.radeflex.steamshop.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountReadDto mapFrom(Account account) {
        return AccountReadDto.builder()
                .productId(account.getProduct().getId())
                .username(account.getUsername())
                .password(account.getPassword())
                .email(account.getEmail())
                .emailPassword(account.getEmailPassword())
                .build();
    }
}
