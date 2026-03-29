package com.example.taskflow.mapper;

import com.example.taskflow.dtos.TaskDTO;
import com.example.taskflow.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskMapper {

    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    // Entity -> DTO (todos os campos)
    TaskDTO toDTO(Task task);

    // DTO -> Entity (ignorar id pois será gerado pelo banco)
    @Mapping(target = "id", ignore = true)
    Task toEntity(TaskDTO taskDTO);
}