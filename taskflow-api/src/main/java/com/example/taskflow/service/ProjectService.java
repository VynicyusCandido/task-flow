package com.example.taskflow.service;

import com.example.taskflow.dtos.project.InviteRequest;
import com.example.taskflow.dtos.project.ProjectDTO;
import com.example.taskflow.dtos.project.ProjectMemberDTO;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.ProjectMember;
import com.example.taskflow.model.User;
import com.example.taskflow.model.enums.ProjectRole;
import com.example.taskflow.repository.ProjectMemberRepository;
import com.example.taskflow.repository.ProjectRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<ProjectDTO> getCurrentUserProjects() {
        User currentUser = getCurrentUser();
        List<ProjectMember> memberships = projectMemberRepository.findByUserId(currentUser.getId());
        
        return memberships.stream()
                .map(ProjectMember::getProject)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDTO createProject(ProjectDTO customProjectDTO) {
        User currentUser = getCurrentUser();
        
        Project project = Project.builder()
                .name(customProjectDTO.getName())
                .description(customProjectDTO.getDescription())
                .build();
        
        project = projectRepository.save(project);
        
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(currentUser)
                .role(ProjectRole.OWNER)
                .build();
        
        projectMemberRepository.save(member);
        
        return mapToDto(project);
    }
    
    public ProjectDTO getProjectById(Long id) {
        User currentUser = getCurrentUser();
        ensureMemberAccess(id, currentUser.getId());
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
                
        return mapToDto(project);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        User currentUser = getCurrentUser();
        ensureOwnerAccess(id, currentUser.getId());
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
                
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        
        return mapToDto(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        User currentUser = getCurrentUser();
        ensureOwnerAccess(id, currentUser.getId());
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
                
        projectRepository.delete(project);
    }

    public List<ProjectMemberDTO> getProjectMembers(Long projectId) {
        User currentUser = getCurrentUser();
        ensureMemberAccess(projectId, currentUser.getId());
        
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(this::mapMemberToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectMemberDTO inviteMember(Long projectId, InviteRequest request) {
        User currentUser = getCurrentUser();
        ensureOwnerAccess(projectId, currentUser.getId());
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
                
        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User with email not found"));
                
        projectMemberRepository.findByProjectIdAndUserId(projectId, targetUser.getId())
                .ifPresent(m -> { throw new RuntimeException("User is already a member"); });
                
        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(targetUser)
                .role(ProjectRole.MEMBER)
                .build();
                
        return mapMemberToDto(projectMemberRepository.save(member));
    }
    
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        User currentUser = getCurrentUser();
        ensureOwnerAccess(projectId, currentUser.getId());
        
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found in project"));
                
        if (member.getRole() == ProjectRole.OWNER) {
            throw new RuntimeException("Cannot remove project owner");
        }
        
        projectMemberRepository.delete(member);
    }

    // Authorization helpers
    private void ensureMemberAccess(Long projectId, Long userId) {
        projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this project"));
    }
    
    private void ensureOwnerAccess(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this project"));
                
        if (member.getRole() != ProjectRole.OWNER) {
            throw new AccessDeniedException("Only project owners can perform this action");
        }
    }

    // Mappers (we could use MapStruct, but for simplicity we write them here or create a proper mapper component)
    private ProjectDTO mapToDto(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .build();
    }
    
    private ProjectMemberDTO mapMemberToDto(ProjectMember member) {
        return ProjectMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .userEmail(member.getUser().getEmail())
                .role(member.getRole())
                .build();
    }
}
