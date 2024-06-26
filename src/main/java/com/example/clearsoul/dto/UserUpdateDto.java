package com.example.clearsoul.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserUpdateDto {
    @Size(min = 2, max = 100)
    @Email
    private String email;
    @Size(min = 2, max = 50)
    private String firstName;
    @Size(min = 2, max = 50)
    private String lastName;
    @Past
    private LocalDate birthDate;
    @Size(min = 2, max = 100)
    private String address;
    @Size(min = 6, max = 20)
    @Pattern(regexp = "\\+[0-9]{5,13}")
    private String phoneNumber;
}
