package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.NotificationCreateDto;
import by.radeflex.steamshop.dto.NotificationReadDto;
import by.radeflex.steamshop.dto.PageResponse;
import by.radeflex.steamshop.entity.NotificationRead;
import by.radeflex.steamshop.entity.Payment;
import by.radeflex.steamshop.mapper.NotificationMapper;
import by.radeflex.steamshop.repository.NotificationReadRepository;
import by.radeflex.steamshop.repository.NotificationRepository;
import by.radeflex.steamshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final NotificationMapper notificationMapper;
    private final CurrentUserService currentUserService;

    @Cacheable(
            value = "notifications::user",
            key = "@currentUserService.getCurrentUserId()")
    public PageResponse<NotificationReadDto> findAll(Pageable pageable) {
        return PageResponse.of(notificationRepository.findAllByUserIdOrUserIdNull(
                currentUserService.getCurrentUserEntity().getId(), pageable)
                .map(notificationMapper::mapFrom));
    }

    @Cacheable(value = "notifications")
    public PageResponse<NotificationReadDto> findAllAdmin(Pageable pageable) {
        return PageResponse.of(notificationRepository.findAll(pageable)
                .map(notificationMapper::mapFrom));
    }

    @Cacheable(
            value = "notifications::user::unread",
            key = "@currentUserService.getCurrentUserId()")
    public PageResponse<NotificationReadDto> findUnread(Pageable pageable) {
        return PageResponse.of(notificationRepository.findAllUnread(
                currentUserService.getCurrentUserEntity().getId(), pageable)
                .map(notificationMapper::mapFrom));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notifications"),
            @CacheEvict(value = "notifications::user", allEntries = true),
            @CacheEvict(value = "notifications::user::unread", allEntries = true)
    })
    public NotificationReadDto sendAll(NotificationCreateDto dto) {
        return Optional.of(notificationMapper.mapFrom(dto, null))
                .map(notificationRepository::save)
                .map(notificationMapper::mapFrom)
                .orElseThrow();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = "notifications::user::unread",
                    key = "#result.user.id",
                    condition = "#result != null"),
            @CacheEvict(
                    value = "notifications::user",
                    key = "#result.user.id",
                    condition = "#result != null"),
            @CacheEvict(value = "notifications", allEntries = true)
    })
    public Payment sendPayment(Payment payment) {
        notificationRepository.findByPaymentId(payment.getId())
                .ifPresentOrElse(n -> {
                    notificationRepository.saveAndFlush(notificationMapper.mapFrom(n, payment));
                    notificationReadRepository.deleteByNotification(n);
                }, () -> notificationRepository.saveAndFlush(notificationMapper.mapFrom(null, payment)));
        return payment;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = "notifications::user::unread",
                    key = "@currentUserService.getCurrentUserId()",
                    condition = "#result.isPresent()"
            ),
            @CacheEvict(
                    value = "notifications::user",
                    key = "@currentUserService.getCurrentUserId()",
                    condition = "#result.isPresent()"
            ),
            @CacheEvict(
                    value = "notifications",
                    allEntries = true,
                    condition = "#result.isPresent()"
            )
    })
    public Optional<NotificationReadDto> sendToUser(Integer userId, NotificationCreateDto dto) {
        return userRepository.findById(userId)
                .map(u -> notificationMapper.mapFrom(dto, u))
                .map(notificationRepository::save)
                .map(notificationMapper::mapFrom);
    }

    @Transactional
    @CacheEvict(
            value = "notifications::user::unread",
            key = "@currentUserService.getCurrentUserId()",
            condition = "#result"
    )
    public boolean read(Integer id) {
        var u = currentUserService.getCurrentUserEntity();
        return notificationRepository.findById(id)
                .filter(n -> !notificationReadRepository.existsByNotificationAndUser(n, u))
                .map(n -> {
                    var read = NotificationRead.builder()
                            .user(u)
                            .notification(n)
                            .build();
                    notificationReadRepository.save(read);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public boolean delete(Integer id) {
        return notificationRepository.findById(id)
                .map(n -> {
                    notificationRepository.delete(n);
                    return true;
                })
                .orElse(false);
    }
}
