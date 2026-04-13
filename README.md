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

### ⚗️ C4 Model

  * [Imagem C4 Model](https://imgur.com/a/v5jLalz)

## 📋 Requisitos do Sistema

## 🟢 Requisitos Funcionais (RF)
*Representam o que o sistema deve fazer; as ações diretas do usuário.*

### 1. Gestão de Usuários e Autenticação
* **RF01 - Cadastro de Usuário:** O sistema deve permitir que novos usuários se cadastrem informando nome, e-mail e senha.
* **RF02 - Login e Logout:** O sistema deve permitir login seguro com e-mail e senha, além de encerramento da sessão ativa.
* **RF03 - Proteção de Perfil:** O usuário autenticado deve poder visualizar ou editar os dados básicos do seu perfil.

### 2. Gestão de Projetos (Workspaces)
* **RF04 - Criar Projeto:** Usuários logados devem ser capazes de criar novos projetos/boards do zero.
* **RF05 - Editar/Excluir Projeto:** O administrador do projeto deve poder editar seus detalhes (título, descrição) ou excluí-lo por completo.
* **RF06 - Convites e Equipe:** O criador do projeto deve ser capaz de convidar outros usuários cadastrados no sistema para acessarem o Board (via busca de e-mail ou gerando um link de acesso).
* **RF07 - Controle de Papéis:** O projeto deve distinguir propriedades, como `Proprietário` (Pode excluir/adicionar pessoas) e `Membro` (Pode apenas gerenciar tarefas).

### 3. Gestão de Tarefas
* **RF08 - Gerenciamento Básico (CRUD):** Os membros de um projeto devem poder criar, editar detalhes, visualizar e deletar tarefas no projeto.
* **RF09 - Campos da Tarefa:** Uma tarefa deve conter Título, Descrição, Prioridade (Alta, Média, Baixa), Data de Entrega e Responsável (Assignee).
* **RF10 - Atribuição:** Deve ser possível atribuir (dar o *assign*) de uma tarefa a um membro específico do projeto.
* **RF11 - Comentários na Tarefa:** Membros devem poder adicionar pequenos comentários/anotações na visualização de detalhes da tarefa para manter histórico.

### 4. Kanban e Interações
* **RF12 - Quadro Visual (Board):** As tarefas de um projeto devem ser exibidas em colunas que representam o seu *Status* (Padrão sugerido: `A Fazer`, `Em Andamento`, `Concluído`).
* **RF13 - Funcionalidade Drag and Drop:** O usuário deve mover as tarefas lateralmente entre colunas ou alterar a ordem delas na mesma coluna usando o ponteiro do mouse (Arrastar e Soltar).

---

## 🔴 Requisitos Não Funcionais (RNF)
*Representam os atributos de qualidade, segurança e arquitetura técnica.*

### 1. Arquitetura e Tecnologias
* **RNF01 - Frontend Stack:** O cliente web deve ser construído em **Next.js** usando as tecnologias já validadas (**Tailwind CSS** e **shadcn/ui**).
* **RNF02 - Backend Stack:** A API que consumirá as regras de negócio será feita em **Java + Spring Boot**.
* **RNF03 - Banco de Dados:** Modelagem e Persistência deverão ocorrer em um modelo relacional (ex: **PostgreSQL**) para garantir estrutura sólida entre relacionamento de Usuários, Projetos e Tarefas.

### 2. Segurança
* **RNF04 - Autenticação Segura (JWT):** A transição de dados de login deverá gerar um *JSON Web Token* para validar as comunicações futuras na API. Recomenda-se guardar o token de forma segura (como *HttpOnly Cookies* ou adequadamente no estado da aplicação) para evitar *XSS*.
* **RNF05 - Hashing de Senhas:** Nenhuma senha deverá ser salva em texto puro. Deve-se aplicar criptografia (ex: **Bcrypt**) nos cadastros.
* **RNF06 - Autorização de Dados:** Um usuário não pode acessar, via API ou UI, as informações de tarefas de um projeto para o qual não foi convidado. 

### 3. Usabilidade e Desempenho (UX/UI)
* **RNF07 - Biblioteca de Drag & Drop:** Para manter a leveza no frontend React, sugere-se a adoção de bibliotecas modernas para manuseio do Kanban, como `@hello-pangea/dnd` ou `dnd-kit`.
* **RNF08 - Foco Web/Desktop:** Por ser um projeto de viés acadêmico, o Kanban e suas interações (Drag & Drop) serão projetados e testados visando uso primário via navegadores Desktop com mouse. Não haverá exigência de criação de componentes responsivos ou adaptados para telas móveis.
* **RNF09 - Optimistic UI (Tempo de Resposta Perceptível):** Ao mover um card via UI no Kanban, a requisição de atualização para o backend deverá correr de forma secundária; o Frontend aplicará a mudança visual imediatamente para provar sensação instantânea de performance para o usuário. 


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