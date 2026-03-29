package com.example.taskflow.service;

import com.example.taskflow.dtos.TaskDTO;
import com.example.taskflow.mapper.TaskMapper;
import com.example.taskflow.model.Task;
import com.example.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<TaskDTO> findAll() {
        return taskRepository.findAll()
                .stream()
                .map(TaskMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDTO findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return TaskMapper.INSTANCE.toDTO(task);
    }

    @Transactional
    public TaskDTO create(TaskDTO taskDTO) {
        Task task = TaskMapper.INSTANCE.toEntity(taskDTO);
        Task saved = taskRepository.save(task);
        return TaskMapper.INSTANCE.toDTO(saved);
    }

    @Transactional
    public TaskDTO save(Long id, TaskDTO taskDTO) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        existingTask.setTitle(taskDTO.title());
        existingTask.setCompleted(taskDTO.completed());
        Task updated = taskRepository.save(existingTask);
        return TaskMapper.INSTANCE.toDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
}