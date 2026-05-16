package com.example.taskflow.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role; // "ADMIN" ou "MEMBER" para refletir o front
    private String avatarUrl; // Opcional, para refletir o front
}
