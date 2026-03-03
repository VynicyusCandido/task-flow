package com.example.taskflow.mapper;

import com.example.taskflow.dtos.TaskDTO;
import com.example.taskflow.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaskMapper {

    // Chamada do instancia do Mapper
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    // Passa entidade para DTO
    TaskDTO toDTO(Task entity);

    // Passa DTO para entidade (Com anotação para ignorar o id, que será gerado pelo banco)
    @Mapping(target = "id", ignore = true)
    Task toEntity(TaskDTO dto);

    // Opcional: para atualização, se necessário (ignorando o id)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(TaskDTO dto, @MappingTarget Task entity);
}
