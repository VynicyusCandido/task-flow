package com.example.taskflow.service;

import com.example.taskflow.dtos.task.TaskDTO;
import com.example.taskflow.model.Project;
import com.example.taskflow.model.Task;
import com.example.taskflow.model.User;
import com.example.taskflow.model.enums.TaskPriority;
import com.example.taskflow.model.enums.TaskStatus;
import com.example.taskflow.repository.ProjectMemberRepository;
import com.example.taskflow.repository.ProjectRepository;
import com.example.taskflow.repository.TaskCommentRepository;
import com.example.taskflow.repository.TaskRepository;
import com.example.taskflow.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Objects;

/**
 * Unit tests for {@link TaskService}.
 *
 * Spring context is NOT loaded — all dependencies are mocked with Mockito.
 * The SecurityContextHolder is set up manually so that getCurrentUser() works.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TaskService — testes unitários")
class TaskServiceTest {

        // ----- Mocks -----
        @Mock
        private TaskRepository taskRepository;
        @Mock
        private ProjectRepository projectRepository;
        @Mock
        private ProjectMemberRepository projectMemberRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private TaskCommentRepository taskCommentRepository;
        @Mock
        private MeterRegistry meterRegistry;
        @Mock
        private Counter tasksCreatedCounter;
        @Mock
        private SecurityContext securityContext;
        @Mock
        private Authentication authentication;

        @InjectMocks
        private TaskService taskService;

        // ----- Test fixtures -----
        private User currentUser;
        private User assigneeUser;
        private Project project;
        private Task task;

        @BeforeEach
        void setUp() {
                // Configure SecurityContextHolder to return a fake authenticated user
                when(authentication.getName()).thenReturn("user@example.com");
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                // Build test entities
                currentUser = User.builder()
                                .id(1L)
                                .name("Current User")
                                .email("user@example.com")
                                .password("encoded-secret")
                                .build();

                assigneeUser = User.builder()
                                .id(2L)
                                .name("Assigned User")
                                .email("assignee@example.com")
                                .password("encoded-secret")
                                .build();

                project = Project.builder()
                                .id(10L)
                                .name("TaskFlow Project")
                                .description("Demo project")
                                .build();

                task = Task.builder()
                                .id(100L)
                                .title("Fix login bug")
                                .description("Login button broken on mobile")
                                .status(TaskStatus.TODO)
                                .priority(TaskPriority.HIGH)
                                .orderIndex(0)
                                .createdAt(LocalDateTime.now())
                                .project(project)
                                .assignee(assigneeUser)
                                .build();

                when(meterRegistry.counter("taskflow.tasks.created")).thenReturn(tasksCreatedCounter);

                // Default: current user is found by email
                when(userRepository.findByEmail("user@example.com"))
                                .thenReturn(Optional.of(currentUser));

                // Default: current user is a project member
                when(projectMemberRepository.findByProjectIdAndUserId(project.getId(), currentUser.getId()))
                                .thenReturn(Optional.of(mock(com.example.taskflow.model.ProjectMember.class)));
        }

        // =========================================================================
        // getTasksByProject
        // =========================================================================
        @Nested
        @DisplayName("getTasksByProject")
        class GetTasksByProject {

                @Test
                @DisplayName("Retorna lista de DTOs para um projeto do qual o usuário é membro")
                void memberAccess_returnsTasks() {
                        when(taskRepository.findByProjectIdOrderByOrderIndexAsc(project.getId()))
                                        .thenReturn(List.of(task));

                        List<TaskDTO> result = taskService.getTasksByProject(project.getId());

                        assertThat(result).hasSize(1);
                        TaskDTO dto = result.get(0);
                        assertThat(dto.getId()).isEqualTo(task.getId());
                        assertThat(dto.getTitle()).isEqualTo(task.getTitle());
                        assertThat(dto.getStatus()).isEqualTo(TaskStatus.TODO);
                        assertThat(dto.getAssigneeId()).isEqualTo(assigneeUser.getId());
                        assertThat(dto.getAssigneeName()).isEqualTo(assigneeUser.getName());
                }

                @Test
                @DisplayName("Lança AccessDeniedException quando o usuário não é membro do projeto")
                void nonMember_throwsAccessDenied() {
                        when(projectMemberRepository.findByProjectIdAndUserId(project.getId(), currentUser.getId()))
                                        .thenReturn(Optional.empty());

                        assertThatExceptionOfType(AccessDeniedException.class)
                                        .isThrownBy(() -> taskService.getTasksByProject(project.getId()));
                }
        }

        // =========================================================================
        // createTask
        // =========================================================================
        @Nested
        @DisplayName("createTask — criação de tarefa")
        class CreateTask {

                @Test
                @DisplayName("Cria e retorna o DTO da tarefa quando todos os campos são válidos")
                void validPayload_createsTask() {
                        TaskDTO dto = TaskDTO.builder()
                                        .title("New task")
                                        .description("Some description")
                                        .status(TaskStatus.IN_PROGRESS)
                                        .priority(TaskPriority.MEDIUM)
                                        .assigneeId(assigneeUser.getId())
                                        .build();

                        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
                        when(userRepository.findById(Objects.requireNonNull(assigneeUser.getId())))
                                        .thenReturn(Optional.of(assigneeUser));
                        when(projectMemberRepository.findByProjectIdAndUserId(project.getId(), assigneeUser.getId()))
                                        .thenReturn(Optional.of(mock(com.example.taskflow.model.ProjectMember.class)));

                        Task savedTask = Task.builder()
                                        .id(200L)
                                        .title(dto.getTitle())
                                        .description(dto.getDescription())
                                        .status(dto.getStatus())
                                        .priority(dto.getPriority())
                                        .orderIndex(0)
                                        .createdAt(LocalDateTime.now())
                                        .project(project)
                                        .assignee(assigneeUser)
                                        .build();

                        when(taskRepository.save(notNull())).thenReturn(savedTask);

                        TaskDTO result = taskService.createTask(project.getId(), dto);

                        assertThat(result.getId()).isEqualTo(200L);
                        assertThat(result.getTitle()).isEqualTo("New task");
                        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
                        assertThat(result.getAssigneeId()).isEqualTo(assigneeUser.getId());
                        assertThat(result.getAssigneeName()).isEqualTo(assigneeUser.getName());

                        verify(taskRepository).save(notNull());
                }

                @Test
                @DisplayName("Cria tarefa sem responsável quando assigneeId é nulo")
                void noAssignee_taskCreatedWithoutAssignee() {
                        TaskDTO dto = TaskDTO.builder()
                                        .title("Unassigned task")
                                        .description("No one assigned yet")
                                        .status(TaskStatus.TODO)
                                        .build(); // assigneeId = null

                        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

                        Task savedTask = Task.builder()
                                        .id(201L)
                                        .title(dto.getTitle())
                                        .description(dto.getDescription())
                                        .status(dto.getStatus())
                                        .priority(TaskPriority.MEDIUM)
                                        .orderIndex(0)
                                        .createdAt(LocalDateTime.now())
                                        .project(project)
                                        .assignee(null)
                                        .build();

                        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

                        TaskDTO result = taskService.createTask(project.getId(), dto);

                        assertThat(result.getAssigneeId()).isNull();
                        assertThat(result.getAssigneeName()).isNull();
                }

                @Test
                @DisplayName("Lança RuntimeException quando o projeto não é encontrado")
                void projectNotFound_throws() {
                        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

                        TaskDTO dto = TaskDTO.builder()
                                        .title("Task")
                                        .description("desc")
                                        .status(TaskStatus.TODO)
                                        .build();

                        assertThatRuntimeException()
                                        .isThrownBy(() -> taskService.createTask(project.getId(), dto))
                                        .withMessageContaining("Project not found");
                }

                @Test
                @DisplayName("Lança RuntimeException quando o responsável não é encontrado")
                void assigneeNotFound_throws() {
                        TaskDTO dto = TaskDTO.builder()
                                        .title("Task")
                                        .description("desc")
                                        .status(TaskStatus.TODO)
                                        .assigneeId(999L)
                                        .build();

                        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
                        when(userRepository.findById(999L)).thenReturn(Optional.empty());

                        assertThatRuntimeException()
                                        .isThrownBy(() -> taskService.createTask(project.getId(), dto))
                                        .withMessageContaining("Assignee not found");
                }

                @Test
                @DisplayName("Incrementa counter taskflow.tasks.created ao criar tarefa")
                void validPayload_incrementsCreatedCounter() {
                        TaskDTO dto = TaskDTO.builder()
                                        .title("Counted task")
                                        .description("desc")
                                        .status(TaskStatus.TODO)
                                        .build();

                        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

                        Task savedTask = Task.builder()
                                        .id(300L).title(dto.getTitle()).description(dto.getDescription())
                                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM)
                                        .orderIndex(0).createdAt(LocalDateTime.now())
                                        .project(project).assignee(null).build();

                        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

                        taskService.createTask(project.getId(), dto);

                        verify(tasksCreatedCounter).increment();
                }

                @Test
                @DisplayName("Lança AccessDeniedException quando o responsável não é membro do projeto")
                void assigneeNotMember_throwsAccessDenied() {
                        TaskDTO dto = TaskDTO.builder()
                                        .title("Task")
                                        .description("desc")
                                        .status(TaskStatus.TODO)
                                        .assigneeId(assigneeUser.getId())
                                        .build();

                        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
                        when(userRepository.findById(assigneeUser.getId())).thenReturn(Optional.of(assigneeUser));
                        // assignee is NOT a member
                        when(projectMemberRepository.findByProjectIdAndUserId(project.getId(), assigneeUser.getId()))
                                        .thenReturn(Optional.empty());

                        assertThatExceptionOfType(AccessDeniedException.class)
                                        .isThrownBy(() -> taskService.createTask(project.getId(), dto));
                }
        }

        // =========================================================================
        // updateTask
        // =========================================================================
        @Nested
        @DisplayName("updateTask — atualização de tarefa")
        class UpdateTask {

                @Test
                @DisplayName("Atualiza e retorna a tarefa quando os dados são válidos")
                void validUpdate_returnsUpdatedDto() {
                        TaskDTO dto = TaskDTO.builder()
                                        .title("Updated title")
                                        .description("Updated description")
                                        .status(TaskStatus.DONE)
                                        .build();

                        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

                        Task updatedTask = Task.builder()
                                        .id(task.getId())
                                        .title(dto.getTitle())
                                        .description(dto.getDescription())
                                        .status(TaskStatus.DONE)
                                        .priority(task.getPriority())
                                        .orderIndex(task.getOrderIndex())
                                        .createdAt(task.getCreatedAt())
                                        .project(project)
                                        .assignee(null)
                                        .build();

                        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

                        TaskDTO result = taskService.updateTask(project.getId(), task.getId(), dto);

                        assertThat(result.getTitle()).isEqualTo("Updated title");
                        assertThat(result.getStatus()).isEqualTo(TaskStatus.DONE);
                        assertThat(result.getAssigneeId()).isNull();
                }

                @Test
                @DisplayName("Lança RuntimeException quando a tarefa não pertence ao projeto informado")
                void taskNotInProject_throws() {
                        Task taskInOtherProject = Task.builder()
                                        .id(task.getId())
                                        .title("Other")
                                        .status(TaskStatus.TODO)
                                        .priority(TaskPriority.LOW)
                                        .orderIndex(0)
                                        .createdAt(LocalDateTime.now())
                                        .project(Project.builder().id(99L).name("Other").build())
                                        .build();

                        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(taskInOtherProject));

                        assertThatRuntimeException()
                                        .isThrownBy(() -> taskService.updateTask(project.getId(), task.getId(),
                                                        TaskDTO.builder().title("t").description("d")
                                                                        .status(TaskStatus.TODO).build()))
                                        .withMessageContaining("does not belong to this project");
                }
        }

        // =========================================================================
        // deleteTask
        // =========================================================================
        @Nested
        @DisplayName("deleteTask — exclusão de tarefa")
        class DeleteTask {

                @Test
                @DisplayName("Remove a tarefa quando o usuário tem acesso e a tarefa pertence ao projeto")
                void validDelete_callsRepositoryDelete() {
                        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

                        taskService.deleteTask(project.getId(), task.getId());

                        verify(taskRepository).delete(task);
                }

                @Test
                @DisplayName("Lança RuntimeException quando a tarefa não é encontrada")
                void taskNotFound_throws() {
                        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

                        assertThatRuntimeException()
                                        .isThrownBy(() -> taskService.deleteTask(project.getId(), 999L))
                                        .withMessageContaining("Task not found");
                }

                @Test
                @DisplayName("Lança AccessDeniedException quando o usuário não é membro ao tentar excluir")
                void nonMember_throwsAccessDenied() {
                        when(projectMemberRepository.findByProjectIdAndUserId(project.getId(), currentUser.getId()))
                                        .thenReturn(Optional.empty());

                        assertThatExceptionOfType(AccessDeniedException.class)
                                        .isThrownBy(() -> taskService.deleteTask(project.getId(), task.getId()));
                }
        }

        // =========================================================================
        // User resolution
        // =========================================================================
        @Nested
        @DisplayName("Resolução do usuário autenticado (getCurrentUser)")
        class UserResolution {

                @Test
                @DisplayName("Lança RuntimeException quando o e-mail do usuário autenticado não existe no banco")
                void unknownEmail_throws() {
                        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

                        assertThatRuntimeException()
                                        .isThrownBy(() -> taskService.getTasksByProject(project.getId()))
                                        .withMessageContaining("User not found");
                }
        }
}
