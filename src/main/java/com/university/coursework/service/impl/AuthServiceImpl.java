package com.university.coursework.service.impl;

import com.university.coursework.domain.LoginRequest;
import com.university.coursework.domain.RegisterRequest;
import com.university.coursework.domain.UserDTO;
import com.university.coursework.domain.enums.Role;
import com.university.coursework.entity.UserEntity;
import com.university.coursework.exception.AuthenticationException;
import com.university.coursework.exception.UserAlreadyExistsException;
import com.university.coursework.repository.UserRepository;
import com.university.coursework.security.jwt.JwtTokenProvider;
import com.university.coursework.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        UserEntity user = userRepository.save(UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build());

        return new UserDTO(user.getEmail(), user.getPassword(), user.getUsername(), user.getPhone());
    }

    @Override
    public UserDTO loginUser(LoginRequest request) {
        Optional<UserEntity> userOpt = Optional.ofNullable(userRepository.findByEmail(request.getEmail()));
        UserEntity user = userOpt.orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("Password does not match for email: " + request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        return new UserDTO(user.getEmail(), user.getPassword(), user.getUsername(), user.getPhone());
    }

    public String generateToken(UserDTO user) {
        Role role = userRepository.findByEmail(user.getEmail()).getRole();
        return jwtTokenProvider.createToken(user.getEmail(), role);
    }
}
