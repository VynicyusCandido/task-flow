package com.example.taskflow.controller;

import com.example.taskflow.dtos.user.UserResponseDTO;
import com.example.taskflow.model.User;
import com.example.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role("MEMBER") // Simulando role por enquanto, conforme o front
                .avatarUrl("https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getName()) // Avatar dinâmico bonitinho
                .build();

        return ResponseEntity.ok(responseDTO);
    }
}
