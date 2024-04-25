package com.example.clearSoul.service;

import com.example.clearSoul.dto.UserCreateDto;
import com.example.clearSoul.dto.UserDto;
import com.example.clearSoul.dto.UserUpdateDto;
import com.example.clearSoul.exception.EntityNotFoundException;
import com.example.clearSoul.mapper.UserMapper;
import com.example.clearSoul.model.User;
import com.example.clearSoul.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements UserService {
    private final UserRepository customerRepository;
    private final UserMapper customerMapper;

    @Override
    public UserDto saveUser(UserCreateDto requestDto) {
        User customer = customerMapper.toModel(requestDto);
        LocalDateTime creatingTime = LocalDateTime.now();
        customer.setCreated(creatingTime);
        customer.setUpdated(creatingTime);
        customer = customerRepository.save(customer);
        return customerMapper.toDto(customer);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toDto)
                .toList();
    }

    @Override
    public UserDto getById(Long id) {
        return customerMapper.toDto(customerRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't find customer with id " + id)));
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto updateDto) {
        User customer = customerRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't find customer with id " + id));
        correctCustomer(customer, updateDto);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    public void deleteUser(Long id) {
        customerRepository.deleteById(id);
    }

    private void correctCustomer(User customer, UserUpdateDto updateDto) {
        if (updateDto.getFullName() != null) {
            customer.setFullName(updateDto.getFullName());
        }
        if (updateDto.getPhone() != null) {
            customer.setPhone(updateDto.getPhone());
        }
        customer.setUpdated(LocalDateTime.now());
    }
}
