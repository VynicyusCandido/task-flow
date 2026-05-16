-- liquibase formatted sql

-- changeset antigravity:insert-default-users-v2
-- preconditions onFail:CONTINUE
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='users';
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM users;

INSERT INTO users (name, email, password) 
VALUES ('Admin TaskFlow', 'admin@taskflow.com', '$2a$10$FOOfZVqeRDqV/ZVmW9Iu9uOdCQS1ckPRzNr1a9pKMoiLKXD6A3GL.'); -- senha: admin123

INSERT INTO users (name, email, password) 
VALUES ('Usuário Padrão', 'user@taskflow.com', '$2a$10$Fxa7DVQ5bZTtkQHoxcF0DO1NJJCAwpgBblsc5yvSRCTtqRf5Serxe'); -- senha: user123
