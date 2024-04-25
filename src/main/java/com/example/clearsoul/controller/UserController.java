package com.example.clearsoul.controller;

import com.example.clearsoul.dto.UserCreateDto;
import com.example.clearsoul.dto.UserDto;
import com.example.clearsoul.dto.UserUpdateDto;
import com.example.clearsoul.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for managing users")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create new user", description = "Create new user")
    public UserDto addUser(@RequestBody @Valid UserCreateDto createDto) {
        return userService.saveUser(createDto);
    }

    @GetMapping
    @Operation(summary = "Get all users",
            description = "Get list of all users")
    public List<UserDto> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id", description = "Get user by id")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user by id", description = "Update user by id")
    public UserDto updateUser(@PathVariable Long id,
                                  @Valid @RequestBody UserUpdateDto updateDto) {
        return userService.updateUser(id, updateDto);
    }

    @GetMapping("/search")
    @Operation(summary = "Search user by birth date range",
            description = "Search user by birth date range")
    public List<UserDto> searchByDateRange(@RequestParam LocalDate fromDate,
                                  @RequestParam LocalDate toDate) {
        return userService.searchUsersByDateRange(fromDate, toDate);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by id", description = "Delete user by id")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
