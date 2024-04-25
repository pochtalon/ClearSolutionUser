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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.time.LocalDate;
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
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;
    private static final Long ID = 1994L;
    private static final String EMAIL = "arthur_gordon_pym@mail.com";
    private static final String FIRST_NAME = "Arthur";
    private static final String LAST_NAME = "Pym";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1838, 6, 16);
    private static final String ADDRESS = "Nantucket";
    private static final String PHONE_NUMBER = "+380969609696";

    @Test
    @DisplayName("Adding new user")
    public void saveUser_ValidUserCreateDto_ReturnUserDto(){
        UserCreateDto requestDto = createUserCreateDto();
        User user = createUser();
        UserDto expected = createUserDto();

        when(userMapper.toModel(requestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        UserDto actual = userService.saveUser(requestDto);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Adding new user")
    public void saveUser_InvalidUserAge_ThrowException(){
        UserCreateDto requestDto = createUserCreateDto();
        requestDto.setBirthDate(LocalDate.of(2020, 3, 16));

        Exception exception = assertThrows(ForbiddenAgeException.class,
                () -> userService.saveUser(requestDto));
        String expected = "Age is forbidden. Come back to us later";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get all users")
    public void getAllUsers_ReturnListOfUsers(){
        User user = createUser();
        UserDto userDto = createUserDto();
        List<User> users = List.of(user);
        List<UserDto> expected = List.of(userDto);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(user)).thenReturn(userDto);

        List<UserDto> actual = userService.getAllUsers();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get user by valid id")
    public void getById_ValidId_ReturnUserDto(){
        User user = createUser();
        UserDto expected = createUserDto();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expected);

        UserDto actual = userService.getById(ID);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get user by invalid id")
    public void getById_InvalidId_ThrowException(){
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> userService.getById(ID));
        String expected = "Can't find user with id " + ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update user by valid id")
    public void updateCustomer_ValidIdAndDto_ReturnCustomerDto(){
        UserUpdateDto updateDto = createUserUpdateDto();
        User user = new User()
                .setEmail("EMAIL")
                .setFirstName("FIRST_NAME")
                .setLastName("LAST_NAME")
                .setBirthDate(BIRTH_DATE)
                .setAddress("ADDRESS")
                .setPhoneNumber("+380653268497");
        UserDto expected = createUserDto();

        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        UserDto actual = userService.updateUser(ID, updateDto);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update user by invalid id")
    public void updateUser_InvalidId_ThrowException(){
        UserUpdateDto updateDto = createUserUpdateDto();
        updateDto.setBirthDate(LocalDate.of(2000, 3, 16));

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(ID, updateDto));
        String expected = "Can't find user with id " + ID;
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update user by invalid age")
    public void updateUser_InvalidAge_ThrowException(){
        UserUpdateDto updateDto = new UserUpdateDto();
        User user = createUser();
        updateDto.setBirthDate(LocalDate.of(2020, 12, 4));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(ForbiddenAgeException.class,
                () -> userService.updateUser(ID, updateDto));
        String expected = "Age is forbidden. Come back to us later";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Search users by valid date range")
    public void searchUsersByDateRange_ValidRange_ReturnListOfUsers() {
        LocalDate fromDate = LocalDate.of(1835, 3, 6);
        LocalDate toDate = LocalDate.of(1837, 6, 3);
        User user = createUser();
        UserDto userDto = createUserDto();

        List<User> users = List.of(user);
        List<UserDto> expected = List.of(userDto);

        when(userRepository.findByBirthDateBetween(fromDate, toDate)).thenReturn(users);
        when(userMapper.toDto(user)).thenReturn(userDto);

        List<UserDto> actual = userService.searchUsersByDateRange(fromDate, toDate);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Search users by invalid date range")
    public void searchUsersByDateRange_InvalidRange_ThrowException() {
        LocalDate fromDate = LocalDate.of(1837, 3, 6);
        LocalDate toDate = LocalDate.of(1835, 6, 3);

        Exception exception = assertThrows(DateRangeException.class,
                () -> userService.searchUsersByDateRange(fromDate, toDate));
        String expected = "The second date is after the first. Change it!";
        String actual = exception.getMessage();
        assertEquals(expected, actual);
    }

    private static UserCreateDto createUserCreateDto() {
        return new UserCreateDto()
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setBirthDate(BIRTH_DATE)
                .setAddress(ADDRESS)
                .setPhoneNumber(PHONE_NUMBER);
    }

    private static User createUser() {
        return new User()
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setBirthDate(BIRTH_DATE)
                .setAddress(ADDRESS)
                .setPhoneNumber(PHONE_NUMBER);
    }

    private static UserDto createUserDto() {
        return new UserDto()
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setBirthDate(BIRTH_DATE)
                .setAddress(ADDRESS)
                .setPhoneNumber(PHONE_NUMBER);
    }

    private static UserUpdateDto createUserUpdateDto() {
        return new UserUpdateDto()
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setBirthDate(BIRTH_DATE)
                .setAddress(ADDRESS)
                .setPhoneNumber(PHONE_NUMBER);
    }
}