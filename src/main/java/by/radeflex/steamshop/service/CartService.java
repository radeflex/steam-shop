package by.radeflex.steamshop.service;

import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserProduct;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.repository.UserProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final ProductRepository productRepository;
    private final UserProductRepository userProductRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional
    public boolean add(Integer productId) {
        return productRepository.findById(productId)
                .map(product -> {
                    var user = getCurrentUser();
                    var userProduct = new UserProduct(null, user, product, null);
                    userProductRepository.save(userProduct);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public boolean delete(Integer userProductId) {
        return userProductRepository.findById(userProductId)
                .map(up -> {
                    userProductRepository.delete(up);
                    return true;
                }).orElse(false);
    }


    @Transactional
    public boolean updateQuantity(Integer userProductId, Integer quantity) {
        return userProductRepository.findById(userProductId)
                .map(up -> {
                    up.setQuantity(quantity);
                    userProductRepository.saveAndFlush(up);
                    return true;
                }).orElse(false);
    }
}
