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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static by.radeflex.steamshop.service.AuthService.getCurrentUser;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProductHistoryMapper productHistoryMapper;
    private final UserProductHistoryRepository userProductHistoryRepository;
    private final ImageService imageService;

    private void checkUnique(UserInfo dto) {
        List<String> existing = new ArrayList<>();
        var user = getCurrentUser();
        var byUsername = dto.username() == null ? null : userRepository.findBy(QUser.user.username.eq(dto.username()),
                FluentQuery.FetchableFluentQuery::firstValue);
        var byEmail = dto.email() == null ? null : userRepository.findBy(QUser.user.email.eq(dto.email()),
                FluentQuery.FetchableFluentQuery::firstValue);
        if (byUsername != null) {
            if (user == null || user != null && !byUsername.equals(user)) {
                existing.add("username");
            }
        }
        if (byEmail != null)
            if (user == null || user != null && !byEmail.equals(user)) {
                existing.add("email");
            }
        if (!existing.isEmpty())
            throw new ObjectExistsException(existing);
    }

    @Transactional(readOnly = true)
    public CurrentUserReadDto findCurrent() {
        return userRepository.findById(getCurrentUser().getId())
                .map(userMapper::mapCurrentFrom).orElseThrow();
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
    public Optional<CurrentUserReadDto> update(UserUpdateDto userUpdateDto,
                                               MultipartFile image) {
        checkUnique(userUpdateDto);
        var passwordHash = userUpdateDto.password() == null ?
                null : passwordEncoder.encode(userUpdateDto.password());
        return userRepository.findById(getCurrentUser().getId())
                .map(u -> uploadImage(image, u))
                .map(u -> userMapper.mapFrom(u, userUpdateDto.withPassword(passwordHash)))
                .map(userRepository::saveAndFlush)
                .map(userMapper::mapCurrentFrom);
    }

    @Transactional
    public void resetAvatar() {
        var user = getCurrentUser();
        if (user.getAvatarUrl() != null) {
            imageService.delete(user.getAvatarUrl());
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }

    private User uploadImage(MultipartFile file, User u) {
        if (file != null) {
            if (u.getAvatarUrl() != null)
                imageService.delete(u.getAvatarUrl());
            var url = imageService.upload(file);
            u.setAvatarUrl(url);
        }
        return u;
    }

    @Transactional(readOnly = true)
    public Page<ProductHistoryReadDto> getProductHistoryCurrent(Pageable pageable) {
        var user = getCurrentUser();
        return userProductHistoryRepository.findByUser(user, pageable)
                .map(productHistoryMapper::mapFrom);
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
