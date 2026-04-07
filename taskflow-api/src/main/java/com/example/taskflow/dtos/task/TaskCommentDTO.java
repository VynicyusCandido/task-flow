package com.example.taskflow.dtos.task;

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
public class TaskCommentDTO {
    private Long id;
    
    @NotBlank(message = "Content cannot be empty")
    private String content;
    
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
}
