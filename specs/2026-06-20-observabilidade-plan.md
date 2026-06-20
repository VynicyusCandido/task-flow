# Observabilidade — Plano de Implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Adicionar logs JSON estruturados, métricas via Prometheus e Correlation ID ao TaskFlow (backend Spring Boot + frontend Next.js).

**Architecture:** Um `CorrelationIdFilter` propaga um `X-Correlation-ID` por request via MDC. O Logback serializa cada log como JSON incluindo esse ID automaticamente. O Micrometer expõe métricas HTTP automáticas via `/actuator/prometheus` e 3 counters de negócio incrementados nos services. O frontend captura o ID e loga erros estruturados via `logger.ts`.

**Tech Stack:** Spring Boot 3.4.3 · Micrometer · Logback + logstash-logback-encoder 7.4 · Spring Boot Actuator · Next.js 14 (fetch nativo)

## Global Constraints

- Java 21, Spring Boot 3.4.3 — não alterar versões
- Package raiz: `com.example.taskflow`
- Commits em Conventional Commits one-line: `type(scope): mensagem`
- Nenhuma lógica de negócio alterada — apenas adicionar logs/métricas ao código existente
- Frontend usa `fetch` nativo (Server Actions) — não há Axios

---

### Task 1: Dependências Maven e arquivos de configuração

**Files:**
- Modify: `taskflow-api/pom.xml`
- Modify: `taskflow-api/src/main/resources/application.properties`
- Create: `taskflow-api/src/main/resources/logback-spring.xml`

**Interfaces:**
- Produces: dependências disponíveis para todos os tasks seguintes; perfil `prod` ativa logs JSON

- [ ] **Step 1: Adicionar as 4 dependências ao `pom.xml`**

Inserir após a dependência `spring-boot-starter-test` (antes de `</dependencies>`):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

- [ ] **Step 2: Adicionar propriedades ao `application.properties`**

Adicionar ao final do arquivo:

```properties
# Actuator
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.tags.application=taskflow-api

# Log level para o pacote da aplicação
logging.level.com.example.taskflow=INFO
```

- [ ] **Step 3: Criar `logback-spring.xml`**

Criar em `taskflow-api/src/main/resources/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Perfil padrão (dev): texto legível com correlationId -->
    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} %-5level %logger{36} [%X{correlationId}] - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <!-- Perfil prod: JSON estruturado com todos os campos MDC -->
    <springProfile name="prod">
        <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <includeMdcKeyName>correlationId</includeMdcKeyName>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
        </root>
    </springProfile>

</configuration>
```

> Em produção (Render), definir a variável de ambiente `SPRING_PROFILES_ACTIVE=prod` para ativar o output JSON.

- [ ] **Step 4: Verificar compilação**

```bash
cd taskflow-api && ./mvnw compile -q
```

Esperado: BUILD SUCCESS sem erros.

- [ ] **Step 5: Commit**

```bash
git add taskflow-api/pom.xml taskflow-api/src/main/resources/application.properties taskflow-api/src/main/resources/logback-spring.xml
git commit -m "chore(observability): add actuator, micrometer, aop and logback-json deps"
```

---

### Task 2: CorrelationIdFilter

**Files:**
- Create: `taskflow-api/src/main/java/com/example/taskflow/security/CorrelationIdFilter.java`
- Create: `taskflow-api/src/test/java/com/example/taskflow/security/CorrelationIdFilterTest.java`

**Interfaces:**
- Produces: header `X-Correlation-ID` em toda resposta HTTP; chave `correlationId` no MDC durante o request

- [ ] **Step 1: Escrever os testes que devem falhar**

Criar `CorrelationIdFilterTest.java`:

```java
package com.example.taskflow.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void whenNoHeader_generatesUUIDAndSetsResponseHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        String correlationId = response.getHeader("X-Correlation-ID");
        assertThat(correlationId).isNotNull().matches("[0-9a-f\\-]{36}");
        verify(chain).doFilter(request, response);
    }

    @Test
    void whenHeaderProvided_usesExistingId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-ID", "custom-id-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader("X-Correlation-ID")).isEqualTo("custom-id-123");
    }

    @Test
    void afterRequest_mdcIsCleared() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(MDC.get("correlationId")).isNull();
    }
}
```

- [ ] **Step 2: Rodar para confirmar falha**

```bash
cd taskflow-api && ./mvnw test -Dtest=CorrelationIdFilterTest -q 2>&1 | tail -5
```

Esperado: FAIL — `CorrelationIdFilter` não existe ainda.

- [ ] **Step 3: Criar `CorrelationIdFilter.java`**

```java
package com.example.taskflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String id = request.getHeader(HEADER);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, id);
        response.setHeader(HEADER, id);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
```

- [ ] **Step 4: Rodar para confirmar aprovação**

