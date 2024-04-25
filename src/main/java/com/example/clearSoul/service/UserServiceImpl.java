package com.example.clearSoul.service;

import com.example.clearSoul.dto.UserCreateDto;
import com.example.clearSoul.dto.UserDto;
import com.example.clearSoul.dto.UserUpdateDto;
import com.example.clearSoul.exception.DateRangeException;
import com.example.clearSoul.exception.EntityNotFoundException;
import com.example.clearSoul.exception.ForbiddenAgeException;
import com.example.clearSoul.mapper.UserMapper;
import com.example.clearSoul.model.User;
import com.example.clearSoul.repository.UserRepository;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    @Value("${user.minAge}")
    private final int MIN_AGE;
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
        if (toDate.isAfter(fromDate)) {
            throw new DateRangeException("The second date is after the first. Change it!");
        }
        return userRepository.findByBirthDateBetween(fromDate, toDate);
    }

    private void validateAge(LocalDate birthDate) {
        int userYears = Period.between(LocalDate.now(), birthDate).getYears();

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
