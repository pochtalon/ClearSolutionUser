package com.example.clearSoul.service;

import com.example.clearSoul.dto.UserCreateDto;
import com.example.clearSoul.dto.UserDto;
import com.example.clearSoul.dto.UserUpdateDto;
import java.util.List;

public interface UserService {
    UserDto saveUser(UserCreateDto requestDto);

    List<UserDto> getAllUsers();

    UserDto getById(Long id);

    UserDto updateUser(Long id, UserUpdateDto updateDto);

    void deleteUser(Long id);
}
