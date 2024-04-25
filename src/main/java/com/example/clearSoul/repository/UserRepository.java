package com.example.clearSoul.repository;

import com.example.clearSoul.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
