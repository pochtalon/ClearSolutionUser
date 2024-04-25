package com.example.clearsoul.service;

import com.example.clearsoul.dto.UserCreateDto;
import com.example.clearsoul.dto.UserDto;
import com.example.clearsoul.dto.UserUpdateDto;
import com.example.clearsoul.exception.DateRangeException;
import com.example.clearsoul.exception.EntityNotFoundException;
import com.example.clearsoul.exception.ForbiddenAgeException;
import com.example.clearsoul.mapper.UserMapper;
import com.example.clearsoul.model.User;
import com.example.clearsoul.repository.UserRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private static final int MIN_AGE = 18;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto saveUser(UserCreateDto requestDto) {
        validateAge(requestDto.getBirthDate());
        User user = userMapper.toModel(requestDto);
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto).toList();
    }

    @Override
    public UserDto getById(Long id) {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't find user with id " + id)));
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto updateDto) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't find user with id " + id));
        correctUser(user, updateDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> searchUsersByDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new DateRangeException("The second date is after the first. Change it!");
        }

        return userRepository.findByBirthDateBetween(fromDate, toDate).stream()
                .map(userMapper::toDto)
                .toList();
    }

    private void validateAge(LocalDate birthDate) {
        int userYears = Period.between(birthDate, LocalDate.now()).getYears();
        if (userYears < MIN_AGE) {
            throw new ForbiddenAgeException("Age is forbidden. Come back to us later");
        }
    }

    private void correctUser(User user, UserUpdateDto updateDto) {
        if (updateDto.getEmail() != null) {
            user.setEmail(updateDto.getEmail());
        }
        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        if (updateDto.getBirthDate() != null) {
            validateAge(updateDto.getBirthDate());
            user.setBirthDate(updateDto.getBirthDate());
        }
        if (updateDto.getAddress() != null) {
            user.setAddress(updateDto.getAddress());
        }
        if (updateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }
    }
}