```bash
cd taskflow-api && ./mvnw test -Dtest=CorrelationIdFilterTest -q 2>&1 | tail -5
```

Esperado: BUILD SUCCESS, Tests run: 3, Failures: 0.

- [ ] **Step 5: Commit**

```bash
git add taskflow-api/src/main/java/com/example/taskflow/security/CorrelationIdFilter.java \
        taskflow-api/src/test/java/com/example/taskflow/security/CorrelationIdFilterTest.java
git commit -m "feat(observability): add CorrelationIdFilter for request tracing"
```

---

### Task 3: ObservabilityConfig + SecurityConfig + @Timed nos controllers

**Files:**
- Create: `taskflow-api/src/main/java/com/example/taskflow/config/ObservabilityConfig.java`
- Modify: `taskflow-api/src/main/java/com/example/taskflow/config/SecurityConfig.java`
- Modify: `taskflow-api/src/main/java/com/example/taskflow/controller/TaskController.java`
- Modify: `taskflow-api/src/main/java/com/example/taskflow/controller/ProjectController.java`

**Interfaces:**
- Consumes: `spring-boot-starter-aop` (Task 1), `spring-boot-starter-actuator` (Task 1)
- Produces: bean `TimedAspect`; endpoints `/actuator/health` e `/actuator/prometheus` públicos; todos os métodos dos controllers temporizados

- [ ] **Step 1: Criar `ObservabilityConfig.java`**

```java
package com.example.taskflow.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
```

- [ ] **Step 2: Liberar endpoints do Actuator no `SecurityConfig.java`**

Localizar a linha:
```java
.requestMatchers("/api/auth/**").permitAll()
```

Substituir por:
```java
.requestMatchers("/api/auth/**", "/actuator/health", "/actuator/prometheus").permitAll()
```

- [ ] **Step 3: Adicionar `@Timed` ao `TaskController.java`**

Adicionar import e anotação na classe:

```java
import io.micrometer.core.annotation.Timed;

@Timed(value = "taskflow.tasks.controller", description = "Task controller execution time")
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {
```

- [ ] **Step 4: Adicionar `@Timed` ao `ProjectController.java`**

```java
import io.micrometer.core.annotation.Timed;

@Timed(value = "taskflow.projects.controller", description = "Project controller execution time")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
```

- [ ] **Step 5: Verificar compilação**

```bash
cd taskflow-api && ./mvnw compile -q
```

Esperado: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add taskflow-api/src/main/java/com/example/taskflow/config/ObservabilityConfig.java \
        taskflow-api/src/main/java/com/example/taskflow/config/SecurityConfig.java \
        taskflow-api/src/main/java/com/example/taskflow/controller/TaskController.java \
        taskflow-api/src/main/java/com/example/taskflow/controller/ProjectController.java
git commit -m "feat(observability): add TimedAspect, expose actuator endpoints, @Timed on controllers"
```

---

### Task 4: Logs e métricas no TaskService

**Files:**
- Modify: `taskflow-api/src/main/java/com/example/taskflow/service/TaskService.java`
- Modify: `taskflow-api/src/test/java/com/example/taskflow/service/TaskServiceTest.java`

**Interfaces:**
- Consumes: `MeterRegistry` (auto-wired pelo Spring via actuator)
- Produces: counter `taskflow.tasks.created` incrementado em `createTask`; logs INFO/WARN nas operações principais

- [ ] **Step 1: Escrever teste para o counter antes de implementar**

No `TaskServiceTest.java`, adicionar os seguintes imports e campos:

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
```

Adicionar mocks no bloco de campos (junto com os outros `@Mock`):

```java
@Mock
private MeterRegistry meterRegistry;
@Mock
private Counter tasksCreatedCounter;
```

Adicionar no `setUp()`, após as outras configurações de mock:

```java
when(meterRegistry.counter("taskflow.tasks.created")).thenReturn(tasksCreatedCounter);
```

Adicionar um novo teste dentro de `CreateTask`:

```java
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
```

- [ ] **Step 2: Rodar para confirmar falha**

```bash
cd taskflow-api && ./mvnw test -Dtest=TaskServiceTest -q 2>&1 | tail -5
```

Esperado: FAIL — `TaskService` não aceita `MeterRegistry` ainda.

- [ ] **Step 3: Atualizar `TaskService.java`**

Adicionar imports:

```java
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
```

