package com.example.clearSoul.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
}
