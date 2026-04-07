package com.example.taskflow.mapper;

import com.example.taskflow.dtos.project.ProjectDTO;
import com.example.taskflow.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper extends EntityMapper<ProjectDTO, Project> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Project toEntity(ProjectDTO dto);
}
