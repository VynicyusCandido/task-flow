# TaskFlow 📋

Uma aplicação de lista de tarefas (to-do list) simples, moderna e eficiente, desenvolvida para demonstrar a integração entre **Next.js (React)** no frontend e **Spring Boot** no backend com **Java 21+**, banco de dados **PostgreSQL** e todo o ambiente containerizado com **Docker**. O projeto também inclui pipelines de CI/CD com **GitHub Actions**.

## 📌 Problema que resolve

No dia a dia, precisamos de uma ferramenta rápida e intuitiva para organizar nossas tarefas. O TaskFlow resolve exatamente isso: permite criar, editar, marcar como concluída e excluir tarefas de forma simples, com uma interface limpa e responsiva. É a solução ideal para quem quer manter o foco e a produtividade.

### ✨ Funcionalidades

* ✅ Criar novas tarefas
* 📝 Editar tarefas existentes (com duplo clique no texto)
* ✔️ Marcar/desmarcar tarefas como concluídas
* 🗑️ Excluir tarefas
* 📱 Design responsivo (funciona em desktop e mobile)
* 🎨 Interface moderna com Tailwind CSS e componentes shadcn/ui

### C4 Model

  * [Imagem C4 Model](https://ibb.co/r2tkrr8H)

## 🛠️ Tecnologias utilizadas

**Frontend**
* Next.js 14+ (React framework)
* TypeScript
* Tailwind CSS para estilização
* shadcn/ui para componentes acessíveis e customizáveis
* Jest e Testing Library para testes

**Backend**
* Spring Boot 3.2+ (Java 21)
* Spring Data JPA para persistência
* PostgreSQL como banco de dados
* Maven para gerenciamento de dependências
* JUnit 5 e Mockito para testes

**Infraestrutura e DevOps**
* Docker e Docker Compose para containerização
* GitHub Actions para CI/CD (integração contínua e deploy automatizado)
* Repositório único, mas com frontend e backend em diretórios separados

## 📋 Pré-requisitos

Para rodar o projeto localmente, você precisará ter instalado:
* **Docker e Docker Compose** (recomendado)

Ou, se preferir executar manualmente:
* **Java 21+** e **Maven** (para o backend)
* **Node.js 18+** e **npm/yarn** (para o frontend)
* **PostgreSQL** (pode ser via Docker)

## 🚀 Como executar

### Com Docker Compose (recomendado)

Na raiz do repositório, execute:

```bash
docker-compose up --build
```

A aplicação estará disponível em:
* **Frontend:** http://localhost:3000
* **Backend API:** http://localhost:8080/api
* **Banco de dados PostgreSQL:** `localhost:5432` (usuário `admin`, senha `admin123`, database `todo_db`)

---

### Manualmente (Sem Docker Compose)

**1. Suba o banco de dados (opcional, via Docker)**

```bash
docker run --name taskflow-postgres -e POSTGRES_DB=todo_db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin123 -p 5432:5432 -d postgres:15
```

**2. Rodando o Backend**

```bash
cd backend
./mvnw spring-boot:run
```

**3. Rodando o Frontend**

```bash
cd frontend
npm install
npm run dev
```

## 📁 Estrutura do projeto

```text
taskflow/
├── file
│   ├── c4Model/
│   ├── requisitos/
├── taskflow-api/               # Projeto Spring Boot (Java 21)
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── ...
├── taskflow-app/              # Projeto Next.js
│   ├── app/
│   ├── components/
│   ├── lib/
│   ├── package.json
│   ├── Dockerfile
│   └── ...
├── docker-compose.yml     # Orquestração dos serviços
├── .github/
│   └── workflows/
│       └── ci.yml         # Pipeline CI/CD
└── README.md
```

<br>

<div align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg" height="40" alt="react logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nextjs/nextjs-original.svg" height="40" alt="nextjs logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tailwindcss/tailwindcss-original-wordmark.svg" height="40" alt="tailwindcss logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" height="40" alt="spring logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg" height="40" alt="postgresql logo" />
  <img width="12" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg" height="40" alt="docker logo" />
</div>