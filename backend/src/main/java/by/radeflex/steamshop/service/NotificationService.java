package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.NotificationCreateDto;
import by.radeflex.steamshop.dto.NotificationReadDto;
import by.radeflex.steamshop.entity.NotificationRead;
import by.radeflex.steamshop.entity.Payment;
import by.radeflex.steamshop.mapper.NotificationMapper;
import by.radeflex.steamshop.repository.NotificationReadRepository;
import by.radeflex.steamshop.repository.NotificationRepository;
import by.radeflex.steamshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    public Page<NotificationReadDto> findAll(Pageable pageable) {
        return notificationRepository.findAllByUserIdOrUserIdNull(
                AuthService.getCurrentUser().getId(), pageable)
                .map(notificationMapper::mapFrom);
    }

    public Page<NotificationReadDto> findAllAdmin(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(notificationMapper::mapFrom);
    }

    public Page<NotificationReadDto> findUnread(Pageable pageable) {
        return notificationRepository.findAllUnread(
                AuthService.getCurrentUser().getId(), pageable)
                .map(notificationMapper::mapFrom);
    }

    @Transactional
    public NotificationReadDto sendAll(NotificationCreateDto dto) {
        return Optional.of(notificationMapper.mapFrom(dto, null))
                .map(notificationRepository::save)
                .map(notificationMapper::mapFrom)
                .orElseThrow();
    }

    @Transactional
    void sendPayment(Payment payment) {
        notificationRepository.findByPaymentId(payment.getId())
                .ifPresentOrElse(n -> {
                    notificationRepository.saveAndFlush(notificationMapper.mapFrom(n, payment));
                    notificationReadRepository.deleteByNotification(n);
                }, () -> notificationRepository.saveAndFlush(notificationMapper.mapFrom(null, payment)));
    }

    @Transactional
    public Optional<NotificationReadDto> sendToUser(Integer userId, NotificationCreateDto dto) {
        return userRepository.findById(userId)
                .map(u -> notificationMapper.mapFrom(dto, u))
                .map(notificationRepository::save)
                .map(notificationMapper::mapFrom);
    }
    @Transactional
    public boolean read(Integer id) {
        return notificationRepository.findById(id)
                .map(n -> {
                    var read = NotificationRead.builder()
                            .user(AuthService.getCurrentUser())
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
