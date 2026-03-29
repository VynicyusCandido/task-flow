package com.example.taskflow.dtos;

import jakarta.validation.constraints.NotBlank;

public record TaskDTO(
        Long id,
        @NotBlank(message = "Title is required") String title,
        boolean completed
) {}