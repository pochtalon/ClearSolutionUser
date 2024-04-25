package com.example.clearSoul.mapper;

import com.example.clearSoul.config.MapperConfig;
import com.example.clearSoul.dto.UserCreateDto;
import com.example.clearSoul.dto.UserDto;
import com.example.clearSoul.dto.UserUpdateDto;
import com.example.clearSoul.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserCreateDto createDto);

    User toModel(UserUpdateDto updateDto);

    UserDto toDto(User customer);
}
