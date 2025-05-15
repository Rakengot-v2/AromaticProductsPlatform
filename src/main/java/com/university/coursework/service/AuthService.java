package com.university.coursework.service;

import com.university.coursework.domain.LoginRequest;
import com.university.coursework.domain.RegisterRequest;
import com.university.coursework.domain.UserDTO;

public interface AuthService {
    UserDTO loginUser(LoginRequest request);

    UserDTO registerUser(RegisterRequest request);
    String generateToken(UserDTO user);
}
