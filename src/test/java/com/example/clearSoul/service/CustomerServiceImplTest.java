package com.example.clearSoul.service;

import com.example.clearSoul.dto.UserCreateDto;
import com.example.clearSoul.dto.UserDto;
import com.example.clearSoul.dto.UserUpdateDto;
import com.example.clearSoul.exception.EntityNotFoundException;
import com.example.clearSoul.mapper.UserMapper;
import com.example.clearSoul.model.User;
import com.example.clearSoul.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock
    private UserRepository customerRepository;
    @Mock
    private UserMapper customerMapper;
    @InjectMocks
    private UserServiceImpl customerService;
    private static final Long ID = 1994L;
    private static final String EMAIL = "eric.draven@mail.com";
    private static final String FULL_NAME = "Eric Draven";
    private static final String PHONE = "+380969609696";

    @Test
    @DisplayName("Adding new customer")
    public void saveCustomer_ValidCustomerCreateDto_ReturnCustomerDto(){
        UserCreateDto requestDto = new UserCreateDto()
                .setEmail(EMAIL)
                .setFullName(FULL_NAME)
                .setPhone(PHONE);
        User customer = createCustomer();
        UserDto expected = createCustomerDto();

        when(customerMapper.toModel(requestDto)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toDto(customer)).thenReturn(expected);

        UserDto actual = customerService.saveUser(requestDto);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get all customers")
    public void getAllCustomers_ReturnListOfCustomers(){
        User customer = createCustomer();
        UserDto customerDto = createCustomerDto();
        List<User> customers = List.of(customer);
        List<UserDto> expected = List.of(customerDto);

        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.toDto(customer)).thenReturn(customerDto);

        List<UserDto> actual = customerService.getAllUsers();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get customer by valid id")
    public void getById_ValidId_ReturnCustomerDto(){
        User customer = createCustomer();
        UserDto expected = createCustomerDto();

        when(customerRepository.findById(anyLong())).thenReturn(Optional.of(customer));
        when(customerMapper.toDto(customer)).thenReturn(expected);

        UserDto actual = customerService.getById(ID);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get customer by invalid id")
    public void getById_InvalidId_ThrowException(){
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> customerService.getById(ID));
        String expected = "Can't find customer with id " + ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update customer by valid id")
    public void updateCustomer_ValidIdAndDto_ReturnCustomerDto(){
        UserUpdateDto updateDto = new UserUpdateDto()
                .setFullName(FULL_NAME)
                .setPhone(PHONE);
        User customer = new User()
                .setEmail(EMAIL)
                .setFullName("Allan Poe")
                .setPhone("+380502641937");
        UserDto expected = createCustomerDto();

        when(customerRepository.findById(ID)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toDto(customer)).thenReturn(expected);

        UserDto actual = customerService.updateUser(ID, updateDto);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update customer by invalid id")
    public void updateCustomer_InvalidId_ThrowException(){
        UserUpdateDto updateDto = new UserUpdateDto();

        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> customerService.updateUser(ID, updateDto));
        String expected = "Can't find customer with id " + ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    private static User createCustomer() {
        return new User()
                .setEmail(EMAIL)
                .setFullName(FULL_NAME)
                .setPhone(PHONE);
    }

    private static UserDto createCustomerDto() {
        return new UserDto()
                .setEmail(EMAIL)
                .setFullName(FULL_NAME)
                .setPhone(PHONE);
    }
}