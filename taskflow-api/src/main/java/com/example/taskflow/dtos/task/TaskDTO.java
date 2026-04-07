package com.example.taskflow.dtos.task;

import com.example.taskflow.model.enums.TaskPriority;
import com.example.taskflow.model.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    
    private Long assigneeId;
    private String assigneeName;
}
