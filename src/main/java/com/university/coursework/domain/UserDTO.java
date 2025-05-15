package com.university.coursework.domain;

import com.university.coursework.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @NotNull(message = "Email must be provided")
    @Email(message = "Invalid email format")
    private String email;

    private String password;
    private String username;
    private String phone;
}