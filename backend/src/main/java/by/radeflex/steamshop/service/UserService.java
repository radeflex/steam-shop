package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.*;
import by.radeflex.steamshop.entity.QUser;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.filter.PredicateBuilder;
import by.radeflex.steamshop.filter.UserFilter;
import by.radeflex.steamshop.mapper.ProductHistoryMapper;
import by.radeflex.steamshop.mapper.UserMapper;
import by.radeflex.steamshop.repository.UserProductHistoryRepository;
import by.radeflex.steamshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProductHistoryMapper productHistoryMapper;
    private final UserProductHistoryRepository userProductHistoryRepository;
    private final ImageService imageService;
    private final CurrentUserService currentUserService;

    private void checkUnique(UserInfo dto) {
        List<String> existing = new ArrayList<>();
        var user = currentUserService.getCurrentUserEntity();
        var byUsername = dto.username() == null ? Optional.empty()
                : userRepository.findByUsername(dto.username());
        var byEmail = dto.email() == null ? Optional.empty()
                : userRepository.findByEmail(dto.email());
        byUsername.ifPresent(u -> {
            if (!u.equals(user))
                existing.add("username");
        });
        byEmail.ifPresent(u -> {
            if (!u.equals(user))
                existing.add("email");
        });
        if (!existing.isEmpty())
            throw new ObjectExistsException(existing);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "user::current", key = "@currentUserService.getCurrentUserId()")
    @SneakyThrows
    public CurrentUserReadDto findCurrent() {
        return userRepository.findById(currentUserService.getCurrentUserEntity().getId())
                .map(userMapper::mapCurrentFrom)
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public Optional<UserReadDto> findById(Integer id) {
        return userRepository.findById(id)
                .map(userMapper::mapFrom);
    }

    @Transactional
    public User create(UserCreateDto userCreateDto) {
        checkUnique(userCreateDto);
        var passwordHash = passwordEncoder.encode(userCreateDto.password());
        return Optional.of(userCreateDto.withPassword(passwordHash))
                .map(userMapper::mapFrom)
                .map(userRepository::save)
                .orElseThrow();
    }

    @Transactional
    @CacheEvict(value = "user::current",
            key = "#result.get().id()",
            condition = "#result.isPresent()")
    public Optional<CurrentUserReadDto> update(UserUpdateDto userUpdateDto,
                                               MultipartFile image) {
        checkUnique(userUpdateDto);
        var passwordHash = userUpdateDto.password() == null ?
                null : passwordEncoder.encode(userUpdateDto.password());
        return userRepository.findById(currentUserService.getCurrentUserEntity().getId())
                .map(u -> {
                    if (userUpdateDto.email() != null) u.setConfirmed(false);
                    return u;
                }).map(u -> uploadAvatar(image, u))
                .map(u -> userMapper.mapFrom(u, userUpdateDto.withPassword(passwordHash)))
                .map(userRepository::saveAndFlush)
                .map(userMapper::mapCurrentFrom);
    }

    @Transactional
    public void resetAvatar() {
        var user = currentUserService.getCurrentUserEntity();
        if (user.getAvatarUrl() != null) {
            imageService.delete(user.getAvatarUrl());
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }

    private User uploadAvatar(MultipartFile file, User u) {
        if (file != null) {
            if (u.getAvatarUrl() != null)
                imageService.delete(u.getAvatarUrl());
            var url = imageService.upload(file);
            u.setAvatarUrl(url);
        }
        return u;
    }

    @Cacheable(value = "user::product-history", key = "@currentUserService.getCurrentUserId()")
    @Transactional(readOnly = true)
    public PageResponse<ProductHistoryReadDto> getProductHistoryCurrent(Pageable pageable) {
        var user = currentUserService.getCurrentUserEntity();
        return PageResponse.of(userProductHistoryRepository.findByUser(user, pageable)
                .map(productHistoryMapper::mapFrom));
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Page<UserReadDto> findAll(UserFilter filter, Pageable pageable) {
        var predicate = PredicateBuilder.builder()
                .add(filter.username(), QUser.user.username::containsIgnoreCase)
                .add(filter.createdAt(), QUser.user.createdAt::eq).buildAnd();
        if (predicate == null) {
            return userRepository.findAll(pageable)
                    .map(userMapper::mapFrom);
        }
        return userRepository.findAll(predicate, pageable)
                .map(userMapper::mapFrom);
    }
}
