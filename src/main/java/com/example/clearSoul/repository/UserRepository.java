package com.example.clearSoul.repository;

import com.example.clearSoul.dto.UserDto;
import com.example.clearSoul.model.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    List<UserDto> findByBirthDateBetween(LocalDate fromDate, LocalDate toDate);
}
