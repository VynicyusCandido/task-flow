package com.example.taskflow.validation;

import com.example.taskflow.dtos.task.TaskDTO;
import com.example.taskflow.model.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link TaskValidator}.
 *
 * These tests verify the validation rules in isolation, without Spring context.
 */
@DisplayName("TaskValidator — testes unitários")
class TaskValidatorTest {

    private TaskValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TaskValidator();
    }

    // =========================================================================
    // Helper: build a fully valid DTO
    // =========================================================================
    private TaskDTO validDto() {
        return TaskDTO.builder()
                .title("Fix login bug")
                .description("The login button is not working on mobile")
                .status(TaskStatus.TODO)
                .assigneeId(1L)
                .build();
    }

    // =========================================================================
    // Null / empty DTO
    // =========================================================================
    @Nested
    @DisplayName("Payload nulo")
    class NullPayload {

        @Test
        @DisplayName("collectErrors retorna apenas um erro quando o DTO é nulo")
        void nullDto_returnsEarlyError() {
            List<String> errors = validator.collectErrors(null);
            assertThat(errors).hasSize(1)
                    .first().asString().contains("must not be null");
        }

        @Test
        @DisplayName("validate lança excessão quando o DTO é nulo")
        void nullDto_throwsOnValidate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validate(null))
                    .withMessageContaining("must not be null");
        }
    }

    // =========================================================================
    // Title
    // =========================================================================
    @Nested
    @DisplayName("Validação do título")
    class TitleValidation {

        @Test
        @DisplayName("isTitleValid retorna true quando o título está preenchido")
        void titlePresent_valid() {
            assertThat(validator.isTitleValid(validDto())).isTrue();
        }

        @Test
        @DisplayName("isTitleValid retorna false quando o título é nulo")
        void titleNull_invalid() {
            TaskDTO dto = validDto();
            dto.setTitle(null);
            assertThat(validator.isTitleValid(dto)).isFalse();
        }

        @Test
        @DisplayName("isTitleValid retorna false quando o título é vazio/em branco")
        void titleBlank_invalid() {
            TaskDTO dto = validDto();
            dto.setTitle("   ");
            assertThat(validator.isTitleValid(dto)).isFalse();
        }

        @Test
        @DisplayName("validate lança excessão quando o título é vazio")
        void titleBlank_throwsOnValidate() {
            TaskDTO dto = validDto();
            dto.setTitle("");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validate(dto))
                    .withMessageContaining("Title must not be blank");
        }
    }

    // =========================================================================
    // Description
    // =========================================================================
    @Nested
    @DisplayName("Validação da descrição")
    class DescriptionValidation {

        @Test
        @DisplayName("isDescriptionValid retorna true quando a descrição está preenchida")
        void descriptionPresent_valid() {
            assertThat(validator.isDescriptionValid(validDto())).isTrue();
        }

        @Test
        @DisplayName("isDescriptionValid retorna false quando a descrição é nula")
        void descriptionNull_invalid() {
            TaskDTO dto = validDto();
            dto.setDescription(null);
            assertThat(validator.isDescriptionValid(dto)).isFalse();
        }

        @Test
        @DisplayName("isDescriptionValid retorna false quando a descrição é vazia/em branco")
        void descriptionBlank_invalid() {
            TaskDTO dto = validDto();
            dto.setDescription("   ");
            assertThat(validator.isDescriptionValid(dto)).isFalse();
        }

        @Test
        @DisplayName("validate lança excessão quando a descrição é vazia")
        void descriptionBlank_throwsOnValidate() {
            TaskDTO dto = validDto();
            dto.setDescription("");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validate(dto))
                    .withMessageContaining("Description must not be blank");
        }
    }

    // =========================================================================
    // Status
    // =========================================================================
    @Nested
    @DisplayName("Validação do status")
    class StatusValidation {

        @Test
        @DisplayName("isStatusValid retorna true quando o status está definido")
        void statusPresent_valid() {
            assertThat(validator.isStatusValid(validDto())).isTrue();
        }

        @Test
        @DisplayName("isStatusValid retorna false quando o status é nulo")
        void statusNull_invalid() {
            TaskDTO dto = validDto();
            dto.setStatus(null);
            assertThat(validator.isStatusValid(dto)).isFalse();
        }

        @Test
        @DisplayName("validate lança excessão quando o status é nulo")
        void statusNull_throwsOnValidate() {
            TaskDTO dto = validDto();
            dto.setStatus(null);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validate(dto))
                    .withMessageContaining("Status must not be null");
        }

        @Test
        @DisplayName("validate aceita todos os valores válidos de TaskStatus")
        void allStatusValues_valid() {
            for (TaskStatus status : TaskStatus.values()) {
                TaskDTO dto = validDto();
                dto.setStatus(status);
                assertThatNoException().isThrownBy(() -> validator.validate(dto));
            }
        }
    }

    // =========================================================================
    // Project ID
    // =========================================================================
    @Nested
    @DisplayName("Validação do ID do projeto")
    class ProjectIdValidation {

        @Test
        @DisplayName("isProjectIdValid retorna true para um ID positivo")
        void positiveId_valid() {
            assertThat(validator.isProjectIdValid(1L)).isTrue();
            assertThat(validator.isProjectIdValid(99L)).isTrue();
        }

        @Test
        @DisplayName("isProjectIdValid retorna false para nulo")
        void nullId_invalid() {
            assertThat(validator.isProjectIdValid(null)).isFalse();
        }

        @Test
        @DisplayName("isProjectIdValid retorna false para zero")
        void zeroId_invalid() {
            assertThat(validator.isProjectIdValid(0L)).isFalse();
        }

        @Test
        @DisplayName("isProjectIdValid retorna false para valores negativos")
        void negativeId_invalid() {
            assertThat(validator.isProjectIdValid(-5L)).isFalse();
        }

        @Test
        @DisplayName("validateProjectId lança excessão quando o ID é nulo")
        void nullId_throwsOnValidate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validateProjectId(null))
                    .withMessageContaining("valid project ID");
        }

        @Test
        @DisplayName("validateProjectId lança excessão quando o ID é zero")
        void zeroId_throwsOnValidate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validateProjectId(0L))
                    .withMessageContaining("valid project ID");
        }
    }

    // =========================================================================
    // Assignee ID
    // =========================================================================
    @Nested
    @DisplayName("Validação do ID do responsável")
    class AssigneeIdValidation {

        @Test
        @DisplayName("isAssigneeIdValid retorna true quando assigneeId é nulo (tarefa sem responsável)")
        void nullAssignee_valid() {
            assertThat(validator.isAssigneeIdValid(null)).isTrue();
        }

        @Test
        @DisplayName("isAssigneeIdValid retorna true para um ID positivo")
        void positiveAssignee_valid() {
            assertThat(validator.isAssigneeIdValid(42L)).isTrue();
        }

        @Test
        @DisplayName("isAssigneeIdValid retorna false para zero")
        void zeroAssignee_invalid() {
            assertThat(validator.isAssigneeIdValid(0L)).isFalse();
        }

        @Test
        @DisplayName("isAssigneeIdValid retorna false para valor negativo")
        void negativeAssignee_invalid() {
            assertThat(validator.isAssigneeIdValid(-1L)).isFalse();
        }

        @Test
        @DisplayName("validateAssigneeId lança excessão quando o valor é zero")
        void zeroAssignee_throwsOnValidate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validateAssigneeId(0L))
                    .withMessageContaining("positive number");
        }

        @Test
        @DisplayName("validateAssigneeId não lança excessão quando assigneeId é nulo")
        void nullAssignee_doesNotThrow() {
            assertThatNoException().isThrownBy(() -> validator.validateAssigneeId(null));
        }
    }

    // =========================================================================
    // validateAll (full pipeline)
    // =========================================================================
    @Nested
    @DisplayName("validateAll — validações combinadas")
    class ValidateAll {

        @Test
        @DisplayName("validateAll não lança excessão para uma tarefa completamente válida")
        void fullyValid_noException() {
            assertThatNoException()
                    .isThrownBy(() -> validator.validateAll(1L, validDto()));
        }

        @Test
        @DisplayName("validateAll lança excessão quando o projectId é nulo")
        void invalidProjectId_throws() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validateAll(null, validDto()))
                    .withMessageContaining("valid project ID");
        }

        @Test
        @DisplayName("validateAll lança excessão quando o título está ausente")
        void missingTitle_throws() {
            TaskDTO dto = validDto();
            dto.setTitle(null);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validateAll(1L, dto))
                    .withMessageContaining("Title must not be blank");
        }

        @Test
        @DisplayName("validateAll lança excessão quando o status está ausente")
        void missingStatus_throws() {
            TaskDTO dto = validDto();
            dto.setStatus(null);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validateAll(1L, dto))
                    .withMessageContaining("Status must not be null");
        }

        @Test
        @DisplayName("validateAll lança excessão quando o assigneeId é negativo")
        void negativeAssigneeId_throws() {
            TaskDTO dto = validDto();
            dto.setAssigneeId(-10L);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> validator.validateAll(1L, dto))
                    .withMessageContaining("positive number");
        }

        @Test
        @DisplayName("collectErrors acumula múltiplos erros ao mesmo tempo")
        void multipleErrors_collectedTogether() {
            TaskDTO dto = TaskDTO.builder().build(); // all fields null/missing
            List<String> errors = validator.collectErrors(dto);
            assertThat(errors)
                    .hasSizeGreaterThanOrEqualTo(3)
                    .anyMatch(e -> e.contains("Title"))
                    .anyMatch(e -> e.contains("Description"))
                    .anyMatch(e -> e.contains("Status"));
        }
    }
}
