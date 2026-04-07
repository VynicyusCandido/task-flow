package com.example.taskflow.controller;

import com.example.taskflow.dtos.task.TaskCommentDTO;
import com.example.taskflow.dtos.task.TaskDTO;
import com.example.taskflow.dtos.task.TaskMoveRequest;
import com.example.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<TaskDTO> getTasks(@PathVariable Long projectId) {
        return taskService.getTasksByProject(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(@PathVariable Long projectId, @Valid @RequestBody TaskDTO taskDTO) {
        return taskService.createTask(projectId, taskDTO);
    }

    @PutMapping("/{taskId}")
    public TaskDTO updateTask(@PathVariable Long projectId, @PathVariable Long taskId, @Valid @RequestBody TaskDTO taskDTO) {
        return taskService.updateTask(projectId, taskId, taskDTO);
    }

    @PatchMapping("/{taskId}/move")
    public TaskDTO moveTask(@PathVariable Long projectId, @PathVariable Long taskId, @Valid @RequestBody TaskMoveRequest request) {
        return taskService.moveTask(projectId, taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(projectId, taskId);
    }

    // Comments
    @GetMapping("/{taskId}/comments")
    public List<TaskCommentDTO> getTaskComments(@PathVariable Long projectId, @PathVariable Long taskId) {
        return taskService.getTaskComments(projectId, taskId);
    }

    @PostMapping("/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskCommentDTO addComment(@PathVariable Long projectId, @PathVariable Long taskId, @Valid @RequestBody TaskCommentDTO commentDTO) {
        return taskService.addComment(projectId, taskId, commentDTO);
    }
}