package com.example.clearsoul.mapper;

import com.example.clearsoul.config.MapperConfig;
import com.example.clearsoul.dto.UserCreateDto;
import com.example.clearsoul.dto.UserDto;
import com.example.clearsoul.dto.UserUpdateDto;
import com.example.clearsoul.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserCreateDto createDto);

    User toModel(UserUpdateDto updateDto);

    UserDto toDto(User customer);
}
