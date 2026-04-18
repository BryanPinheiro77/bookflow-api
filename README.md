# BookFlow API

API backend do **BookFlow**, um sistema mobile de gerenciamento de biblioteca, desenvolvido com **Java** e **Spring Boot**.

O projeto foi construído para permitir o gerenciamento de acervos por múltiplos administradores, com autenticação JWT, controle de empréstimos, notificações internas, interesse em livros emprestados e upload de capas.

---

## Objetivo do projeto

O BookFlow foi criado para funcionar como backend de um aplicativo mobile de biblioteca.

A proposta do sistema é permitir que:

- **administradores** gerenciem seus próprios acervos
- **usuários** consultem livros, realizem empréstimos e acompanhem notificações
- o sistema suporte múltiplos acervos em uma única aplicação, sem virar marketplace entre usuários

---

## Perfis de acesso

### ADMIN
Representa o responsável por um acervo, biblioteca ou instituição.

Pode:

- cadastrar livros
- editar livros
- excluir livros
- enviar, trocar e remover capa de livro
- visualizar apenas os próprios livros
- visualizar empréstimos dos próprios livros
- registrar devolução
- receber notificações quando um livro do seu acervo for emprestado

### USUARIO
Representa o leitor.

Pode:

- se cadastrar
- fazer login
- listar livros do catálogo
- visualizar detalhes de livros
- pegar livros emprestados
- visualizar os próprios empréstimos
- marcar interesse em livros emprestados
- receber notificação quando um livro voltar a ficar disponível
- visualizar e gerenciar suas notificações

---

## Funcionalidades implementadas

### Autenticação e segurança
- cadastro de usuário
- login com JWT
- autenticação stateless
- senhas criptografadas com BCrypt
- controle de acesso por roles
- proteção de endpoints com Spring Security e `@PreAuthorize`

### Livros
- cadastro de livro
- edição de livro
- exclusão de livro
- listagem de livros
- busca por id
- isolamento por admin nas consultas administrativas

### Empréstimos
- criação de empréstimo
- devolução de empréstimo
- listagem de empréstimos por usuário
- listagem de empréstimos por admin
- atualização automática do status do livro

### Interesse em livro emprestado
- registro de interesse
- prevenção de interesse duplicado
- listagem de interesses do usuário
- remoção de interesse

### Notificações internas
- criação automática de notificações
- listagem de notificações
- marcar como lida
- marcar todas como lidas
- contador de notificações não lidas
- notificação para usuários interessados quando o livro volta
- notificação para admin quando um empréstimo é criado

### Upload de imagem
- upload local de capa de livro
- troca de capa com remoção da anterior
- remoção manual da capa
- atualização do campo `capaUrl`
- exposição pública da pasta de uploads

### Documentação
- Swagger / OpenAPI configurado
- suporte a autenticação JWT via Swagger

---

## Regras de negócio principais

- cada livro pertence a um **ADMIN**
- um admin **não pode alterar nem visualizar administrativamente** livros de outro admin
- um usuário só pode pegar emprestado livro com status `DISPONIVEL`
- não pode existir mais de um empréstimo ativo para o mesmo livro
- quando o livro é emprestado, seu status muda para `EMPRESTADO`
- quando o livro é devolvido, seu status volta para `DISPONIVEL`
- só o admin dono do livro pode registrar a devolução
- interesse só pode ser registrado em livro emprestado
- após a devolução, os usuários interessados recebem notificação e os interesses são removidos
- ao enviar nova capa, a anterior é removida do disco
- ao excluir o livro, a capa também é removida

---

## Tecnologias utilizadas

- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- JWT
- PostgreSQL
- Docker Compose
- Lombok
- Maven
- Swagger / OpenAPI
- JUnit 5
- Mockito

---

## Arquitetura do projeto

O projeto segue uma organização **por feature/módulo**, com subpastas internas por responsabilidade.

Exemplo:

```text
src/main/java/br/com/bookflow
 ┣ auth
 ┣ usuario
 ┣ livro
 ┣ emprestimo
 ┣ interesse
 ┣ notificacao
 ┣ upload
 ┣ config
 ┗ exception
```

Dentro de cada módulo, são usados pacotes como:

- `controller`
- `dto`
- `entity`
- `repository`
- `service`

---

## Como executar o projeto

### Pré-requisitos
- Java 21+
- Maven
- Docker e Docker Compose
- PostgreSQL

### 1. Clonar o repositório
```bash
git clone <url-do-repositorio>
cd bookflow
```

### 2. Subir o banco com Docker
```bash
docker compose up -d
```

### 3. Rodar a aplicação
```bash
./mvnw spring-boot:run
```

Ou, no Windows:
```bash
mvnw spring-boot:run
```

---

## Configurações

Exemplo de propriedades principais:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookflow
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=8080

app.upload.dir=uploads

spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

springdoc.swagger-ui.path=/swagger-ui.html
```

> Ajuste usuário, senha, banco e secret JWT conforme seu ambiente.

---

## Autenticação

A API usa **JWT Bearer Token**.

### Fluxo
1. realizar login em `/auth/login`
2. copiar o token retornado
3. enviar no header:

```http
Authorization: Bearer SEU_TOKEN
```

---

## Swagger

A documentação interativa da API fica disponível em:

```text
http://localhost:8080/swagger-ui.html
```

JSON OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

No Swagger, é possível autenticar clicando em **Authorize** e informando o token JWT.

---

## Principais endpoints

### Auth
- `POST /auth/cadastrar`
- `POST /auth/login`

### Livros
- `GET /livros`
- `GET /livros/{id}`
- `POST /livros`
- `PUT /livros/{id}`
- `DELETE /livros/{id}`
- `POST /livros/{id}/capa`
- `DELETE /livros/{id}/capa`

### Empréstimos
- `POST /emprestimos`
- `PUT /emprestimos/{id}/devolver`
- `GET /emprestimos`
- `GET /emprestimos/me`

### Interesses
- `POST /interesses/livros/{livroId}`
- `GET /interesses/meus`
- `DELETE /interesses/livros/{livroId}`

### Notificações
- `GET /notificacoes`
- `GET /notificacoes/nao-lidas/quantidade`
- `PATCH /notificacoes/{id}/ler`
- `PATCH /notificacoes/ler-todas`

---

## Testes

O projeto possui testes unitários para os principais módulos de negócio, incluindo:

- autenticação
- usuários
- livros
- empréstimos
- interesses
- notificações
- upload de capa

Para executar os testes:

```bash
./mvnw test
```

Ou no Windows:

```bash
mvnw test
```

---

## Status atual

O backend já possui a base principal do sistema implementada e validada, incluindo:

- autenticação JWT
- controle de acesso por perfil
- gestão de livros
- empréstimos e devolução
- notificações internas
- interesse em livro emprestado
- upload de imagens
- Swagger
- testes unitários

---

## Melhorias futuras

Possíveis evoluções futuras do projeto:

- reserva de livro
- push notification real
- paginação e filtros avançados
- integração com frontend mobile
- armazenamento de imagens em cloud
- monitoramento e logs mais detalhados

---

## Autor

Projeto desenvolvido por **Bryan Mendes Pinheiro da Silva**, como parte de estudo e desenvolvimento acadêmico/profissional.
