package com.example.taskflow.service;

import com.example.taskflow.dtos.project.ProjectDTO;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.ProjectMember;
import com.example.taskflow.model.User;
import com.example.taskflow.repository.ProjectMemberRepository;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProjectService — testes unitários")
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter projectsCreatedCounter;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private ProjectService projectService;

    private User currentUser;
    private Project savedProject;

    @BeforeEach
    void setUp() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        currentUser = User.builder().id(1L).name("Owner").email("user@example.com").password("x").build();
        savedProject = Project.builder().id(10L).name("My Project").description("Desc").build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));
        when(meterRegistry.counter("taskflow.projects.created")).thenReturn(projectsCreatedCounter);
    }

    @Test
    @DisplayName("createProject salva projeto, adiciona owner como membro e incrementa counter")
    void createProject_savesProjectAndAddsOwner() {
        ProjectDTO dto = ProjectDTO.builder().name("My Project").description("Desc").build();
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        ProjectDTO result = projectService.createProject(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("My Project");
        verify(projectMemberRepository).save(any(ProjectMember.class));
        verify(projectsCreatedCounter).increment();
    }

    @Test
    @DisplayName("createProject lança RuntimeException se usuário autenticado não existe")
    void createProject_unknownUser_throws() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThatRuntimeException()
                .isThrownBy(() -> projectService.createProject(
                        ProjectDTO.builder().name("P").description("D").build()))
                .withMessageContaining("User not found");
    }
}
