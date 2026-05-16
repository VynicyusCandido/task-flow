package com.example.taskflow.validation;

import com.example.taskflow.dtos.task.TaskDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized validator for TaskDTO.
 * Contains all business-rule validations for a task.
 * Can be used in both the application layer (services) and in unit tests.
 */
@Component
public class TaskValidator {

    /**
     * Validates the required fields of a TaskDTO for creation or update.
     *
     * @param dto the task data to validate
     * @throws IllegalArgumentException if any required field is missing or invalid
     */
    public void validate(TaskDTO dto) {
        List<String> errors = collectErrors(dto);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Task validation failed: " + String.join("; ", errors));
        }
    }

    /**
     * Validates the required fields and returns a list of error messages.
     * Useful for tests and for displaying multiple field errors at once.
     *
     * @param dto the task data to validate
     * @return list of error messages (empty if valid)
     */
    public List<String> collectErrors(TaskDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto == null) {
            errors.add("Task payload must not be null");
            return errors; // no further checks possible
        }

        // --- title ---
        if (!StringUtils.hasText(dto.getTitle())) {
            errors.add("Title must not be blank");
        }

        // --- description ---
        if (!StringUtils.hasText(dto.getDescription())) {
            errors.add("Description must not be blank");
        }

        // --- status ---
        if (dto.getStatus() == null) {
            errors.add("Status must not be null");
        }

        return errors;
    }

    /**
     * Validates that a projectId is present (non-null and positive).
     *
     * @param projectId the project identifier received by the endpoint
     * @throws IllegalArgumentException if projectId is invalid
     */
    public void validateProjectId(Long projectId) {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("A valid project ID must be provided");
        }
    }

    /**
     * Validates that, when an assigneeId is provided, it refers to a positive ID.
     *
     * @param assigneeId the assignee identifier in the DTO (may be null – optional field)
     * @throws IllegalArgumentException if assigneeId is provided but invalid
     */
    public void validateAssigneeId(Long assigneeId) {
        if (assigneeId != null && assigneeId <= 0) {
            throw new IllegalArgumentException("When provided, assignee ID must be a positive number");
        }
    }

    /**
     * Full validation: fields + projectId + optional assigneeId.
     *
     * @param projectId the project ID from the path variable
     * @param dto       the task payload
     */
    public void validateAll(Long projectId, TaskDTO dto) {
        validateProjectId(projectId);
        validate(dto);
        if (dto != null) {
            validateAssigneeId(dto.getAssigneeId());
        }
    }

    // -------------------------------------------------------------------------
    // Convenience boolean checkers (useful in tests and conditional logic)
    // -------------------------------------------------------------------------

    public boolean isTitleValid(TaskDTO dto) {
        return dto != null && StringUtils.hasText(dto.getTitle());
    }

    public boolean isDescriptionValid(TaskDTO dto) {
        return dto != null && StringUtils.hasText(dto.getDescription());
    }

    public boolean isStatusValid(TaskDTO dto) {
        return dto != null && dto.getStatus() != null;
    }

    public boolean isProjectIdValid(Long projectId) {
        return projectId != null && projectId > 0;
    }

    public boolean isAssigneeIdValid(Long assigneeId) {
        return assigneeId == null || assigneeId > 0; // null = unassigned, which is valid
    }
}
