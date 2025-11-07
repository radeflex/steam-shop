package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.UserCreateEditDto;
import by.radeflex.steamshop.dto.UserReadDto;
import by.radeflex.steamshop.entity.QUser;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.mapper.UserMapper;
import by.radeflex.steamshop.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private void checkUnique(UserCreateEditDto dto) {
        if (userRepository.exists(QUser.user.username.eq(dto.username())))
            throw new ObjectExistsException();
        if (userRepository.exists(QUser.user.email.eq(dto.email())))
            throw new ObjectExistsException();
    }

    @Transactional
    public User create(UserCreateEditDto userCreateEditDto) {
        checkUnique(userCreateEditDto);
        var passwordHash = passwordEncoder.encode(userCreateEditDto.password());
        return Optional.of(userCreateEditDto.withPassword(passwordHash))
                .map(userMapper::mapFrom)
                .map(userRepository::save).orElseThrow();
    }

    @Transactional
    public Optional<UserReadDto> update(Integer id, UserCreateEditDto userCreateEditDto) {
        checkUnique(userCreateEditDto);
        var passwordHash = passwordEncoder.encode(userCreateEditDto.password());
        return userRepository.findById(id)
                .map(u -> userMapper.mapFrom(u, userCreateEditDto.withPassword(passwordHash)))
                .map(userRepository::saveAndFlush)
                .map(userMapper::mapFrom);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
