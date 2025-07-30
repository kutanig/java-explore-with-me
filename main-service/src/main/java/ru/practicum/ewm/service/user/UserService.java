package ru.practicum.ewm.service.user;

import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    UserDto registerUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);
}