Adicionar `@Slf4j` na classe e `MeterRegistry` como dependência:

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final MeterRegistry meterRegistry;
```

Atualizar `ensureMemberAccess` para logar acesso negado:

```java
private void ensureMemberAccess(Long projectId, Long userId) {
    projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> {
                log.warn("Access denied: userId={}, projectId={}", userId, projectId);
                return new AccessDeniedException("You do not have access to this project");
            });
}
```

Atualizar o final de `createTask`, substituindo o `return mapToDto(taskRepository.save(task));` por:

```java
Task saved = taskRepository.save(task);
meterRegistry.counter("taskflow.tasks.created").increment();
log.info("Task created: id={}, project={}, assignee={}", saved.getId(), projectId, dto.getAssigneeId());
return mapToDto(saved);
```

Adicionar log em `updateTask`, antes do `return`:

```java
log.info("Task updated: id={}, project={}", taskId, projectId);
return mapToDto(taskRepository.save(task));
```

Adicionar log em `deleteTask`, antes do `taskRepository.delete(task)`:

```java
log.info("Task deleted: id={}, project={}", taskId, projectId);
taskRepository.delete(task);
```

- [ ] **Step 4: Rodar todos os testes do TaskService**

```bash
cd taskflow-api && ./mvnw test -Dtest=TaskServiceTest -q 2>&1 | tail -5
```

Esperado: BUILD SUCCESS, todos os testes passando (incluindo o novo).

- [ ] **Step 5: Commit**

```bash
git add taskflow-api/src/main/java/com/example/taskflow/service/TaskService.java \
        taskflow-api/src/test/java/com/example/taskflow/service/TaskServiceTest.java
git commit -m "feat(observability): add @Slf4j and tasks.created counter to TaskService"
```

---

### Task 5: Logs e métricas no ProjectService + ProjectServiceTest

**Files:**
- Modify: `taskflow-api/src/main/java/com/example/taskflow/service/ProjectService.java`
- Create: `taskflow-api/src/test/java/com/example/taskflow/service/ProjectServiceTest.java`

**Interfaces:**
- Produces: counter `taskflow.projects.created`; logs INFO/WARN nas operações principais

- [ ] **Step 1: Escrever `ProjectServiceTest.java`**

```java
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
```

- [ ] **Step 2: Rodar para confirmar falha**

```bash
cd taskflow-api && ./mvnw test -Dtest=ProjectServiceTest -q 2>&1 | tail -5
```

Esperado: FAIL — `ProjectService` não aceita `MeterRegistry` ainda.

- [ ] **Step 3: Atualizar `ProjectService.java`**

Adicionar imports:

```java
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
```

Adicionar `@Slf4j` e `MeterRegistry` na classe:

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;
```

Atualizar `createProject`, adicionando counter e log após `projectMemberRepository.save(member)`:

```java
projectMemberRepository.save(member);
meterRegistry.counter("taskflow.projects.created").increment();
log.info("Project created: id={}, owner={}", project.getId(), currentUser.getId());
return mapToDto(project);
```

Atualizar `inviteMember`, adicionando log antes do `return`:

```java
log.info("Member invited: project={}, email={}", projectId, request.getEmail());
return mapMemberToDto(projectMemberRepository.save(member));
```

Atualizar `ensureOwnerAccess`, adicionando log quando role não é OWNER:

```java
private void ensureOwnerAccess(Long projectId, Long userId) {
    ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new AccessDeniedException("You do not have access to this project"));

    if (member.getRole() != ProjectRole.OWNER) {
        log.warn("Owner access required: userId={}, projectId={}", userId, projectId);
        throw new AccessDeniedException("Only project owners can perform this action");
    }
}
```

- [ ] **Step 4: Rodar testes**

```bash
cd taskflow-api && ./mvnw test -Dtest=ProjectServiceTest -q 2>&1 | tail -5
```

Esperado: BUILD SUCCESS, Tests run: 2, Failures: 0.

- [ ] **Step 5: Rodar suite completa para garantir que nada quebrou**

```bash
cd taskflow-api && ./mvnw test -q 2>&1 | tail -5
```

Esperado: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add taskflow-api/src/main/java/com/example/taskflow/service/ProjectService.java \
        taskflow-api/src/test/java/com/example/taskflow/service/ProjectServiceTest.java
git commit -m "feat(observability): add @Slf4j and projects.created counter to ProjectService"
```

---

### Task 6: Logs e métricas no AuthController

**Files:**
- Modify: `taskflow-api/src/main/java/com/example/taskflow/controller/AuthController.java`

**Interfaces:**
- Produces: counter `taskflow.auth.failures` incrementado em falhas de login; logs INFO em login/registro

- [ ] **Step 1: Atualizar `AuthController.java`**

Adicionar imports:

```java
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
```

Adicionar `@Slf4j` na classe e `MeterRegistry` como dependência:

```java
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MeterRegistry meterRegistry;
```

Atualizar `register()`, adicionando log após `userRepository.save(user)`:

```java
userRepository.save(user);
log.info("User registered: email={}", request.getEmail());
```

Substituir o corpo do método `login()` inteiro por:

```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
    } catch (Exception e) {
        meterRegistry.counter("taskflow.auth.failures").increment();
        log.warn("Authentication failed: email={}", request.getEmail());
        throw e;
    }

    var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    log.info("User logged in: email={}", request.getEmail());

    return ResponseEntity.ok(AuthResponse.builder()
            .token(jwtToken)
            .name(user.getName())
            .email(user.getEmail())
            .build());
}
```

- [ ] **Step 2: Verificar compilação e suite completa**

```bash
cd taskflow-api && ./mvnw test -q 2>&1 | tail -5
```

Esperado: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add taskflow-api/src/main/java/com/example/taskflow/controller/AuthController.java
git commit -m "feat(observability): add @Slf4j and auth.failures counter to AuthController"
```

