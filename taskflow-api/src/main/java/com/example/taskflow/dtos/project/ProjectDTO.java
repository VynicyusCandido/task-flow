package com.example.taskflow.dtos.project;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTO {
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    private LocalDateTime createdAt;
}
