package com.example.taskflow.service;

import com.example.taskflow.dtos.task.TaskCommentDTO;
import com.example.taskflow.dtos.task.TaskDTO;
import com.example.taskflow.dtos.task.TaskMoveRequest;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.Task;
import com.example.taskflow.model.TaskComment;
import com.example.taskflow.model.User;
import com.example.taskflow.repository.ProjectMemberRepository;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.TaskCommentRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskCommentRepository taskCommentRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void ensureMemberAccess(Long projectId, Long userId) {
        projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this project"));
    }

    public List<TaskDTO> getTasksByProject(Long projectId) {
        User currentUser = getCurrentUser();
        ensureMemberAccess(projectId, currentUser.getId());
        
        return taskRepository.findByProjectIdOrderByOrderIndexAsc(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO createTask(Long projectId, TaskDTO dto) {
        User currentUser = getCurrentUser();
        ensureMemberAccess(projectId, currentUser.getId());
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
                
        Task task = Task.builder()
                .project(project)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .priority(dto.getPriority())
                .dueDate(dto.getDueDate())
                .build();
                
        if (dto.getAssigneeId() != null) {
            User assignee = userRepository.findById(dto.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            // Optionally check if assignee is a member of the project
            ensureMemberAccess(projectId, assignee.getId());
            task.setAssignee(assignee);
        }
        
        return mapToDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDTO updateTask(Long projectId, Long taskId, TaskDTO dto) {
        User currentUser = getCurrentUser();
        ensureMemberAccess(projectId, currentUser.getId());
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
                
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }
        
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : task.getStatus());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : task.getPriority());
        task.setDueDate(dto.getDueDate());
        
        if (dto.getAssigneeId() != null) {
            User assignee = userRepository.findById(dto.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            ensureMemberAccess(projectId, assignee.getId());
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }
        
        return mapToDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDTO moveTask(Long projectId, Long taskId, TaskMoveRequest request) {
        User currentUser = getCurrentUser();
        ensureMemberAccess(projectId, currentUser.getId());
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
                
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }
        
        task.setStatus(request.getStatus());
        task.setOrderIndex(request.getOrderIndex());
        
        // Detailed order fixing can be done either dynamically or assumed to be handled by UI
        
        return mapToDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId) {
        User currentUser = getCurrentUser();
        ensureMemberAccess(projectId, currentUser.getId());
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
                
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }
        
        taskRepository.delete(task);
    }
    
    // Comments

    public List<TaskCommentDTO> getTaskComments(Long projectId, Long taskId) {
        User currentUser = getCurrentUser();
        ensureMemberAccess(projectId, currentUser.getId());
        
        // check task belongs to project
        Task task = taskRepository.findById(taskId).orElseThrow();
        if (!task.getProject().getId().equals(projectId)) throw new RuntimeException("Task not in project");
        
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(this::mapCommentToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskCommentDTO addComment(Long projectId, Long taskId, TaskCommentDTO dto) {
        User currentUser = getCurrentUser();
        ensureMemberAccess(projectId, currentUser.getId());
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
                
        if (!task.getProject().getId().equals(projectId)) {
            throw new RuntimeException("Task does not belong to this project");
        }
        
        TaskComment comment = TaskComment.builder()
                .content(dto.getContent())
                .task(task)
                .author(currentUser)
                .build();
                
        return mapCommentToDto(taskCommentRepository.save(comment));
    }

    // Mappers
    private TaskDTO mapToDto(Task task) {
        return TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .orderIndex(task.getOrderIndex())
                .createdAt(task.getCreatedAt())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getName() : null)
                .build();
    }
    
    private TaskCommentDTO mapCommentToDto(TaskComment comment) {
        return TaskCommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}