package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.ProductHistoryReadDto;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.entity.Payment;
import by.radeflex.steamshop.mapper.ProductHistoryMapper;
import by.radeflex.steamshop.repository.PaymentItemRepository;
import by.radeflex.steamshop.repository.UserProductHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProductHistoryService {
    private final ProductHistoryMapper productHistoryMapper;
    private final UserProductHistoryRepository userProductHistoryRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final CurrentUserService currentUserService;

    @Cacheable(value = "user::product-history", key = "@currentUserService.getCurrentUserId()")
    public PageResponse<ProductHistoryReadDto> findAll(Pageable pageable) {
        var user = currentUserService.getCurrentUserEntity();
        return PageResponse.of(userProductHistoryRepository.findByUser(user, pageable)
                .map(productHistoryMapper::mapFrom));
    }

    @Transactional
    public void saveHistory(Payment payment) {
        paymentItemRepository.findAllByPayment(payment).stream()
                .map(productHistoryMapper::mapFrom)
                .forEach(userProductHistoryRepository::save);
    }
}