---

### Task 7: Frontend — `logger.ts`

**Files:**
- Create: `taskflow-app/src/lib/logger.ts`

**Interfaces:**
- Produces: `logger.info(msg, ctx?)`, `logger.warn(msg, ctx?)`, `logger.error(msg, ctx?)` — usados no Task 8

- [ ] **Step 1: Criar `src/lib/logger.ts`**

```typescript
type LogLevel = 'info' | 'warn' | 'error';

interface LogEntry {
  level: LogLevel;
  message: string;
  timestamp: string;
  [key: string]: unknown;
}

function log(level: LogLevel, message: string, context: Record<string, unknown> = {}): void {
  const entry: LogEntry = {
    level,
    message,
    timestamp: new Date().toISOString(),
    ...context,
  };
  console[level](JSON.stringify(entry));
}

export const logger = {
  info:  (message: string, context?: Record<string, unknown>) => log('info',  message, context),
  warn:  (message: string, context?: Record<string, unknown>) => log('warn',  message, context),
  error: (message: string, context?: Record<string, unknown>) => log('error', message, context),
};
```

- [ ] **Step 2: Verificar TypeScript**

```bash
cd taskflow-app && npx tsc --noEmit 2>&1 | head -20
```

Esperado: sem erros.

- [ ] **Step 3: Commit**

```bash
git add taskflow-app/src/lib/logger.ts
git commit -m "feat(observability): add structured logger utility to frontend"
```

---

### Task 8: Frontend — interceptor de erro em `api.ts`

**Files:**
- Modify: `taskflow-app/src/lib/api.ts`

**Interfaces:**
- Consumes: `logger` de `@/lib/logger` (Task 7); `X-Correlation-ID` header da resposta do backend (Task 2)
- Produces: log estruturado em todo erro HTTP com `{ endpoint, method, status, correlationId }`

- [ ] **Step 1: Atualizar `src/lib/api.ts`**

Substituir o conteúdo completo do arquivo por:

```typescript
import { cookies } from "next/headers";
import { Auth } from "@/app/enums";
import { logger } from "@/lib/logger";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_BASE_URL) {
  throw new Error("NEXT_PUBLIC_API_URL is not defined");
}

export async function fetchApi(endpoint: string, options: RequestInit = {}) {
  const cookieStore = await cookies();
  const token = cookieStore.get(Auth.AUTH_TOKEN)?.value;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (options.headers && !Array.isArray(options.headers) && !(options.headers instanceof Headers)) {
    Object.assign(headers, options.headers);
  }

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    logger.error("API request failed", {
      endpoint,
      method: options.method ?? "GET",
      status: response.status,
      correlationId: response.headers.get("X-Correlation-ID") ?? undefined,
    });
  }

  return response;
}
```

- [ ] **Step 2: Verificar TypeScript**

```bash
cd taskflow-app && npx tsc --noEmit 2>&1 | head -20
```

Esperado: sem erros.

- [ ] **Step 3: Rodar build do frontend para confirmar ausência de erros de runtime**

```bash
cd taskflow-app && npm run build 2>&1 | tail -10
```

Esperado: compiled successfully.

- [ ] **Step 4: Commit**

```bash
git add taskflow-app/src/lib/api.ts
git commit -m "feat(observability): log structured errors with correlationId in fetchApi"
```

---

## Checklist de verificação final

Após concluir todos os tasks, validar:

- [ ] `GET /actuator/health` retorna `{"status":"UP"}` sem autenticação
- [ ] `GET /actuator/prometheus` retorna métricas no formato Prometheus sem autenticação
- [ ] Um request para `/api/projects` com token válido retorna header `X-Correlation-ID` na resposta
- [ ] Logs do backend mostram `[correlationId]` (dev) ou campo JSON `correlationId` (prod)
- [ ] Após criar uma task, a métrica `taskflow_tasks_created_total` aparece em `/actuator/prometheus`
- [ ] Após criar um projeto, `taskflow_projects_created_total` aparece em `/actuator/prometheus`
- [ ] Um login com credenciais erradas incrementa `taskflow_auth_failures_total`
- [ ] Console do servidor Next.js mostra JSON com `correlationId` em erros HTTP
