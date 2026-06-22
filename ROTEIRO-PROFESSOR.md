# TaskFlow — Roteiro de Apresentação

> Apresentação individual · ~10 minutos · Prof. Camargo

---

## 1. Visão Geral (~2 min)

**O que é o TaskFlow?**
Aplicação de gerenciamento de projetos e tarefas no estilo Kanban. O usuário cria projetos, convida membros, cria tarefas com prioridade e data de entrega, e as move entre colunas (A Fazer → Em Andamento → Concluído) via drag and drop.

**Stack escolhida:**

| Camada | Tecnologia | Justificativa |
|---|---|---|
| Frontend | Next.js 15 + TypeScript | Server Actions nativas, roteamento integrado, ecossistema React |
| Backend | Spring Boot 3.4 + Java 21 | Maturidade, ecossistema robusto, virtual threads (Java 21) |
| Banco | PostgreSQL via Supabase | Relacional (projetos/tarefas/membros têm FK), gerenciado sem custo |
| Infra | Docker + Docker Compose | Ambiente reproduzível, backend + DB sobem com um comando |
| CI/CD | GitHub Actions | Integrado ao repositório, sem custo para repos públicos |

**Arquitetura:** monolítica — uma API REST (Spring Boot) consumida por uma SPA (Next.js). Decisão consciente: o escopo do projeto não justificava a complexidade operacional de microsserviços.

---

## 2. Funcionalidades Implementadas (~2 min)

**Autenticação e usuários**
- Registro e login com e-mail + senha (bcrypt)
- JWT armazenado em **HttpOnly cookie** — não acessível via JavaScript, proteção contra XSS
- Logout invalida o cookie no cliente

**Projetos**
- CRUD completo (criar, listar, deletar)
- Controle de papéis: **Proprietário** (gerencia membros) e **Membro** (gerencia tarefas)
- Convite de membros por e-mail

**Tarefas**
- CRUD com título, descrição, prioridade (Alta/Média/Baixa), data de entrega e responsável (assignee)
- Comentários por tarefa
- Movimentação entre colunas via drag and drop

**UX**
- Tema claro/escuro
- Interface com Tailwind CSS + shadcn/ui

---

## 3. Decisões Técnicas (~4 min)

### 3.1 JWT em HttpOnly Cookie (não localStorage)

A maioria dos tutoriais guarda o JWT no `localStorage`. A decisão aqui foi usar **HttpOnly cookie** — o browser envia automaticamente em toda requisição, mas JavaScript não consegue ler o valor. Isso elimina toda uma classe de ataques XSS onde um script malicioso rouba o token.

Tradeoff: requer configuração de CORS mais cuidadosa e o servidor precisa setar o cookie explicitamente.

---

### 3.2 CI/CD com dois pipelines independentes

Dois workflows no GitHub Actions — `api-ci.yml` (backend) e `app-ci.yml` (frontend) — cada um com seu ciclo de build + test + publish de relatório de testes.

Por que separados? O frontend e o backend têm ritmos de mudança diferentes. Um pipeline único bloquearia o deploy do backend por um erro de lint no frontend. Separando, cada serviço tem sua própria porta de qualidade.

**Pipeline backend:** `./mvnw verify` → publica relatório JUnit XML → (SonarCloud pendente)  
**Pipeline frontend:** `npm ci` → lint → `npm run test:coverage` → build Next.js → (SonarCloud pendente)

---

### 3.3 Observabilidade

Esta foi a feature mais densa tecnicamente. O objetivo era rastrear uma requisição do frontend até o backend e ter métricas acionáveis.

**Correlation ID:**
1. Frontend gera ou repassa um UUID no header `X-Correlation-ID`
2. `CorrelationIdFilter` no Spring captura o header e injeta no MDC (Mapped Diagnostic Context)
3. Todos os logs do request carregam o `correlationId` automaticamente
4. O header é devolvido na resposta — frontend pode correlacionar logs dos dois lados

**Métricas com Micrometer (expostas em `/actuator/prometheus`):**
- `taskflow_auth_failures_total` — falhas de autenticação
- `taskflow_tasks_total` — tarefas criadas
- `taskflow_projects_total` — projetos criados
- Latência dos controllers via `@Timed` + `TimedAspect` (AOP)

**Logs estruturados:**
- Backend: logback-json → cada linha de log é um JSON com `timestamp`, `level`, `correlationId`, `message`
- Frontend: `logger.ts` no servidor Next.js → mesmo formato JSON, mesmo `correlationId`

Resultado: dado um erro em produção, o `correlationId` permite encontrar exatamente o request no frontend e no backend sem depender de timestamps.

---

### 3.4 SonarCloud (análise estática)

Configurado para rodar nos dois pipelines:
- **Backend:** JaCoCo 0.8.15 gera `jacoco.xml` → `./mvnw sonar:sonar` envia para SonarCloud
- **Frontend:** Jest com `--coverage` gera `lcov.info` → `SonarSource/sonarcloud-github-action` envia

Quality Gate definido: cobertura < 70% em código novo bloqueia o merge.

**Status atual:** código e pipelines prontos. A análise em si está pendente de acesso de admin ao repositório para importar no SonarCloud — o repositório pertence a outro colaborador.

---

## 4. Testes (~1 min)

Testes unitários com JUnit 5 + Mockito no backend:
- `ProjectServiceTest` — criação, listagem, deleção, controle de papéis
- `TaskServiceTest` — CRUD de tarefas, validação de acesso por projeto
- `CorrelationIdFilterTest` — geração e propagação do correlation ID

Frontend: Jest com Testing Library — componentes de formulário e utilitários.

53 testes no backend, 10 no frontend. Cobertura baixa em partes do código — abaixo do que o Quality Gate exigiria em produção.

---

## 5. Reflexões e Aprendizados (~1 min)

**O que faria diferente:**

- **TDD desde o início.** Os testes foram escritos depois do código em boa parte do projeto. Escrever o teste primeiro força a pensar na interface antes da implementação — o código sai mais limpo.
- **CI/CD no primeiro commit.** O pipeline foi adicionado depois de o projeto já estar em andamento. Configurar desde o início teria pego erros de integração mais cedo.
- **Refresh tokens.** O JWT atual não tem rotação — quando expira, o usuário é deslogado. Implementar refresh token é o próximo passo de segurança.

**O que ficou pendente do PDF:**
- Segurança básica: rate limiting (parcialmente implementado), variáveis de ambiente para credenciais (algumas ainda hardcoded no `application.properties` de dev)
- Deploy em produção (online) — o backend roda local/Docker, não tem URL pública
- Acessibilidade (ARIA attributes, navegação por teclado)

**Principal aprendizado:** observabilidade não é um extra — é o que permite entender o sistema quando ele falha em produção. Implementar correlation ID mostrou na prática como um único UUID conecta logs de sistemas completamente separados.
