package ru.practicum.ewm.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Fetching users with IDs: {}, from: {}, size: {}", ids, from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids, pageable);
            log.debug("Found {} users by IDs", users.size());
        } else {
            users = userRepository.findAll(pageable).getContent();
            log.debug("Found {} users without ID filter", users.size());
        }

        return users.stream()
                .map(userMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto registerUser(NewUserRequest newUserRequest) {
        log.info("Registering new user with email: {}", newUserRequest.getEmail());

        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            log.warn("Registration failed - email already exists: {}", newUserRequest.getEmail());
            throw new ConflictException("Email already exists");
        }

        User user = User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.mapToUserDto(savedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.error("User not found with ID: {}", userId);
            throw new NotFoundException("User not found");
        }

        userRepository.deleteById(userId);
        log.debug("User with ID {} deleted successfully", userId);
    }
}
