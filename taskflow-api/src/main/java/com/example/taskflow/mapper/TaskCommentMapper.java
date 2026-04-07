package com.example.taskflow.mapper;

import com.example.taskflow.dtos.task.TaskCommentDTO;
import com.example.taskflow.model.TaskComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskCommentMapper extends EntityMapper<TaskCommentDTO, TaskComment> {

    @Override
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.name", target = "authorName")
    TaskCommentDTO toDto(TaskComment entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TaskComment toEntity(TaskCommentDTO dto);
}
