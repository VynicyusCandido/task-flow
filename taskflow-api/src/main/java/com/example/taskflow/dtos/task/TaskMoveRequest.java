package com.example.taskflow.dtos.task;

import com.example.taskflow.model.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskMoveRequest {
    
    @NotNull(message = "Status cannot be null")
    private TaskStatus status;
    
    @NotNull(message = "Order index cannot be null")
    private Integer orderIndex;
}
