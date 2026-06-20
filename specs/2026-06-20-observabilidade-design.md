# Observabilidade — TaskFlow

**Data:** 2026-06-20
**Escopo:** Backend (Spring Boot API) + Frontend (Next.js)
**Nível:** Intermediário — Actuator + Micrometer + Logs JSON + Correlation ID

---

## Contexto

O PDF de recomendações da disciplina (slide 5) destaca Observabilidade como item obrigatório para a fase final, cobrindo:
- Logs gerenciáveis
- Métricas: latency, error rate, throughput
- Rastreamento básico de requests entre serviços

O projeto atualmente não possui nenhuma dessas três camadas implementadas.

---

## Arquitetura

```
Frontend (Next.js)                Backend (Spring Boot)
─────────────────                 ──────────────────────────────────────
                                  ┌─ CorrelationIdFilter
Axios request  ──────────────────►│  gera/lê X-Correlation-ID
                                  │  injeta no MDC → aparece em todo log
Axios interceptor                 │
← lê header X-Correlation-ID     └─ Controllers (@Timed)
  armazena para logs de erro         ↓ latency/count/errors automáticos
                                  Services (@Slf4j + JSON logs)
logger.ts                            ↓
  log estruturado no console      Logback (logstash-logback-encoder)
  { correlationId, status,           → JSON: {timestamp, level,
    endpoint, message }               correlationId, message}
                                        ↓
                                  /actuator/prometheus
```

### Componentes novos

| Arquivo | Responsabilidade |
|---|---|
| `CorrelationIdFilter.java` | `OncePerRequestFilter` — gera UUID se ausente, injeta no MDC, devolve no header de resposta, limpa MDC ao final |
| `ObservabilityConfig.java` | Bean `TimedAspect` + bean `MeterRegistry` customizações |
| `logback-spring.xml` | Output JSON via `logstash-logback-encoder` em produção, texto em dev |
| `src/lib/logger.ts` | Wrapper sobre `console` com estrutura JSON consistente |
| Interceptor em `src/lib/api.ts` | Captura `X-Correlation-ID` e chama `logger.error` nos erros HTTP |

---

## Métricas

### Automáticas (zero código extra)
| Métrica | Tipo | Fonte |
|---|---|---|
| `http.server.requests` | Timer | `@Timed` nos controllers via `TimedAspect` — latency, count, error rate por endpoint |
| `hikaricp.connections.*` | Gauge | Actuator auto-config — pool de conexões DB |
| `jvm.memory.*` | Gauge | Actuator auto-config — heap e non-heap |

### Customizadas (counters de negócio)
| Métrica | Onde é incrementada |
|---|---|
| `taskflow.tasks.created` | `TaskService.createTask()` |
| `taskflow.projects.created` | `ProjectService.createProject()` |
| `taskflow.auth.failures` | `AuthController.login()` no bloco catch |

Todas expostas em `/actuator/prometheus` para scrape.

---

## Logs estruturados

### Formato JSON (Logback + logstash-logback-encoder)

```json
{
  "timestamp": "2026-06-20T14:32:01.123Z",
  "level": "INFO",
  "logger": "TaskService",
  "correlationId": "a3f9c1d2-7b4e-4f1a-9c3d-2e5f8a1b6c9d",
  "message": "Task created: id=42, project=10, assignee=2"
}
```

### Pontos de log por classe

**`AuthController`**
- `INFO` — login bem-sucedido (email, sem senha)
- `WARN` — falha de autenticação (email, motivo)
- `INFO` — novo registro (email)

**`TaskService`**
- `INFO` — task criada (id, projectId, assigneeId)
- `INFO` — task atualizada (id, campos alterados)
- `INFO` — task deletada (id, projectId)
- `WARN` — acesso negado (userId, projectId)

**`ProjectService`**
- `INFO` — projeto criado (id, ownerId)
- `INFO` — membro convidado (projectId, targetEmail)
- `WARN` — acesso negado (userId, projectId)

---

## Correlation ID

- O `CorrelationIdFilter` lê o header `X-Correlation-ID` do request de entrada
- Se ausente, gera um `UUID.randomUUID()`
- Injeta no MDC sob a chave `correlationId` — aparece automaticamente em todos os logs do request via Logback
- Adiciona o ID no header da resposta para que o cliente possa correlacionar
- Limpa o MDC em `finally` para evitar vazamento entre threads do pool

---

## Dependências (pom.xml)

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
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

---

## Configuração (application.properties)

```properties
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.tags.application=taskflow-api
logging.level.com.example.taskflow=INFO
```

---

## Segurança dos endpoints do Actuator

`/actuator/health` e `/actuator/prometheus` ficam públicos — não expõem dados de usuário, apenas métricas agregadas e status de saúde. Padrão adotado pela maioria dos projetos que usam Prometheus.

Mudança em `SecurityConfig.java`:
```java
.requestMatchers("/api/auth/**", "/actuator/health", "/actuator/prometheus").permitAll()
```

---

## Frontend

### `src/lib/logger.ts`

Wrapper leve sem dependência externa. Formata `console.error/warn/info` com campos estruturados:

```ts
logger.error("Task creation failed", {
  correlationId: "a3f9c1d2-...",
  endpoint: "POST /api/projects/10/tasks",
  status: 403
})
```

### Interceptor em `src/lib/api.ts`

- Intercepta respostas com erro
- Lê `response.headers['x-correlation-id']`
- Chama `logger.error` com `{ correlationId, endpoint, status, message }`
- Mantém o `throw` original — nenhum componente existente é afetado

---

## O que NÃO muda

- Nenhuma lógica de negócio alterada
- Nenhum componente React alterado
- Fluxo de erro existente (toasts, redirects) continua igual
- Sem dependências externas no frontend

---

## Arquivos a criar/modificar

### Backend
| Ação | Arquivo |
|---|---|
| Criar | `src/main/java/.../security/CorrelationIdFilter.java` |
| Criar | `src/main/java/.../config/ObservabilityConfig.java` |
| Criar | `src/main/resources/logback-spring.xml` |
| Modificar | `pom.xml` — 3 dependências |
| Modificar | `application.properties` — 4 propriedades |
| Modificar | `SecurityConfig.java` — 1 linha |
| Modificar | `TaskService.java` — `@Slf4j` + logs + counters |
| Modificar | `ProjectService.java` — `@Slf4j` + logs + counter |
| Modificar | `AuthController.java` — `@Slf4j` + logs + counter |
| Modificar | `TaskController.java` — `@Timed` |
| Modificar | `ProjectController.java` — `@Timed` |

### Frontend
| Ação | Arquivo |
|---|---|
| Criar | `src/lib/logger.ts` |
| Modificar | `src/lib/api.ts` — interceptor de erro |
