# TaskFlow 📋

Uma aplicação de gerenciamento de projetos e tarefas no estilo Kanban, desenvolvida para demonstrar a integração entre **Next.js (React)** no frontend e **Spring Boot** no backend com **Java 21+**, banco de dados **PostgreSQL** hospedado no **Supabase** e todo o ambiente containerizado com **Docker**. O projeto inclui autenticação JWT, controle de membros por projeto e pipelines de CI/CD com **GitHub Actions**.

## 📌 Problema que resolve

No dia a dia, equipes precisam de uma ferramenta clara para organizar projetos e distribuir tarefas. O TaskFlow resolve exatamente isso: permite criar projetos, convidar membros, gerenciar tarefas com prioridade e data de entrega e acompanhar o progresso de cada item em um quadro Kanban visual com suporte a Drag and Drop.

### ✨ Funcionalidades

* 🔐 Cadastro e login com autenticação JWT (token em HttpOnly Cookie)
* 📁 Criar, editar e excluir projetos
* 👥 Convidar membros para projetos com controle de papéis (Proprietário / Membro)
* ✅ CRUD completo de tarefas (título, descrição, prioridade, data de entrega, responsável)
* 💬 Comentários em tarefas
* 🗂️ Quadro Kanban com colunas A Fazer, Em Andamento e Concluído
* 🖱️ Drag and Drop para mover e reordenar tarefas
* 🌗 Alternância de tema claro e escuro
* 🎨 Interface moderna com Tailwind CSS e shadcn/ui

### ⚗️ C4 Model

  * [Imagem C4 Model](https://imgur.com/a/v5jLalz)

## 📋 Requisitos do Sistema

## 🟢 Requisitos Funcionais (RF)
*Representam o que o sistema deve fazer; as ações diretas do usuário.*

### 1. Gestão de Usuários e Autenticação
* **RF01 - Cadastro de Usuário:** O sistema deve permitir que novos usuários se cadastrem informando nome, e-mail e senha.
* **RF02 - Login e Logout:** O sistema deve permitir login seguro com e-mail e senha, além de encerramento da sessão ativa.
* **RF03 - Perfil:** O usuário autenticado deve poder visualizar os dados básicos do seu perfil (nome, e-mail e avatar).

### 2. Gestão de Projetos (Workspaces)
* **RF04 - Criar Projeto:** Usuários logados devem ser capazes de criar novos projetos/boards do zero.
* **RF05 - Editar/Excluir Projeto:** O administrador do projeto deve poder editar seus detalhes (título, descrição) ou excluí-lo por completo.
* **RF06 - Convites e Equipe:** O criador do projeto deve ser capaz de convidar outros usuários cadastrados no sistema para acessarem o Board via busca por e-mail.
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
* **RNF08 - Foco Web/Desktop:** O Kanban e suas interações (Drag & Drop) são projetados e testados visando uso primário via navegadores Desktop com mouse. Não há exigência de componentes responsivos ou adaptados para telas móveis.
* **RNF09 - Optimistic UI (Tempo de Resposta Perceptível):** Ao mover um card via UI no Kanban, a requisição de atualização para o backend ocorre em segundo plano; o Frontend aplica a mudança visual imediatamente para transmitir sensação instantânea de performance.
* **RNF10 - Tema Claro e Escuro:** A interface deve suportar alternância entre tema claro e escuro via componente ThemeToggle.


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
│       ├── api-ci.yml     # Pipeline CI/CD do backend
│       └── app-ci.yml     # Pipeline CI/CD do frontend
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
* Supabase para hospedagem do banco de dados PostgreSQL em nuvem
* GitHub Actions para CI/CD (integração contínua e deploy automatizado)
* Repositório único, mas com frontend e backend em diretórios separados

## 📋 Pré-requisitos

Para rodar o projeto localmente, você precisará ter instalado:
* **Docker e Docker Compose** (recomendado)

Ou, se preferir executar manualmente:
* **Java 21+** e **Maven** (para o backend)
* **Node.js 18+** e **npm/yarn** (para o frontend)

## 🚀 Como executar

### Com Docker Compose (recomendado)

Na raiz do repositório, execute:

```bash
docker-compose up --build
```

A aplicação estará disponível em:
* **Frontend:** http://localhost:3000
* **Backend API:** http://localhost:8080/api

> O banco de dados é hospedado no **Supabase**. A conexão já está configurada no `application.properties` do backend.

---

### Manualmente (Sem Docker Compose)

**1. Rodando o Backend**

```bash
cd taskflow-api
./mvnw spring-boot:run
```

> O backend conecta automaticamente ao banco Supabase configurado em `src/main/resources/application.properties`.

**2. Rodando o Frontend**

```bash
cd taskflow-app
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