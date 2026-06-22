# Design: Supabase Database Integration for taskflow-api

## Purpose
Transition the `taskflow-api` from a local PostgreSQL database (Docker) to a managed PostgreSQL instance on Supabase using environment variables.

## Architecture
- **Application:** Spring Boot (taskflow-api)
- **Database:** PostgreSQL (Supabase)
- **Connectivity:** JDBC via Environment Variables

## Proposed Changes

### 1. `application.properties` Refactoring
Modify `taskflow-api/src/main/resources/application.properties` to use placeholders for database credentials.

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/todo_db}
spring.datasource.username=${DB_USERNAME:admin}
spring.datasource.password=${DB_PASSWORD:admin123}
```

### 2. Connectivity Strategy
The user will need to retrieve the **JDBC Connection String** from the Supabase Dashboard:
- Path: Project Settings -> Database -> Connection string -> JDBC.

## Success Criteria
1. Application starts successfully without hardcoded credentials.
2. Liquibase migrations run against the Supabase instance.
3. Data is persisted in Supabase.

## Testing Strategy
1. Start the application locally after setting the `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables.
2. Verify connection success in the logs.
3. Check Supabase table structure to ensure Liquibase executed correctly.
