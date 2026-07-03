# IntegraçãoLAB

Middleware de integração laboratorial (LIS) desenvolvido em **Spring Boot**. A aplicação atua como uma ponte entre **equipamentos de análises clínicas** e os **bancos de dados** de cada laboratório/cliente, recebendo resultados de exames via protocolo **HL7/MLLP** por TCP e disponibilizando uma **API REST** para criação de ordens de serviço.

O sistema é **multi-tenant**: cada cliente (`Entidade`) possui sua própria porta TCP, chave de API e credenciais de banco de dados externo.

---

## Sumário

- [Arquitetura](#arquitetura)
- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Modelo de dados](#modelo-de-dados)
- [Configuração](#configuração)
- [Como executar](#como-executar)
- [API REST](#api-rest)
- [Protocolo LIS (TCP/HL7)](#protocolo-lis-tcphl7)
- [Logs](#logs)

---

## Arquitetura

A aplicação possui dois fluxos principais de entrada:

### 1. Recepção de resultados (Equipamento → Aplicação → Banco)

```
Equipamento (Dirui, etc.)
        │  (TCP / HL7 / MLLP em UTF-16LE)
        ▼
TcpLisServer ──► LisConnectionHandler ──► LisMessageParser
                                                │
                                                ▼
                                          LisService
                                         /          \
                             Banco LOCAL (JPA)    Banco EXTERNO do cliente (JDBC)
                          tabela `resultados`      (update via DriverManager)
```

- `TcpLisServer` lê as `Entidade`s ativas do banco e abre **um listener TCP por porta** (uma thread por cliente).
- Cada conexão de equipamento é tratada por um `LisConnectionHandler` (thread dedicada), que interpreta o protocolo MLLP, envia `ACK` e delega o parse.
- `LisMessageParser` extrai os segmentos `OBR` (amostra) e `OBX` (resultados) das mensagens HL7.
- `LisService` persiste os resultados no banco local e, em seguida, atualiza o banco do cliente.

### 2. Criação de ordens (Sistema externo → API REST)

```
Cliente HTTP ──(POST /orders + X-API-KEY)──► ApiKeyFilter ──► LisOrderController ──► LisService ──► tabela `ordens`
```

- `ApiKeyFilter` valida o header `X-API-KEY` contra a tabela `entidades` e coloca a entidade autenticada no `ClienteContext`.
- `LisOrderController` recebe a ordem, mapeia o payload e delega ao `LisService`.

---

## Funcionalidades

- Servidor TCP **multi-cliente** com uma porta dedicada por laboratório.
- Parsing de mensagens **HL7/MLLP** com suporte a codificação **UTF-16LE** (equipamentos Dirui).
- Persistência de resultados no banco local (SQL Server via JPA/Hibernate).
- Replicação/atualização dos resultados no **banco de dados externo** de cada cliente.
- API REST para cadastro de ordens de serviço, protegida por **API Key**.
- Isolamento por cliente através de `ClienteContext` (thread-local).
- Logging em arquivo com rotação diária.

---

## Tecnologias

- **Java 21**
- **Spring Boot 4.0.1**
  - Spring Web
  - Spring Data JPA
  - Spring Security
- **Microsoft SQL Server** (driver `mssql-jdbc`)
- **Maven** (com wrapper `mvnw`)
- **Logback** para logging

---

## Estrutura do projeto

```
app/
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/integracaolab/app/
    │   │   ├── AppApplication.java            # Bootstrap + inicia o TcpLisServer
    │   │   ├── TcpLisServer.java              # Abre listeners TCP por Entidade
    │   │   ├── LisConnectionHandler.java      # Trata cada conexão de equipamento
    │   │   ├── LisConnectionManager.java
    │   │   ├── LisMessageParser.java          # Parse HL7 (OBR/OBX)
    │   │   ├── LisMessageBuffer.java          # Buffer de segmentos da mensagem
    │   │   ├── LisAsciiFormatter.java
    │   │   ├── LisOrder.java                  # Modelo de ordem
    │   │   ├── LisOrderSender.java
    │   │   ├── LisService.java                # Regras de negócio / persistência
    │   │   ├── ClienteContext.java            # Contexto (thread-local) da entidade
    │   │   ├── api/
    │   │   │   ├── controller/LisOrderController.java
    │   │   │   ├── dto/LisOrderRequest.java
    │   │   │   ├── dto/ResultadoDTO.java
    │   │   │   └── security/
    │   │   │       ├── ApiKeyFilter.java      # Autenticação via X-API-KEY
    │   │   │       └── SecurityConfig.java
    │   │   └── persistence/
    │   │       ├── entity/  (Entidade, OrderEntity, ResultadoEntity + repositories)
    │   │       └── repository/ResultadoRepository.java
    │   └── resources/
    │       ├── application.yml                # Config via variáveis de ambiente
    │       ├── application.properties
    │       └── logback-spring.xml             # Config de logging
    └── test/
        └── java/com/integracaolab/app/AppApplicationTests.java
```

---

## Modelo de dados

O Hibernate está configurado com `ddl-auto: validate`, ou seja, as tabelas **devem existir previamente** no banco.

### Tabela `entidades`
Cadastro dos clientes/laboratórios (multi-tenant).

| Coluna        | Tipo      | Descrição                                     |
|---------------|-----------|-----------------------------------------------|
| `ent_cod`     | PK/identity | Identificador da entidade                    |
| `nome`        | string    | Nome do cliente                               |
| `api_key`     | string (único) | Chave usada na autenticação da API REST  |
| `db_url`      | string    | URL JDBC do banco externo do cliente          |
| `db_username` | string    | Usuário do banco externo                       |
| `db_password` | string    | Senha do banco externo                         |
| `ativo`       | boolean   | Indica se a entidade está ativa                |
| `porta`       | int       | Porta TCP em que o listener será aberto        |
| `created_at`  | datetime  | Data de criação                                |

### Tabela `ordens`
Ordens de serviço recebidas pela API REST.

| Coluna               | Descrição                             |
|----------------------|---------------------------------------|
| `id`                 | PK/identity                           |
| `ent_cod`            | FK → `entidades`                      |
| `sample_id`          | Código de barras / ID da amostra      |
| `patient_name`       | Nome do paciente                      |
| `patient_birth_date` | Data de nascimento                    |
| `patient_gender`     | Sexo                                  |
| `patient_cpf`        | CPF                                   |
| `sample_type`        | Tipo de amostra                       |
| `exams_raw`          | Exames solicitados (lista separada por vírgula) |
| `created_at`         | Data de criação                       |

### Tabela `resultados`
Resultados de exames recebidos dos equipamentos.

| Coluna           | Descrição                        |
|------------------|----------------------------------|
| `res_id`         | PK/identity                      |
| `ent_cod`        | FK → `entidades`                 |
| `sample_id`      | ID da amostra                    |
| `exame`          | Código/nome do exame             |
| `valor`          | Valor do resultado               |
| `unidade`        | Unidade de medida                |
| `data_resultado` | Data do resultado                |
| `created_at`     | Data de criação                  |

---

## Configuração

A configuração do banco de dados principal é feita por **variáveis de ambiente**, lidas em `application.yml`:

| Variável         | Descrição                                       |
|------------------|-------------------------------------------------|
| `MASTER_DB_URL`  | URL JDBC do banco principal (ex.: SQL Server)   |
| `MASTER_DB_USER` | Usuário do banco principal                      |
| `MASTER_DB_PASS` | Senha do banco principal                        |
| `SERVER_PORT`    | Porta HTTP da aplicação                          |

> ⚠️ **Atenção:** o arquivo `application.properties` contém `api.key=abc123` e `server.port=4090` como valores de exemplo. **Não** utilize essas credenciais em produção e evite versionar segredos reais. As portas dos listeners TCP são definidas por registro na tabela `entidades` (coluna `porta`).

Exemplo (PowerShell) para definir as variáveis antes de rodar:

```powershell
$env:MASTER_DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=integracaolab;encrypt=false"
$env:MASTER_DB_USER = "sa"
$env:MASTER_DB_PASS = "SuaSenhaForte"
$env:SERVER_PORT = "8080"
```

---

## Como executar

Pré-requisitos: **JDK 21** e acesso a uma instância do **SQL Server** com as tabelas já criadas.

### Via Maven Wrapper

```bash
# Compilar
./mvnw clean package

# Executar os testes
./mvnw test

# Rodar a aplicação
./mvnw spring-boot:run
```

No Windows (PowerShell), use `.\mvnw.cmd` no lugar de `./mvnw`.

### Via JAR gerado

```bash
java -jar target/app-0.0.1-SNAPSHOT.jar
```

Ao iniciar, a aplicação:
1. Sobe o contexto Spring e a API REST na porta configurada.
2. Lê as `Entidade`s ativas e abre um listener TCP para cada porta cadastrada.

---

## API REST

### `POST /orders`

Cria uma nova ordem de serviço.

**Headers**

| Header      | Obrigatório | Descrição                          |
|-------------|-------------|------------------------------------|
| `X-API-KEY` | Sim         | Chave da entidade (tabela `entidades`) |

**Body (JSON)**

```json
{
  "code_bar": "20240001",
  "patient_name": "João da Silva",
  "patient_birth_date": "1990-05-20",
  "patient_gender": "M",
  "patient_cpf": "000.000.000-00",
  "sample-type": "Soro",
  "tests": ["GLI", "COL", "TGO"]
}
```

**Resposta — 201 Created**

```json
{
  "status": "success",
  "message": "Ordem processada com sucesso",
  "codeBar": "20240001"
}
```

**Respostas de erro**

- `401 Unauthorized` — header `X-API-KEY` ausente, inválido ou entidade inativa.
- `500 Internal Server Error` — falha ao processar a ordem (ex.: `code_bar` ausente).

Exemplo com `curl`:

```bash
curl -X POST http://localhost:8080/orders \
  -H "X-API-KEY: abc123" \
  -H "Content-Type: application/json" \
  -d '{"code_bar":"20240001","patient_name":"João da Silva","tests":["GLI"]}'
```

---

## Protocolo LIS (TCP/HL7)

- Cada equipamento se conecta à porta TCP da sua `Entidade`.
- As mensagens seguem o padrão **MLLP** com segmentos HL7 separados por `<CR>` (0x0D) e bloco encerrado por `<EB>` (0x1C).
- A leitura é feita em **UTF-16LE** (Unicode), pois equipamentos como o **Dirui** transmitem nesse formato.
- Ao receber o fim do bloco, a aplicação responde imediatamente com **ACK** (0x06).
- O parser extrai:
  - `OBR` → `sample_id` (ID da amostra).
  - `OBX` → exame, valor e unidade de cada resultado.
- Os resultados são gravados na tabela `resultados` e replicados no banco externo do cliente.

---

## Logs

- Configurados em `src/main/resources/logback-spring.xml`.
- Saída em **console** e em **arquivo** (`logs/integracao-lis.log`).
- Rotação **diária** com histórico de **30 dias**.
