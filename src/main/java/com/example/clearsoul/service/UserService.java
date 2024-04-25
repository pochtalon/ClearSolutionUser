package com.example.clearsoul.service;

import com.example.clearsoul.dto.UserCreateDto;
import com.example.clearsoul.dto.UserDto;
import com.example.clearsoul.dto.UserUpdateDto;
import java.time.LocalDate;
import java.util.List;

public interface UserService {
    UserDto saveUser(UserCreateDto requestDto);

    List<UserDto> getAllUsers();

    UserDto getById(Long id);

    UserDto updateUser(Long id, UserUpdateDto updateDto);

    void deleteUser(Long id);

    List<UserDto> searchUsersByDateRange(LocalDate fromDate, LocalDate toDate);
}
