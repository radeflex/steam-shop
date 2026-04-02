package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.CartProductReadDto;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.entity.UserProduct;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.mapper.CartMapper;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.repository.UserProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final ProductRepository productRepository;
    private final UserProductRepository userProductRepository;
    private final CartMapper cartMapper;
    private final CurrentUserService currentUserService;

    private void checkEnoughAccounts(UserProduct up) {
        if (!userProductRepository.hasEnoughAccounts(up.getProduct(), up.getQuantity()))
            throw new AccountLackException();
    }

    @Cacheable(value = "cart", key = "@currentUserService.getCurrentUserId()")
    @Transactional(readOnly = true)
    public PageResponse<CartProductReadDto> findAll(Pageable pageable) {
        return PageResponse.of(userProductRepository.findPageByUser(currentUserService.getCurrentUserEntity(), pageable)
                .map(cartMapper::mapFrom));
    }

    @CacheEvict(
            value = "cart",
            key = "@currentUserService.getCurrentUserId()",
            condition = "#result.isPresent()")
    @Transactional
    public Optional<CartProductReadDto> create(Integer productId) {
        return productRepository.findById(productId)
                .map(product -> {
                    var user = currentUserService.getCurrentUserEntity();
                    if (userProductRepository.existsByUserAndProduct(user, product))
                        throw new ObjectExistsException();
                    var userProduct = new UserProduct(null, user, product, 1);
                    checkEnoughAccounts(userProduct);
                    userProductRepository.save(userProduct);
                    return userProduct;
                }).map(cartMapper::mapFrom);
    }

    @Transactional
    @CacheEvict(
            value = "cart",
            key = "@currentUserService.getCurrentUserId()",
            condition = "#result == true")
    public boolean delete(Integer userProductId) {
        return userProductRepository.findById(userProductId)
                .map(up -> {
                    userProductRepository.delete(up);
                    return true;
                }).orElse(false);
    }

    @Transactional
    @CacheEvict(
            value = "cart",
            key = "@currentUserService.getCurrentUserId()",
            condition = "#result.isPresent()")
    public Optional<CartProductReadDto> updateQuantity(Integer userProductId, Integer quantity) {
        return userProductRepository.findById(userProductId)
                .map(up -> {
                    up.setQuantity(quantity);
                    checkEnoughAccounts(up);
                    userProductRepository.saveAndFlush(up);
                    return up;
                }).map(cartMapper::mapFrom);
    }
}
