package com.example.taskflow.mapper;

import com.example.taskflow.dtos.project.ProjectMemberDTO;
import com.example.taskflow.model.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper extends EntityMapper<ProjectMemberDTO, ProjectMember> {

    @Override
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    ProjectMemberDTO toDto(ProjectMember entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    ProjectMember toEntity(ProjectMemberDTO dto);
}
