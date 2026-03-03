package com.example.taskflow.dtos;

public record TaskDTO(
        Long id,
        String title,
        boolean completed
) {}