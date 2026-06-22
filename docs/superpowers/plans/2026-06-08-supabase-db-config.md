# Supabase Database Configuration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Configure `taskflow-api` to use Supabase PostgreSQL via environment variables with local fallbacks.

**Architecture:** Update `application.properties` to use Spring's `${VARIABLE_NAME:DEFAULT_VALUE}` syntax for database connectivity.

**Tech Stack:** Spring Boot, PostgreSQL.

---

### Task 1: Update application.properties

**Files:**
- Modify: `taskflow-api/src/main/resources/application.properties`

- [ ] **Step 1: Replace hardcoded database values with environment variables**

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/todo_db}
spring.datasource.username=${DB_USERNAME:admin}
spring.datasource.password=${DB_PASSWORD:admin123}
```

- [ ] **Step 2: Verify the change by reading the file**

Run: `cat taskflow-api/src/main/resources/application.properties`
Expected: File contains the variables instead of hardcoded values.

- [ ] **Step 3: Commit**

```bash
git add taskflow-api/src/main/resources/application.properties
git commit -m "config: use environment variables for database connectivity"
```

---

### Task 2: Verification (Manual/Log-based)

- [ ] **Step 1: Verify local fallback still works**

Run: `cd taskflow-api && ./mvnw spring-boot:run` (Assuming local postgres is up or ignoring failures if it's not the goal to fix local)
Expected: Application starts and connects to `localhost:5432/todo_db`.

- [ ] **Step 2: Inform the user on how to run with Supabase**

Provide the command example to the user.
