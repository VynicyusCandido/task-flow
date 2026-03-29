package com.example.taskflow.mapper;

import com.example.taskflow.dtos.TaskDTO;
import com.example.taskflow.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskMapper {

    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    TaskDTO toDTO(Task task);

    @Mapping(target = "id", ignore = true)
    Task toEntity(TaskDTO taskDTO);
}