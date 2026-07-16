package com.sanskar.taskmanager.service;

import com.sanskar.taskmanager.dto.request.LoginRequest;
import com.sanskar.taskmanager.dto.request.SignupRequest;
import com.sanskar.taskmanager.dto.response.AuthResponse;
import com.sanskar.taskmanager.dto.response.UserResponse;
import com.sanskar.taskmanager.entity.Role;
import com.sanskar.taskmanager.entity.User;
import com.sanskar.taskmanager.repository.UserRepository;
import com.sanskar.taskmanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.MEMBER);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, toUserResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, toUserResponse(user));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
