package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.dto.AccountReadDto;
import by.radeflex.steamshop.entity.Account;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(source = "product.id", target = "productId")
    AccountReadDto mapFrom(Account account);

    @Mapping(target = "product", source = "dto.productId")
    @Mapping(target = "status", constant = "AVAILABLE")
    @Mapping(target = "username", source = "dto.username")
    @Mapping(target = "password", source = "dto.password")
    @Mapping(target = "email", source = "dto.email")
    Account mapFrom(AccountCreateDto dto, User createdBy);

    default Product map(Integer productId) {
        return Product.builder().id(productId).build();
    }
}
