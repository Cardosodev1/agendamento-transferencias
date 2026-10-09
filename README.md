# Agendamento de Transferências Financeiras

Sistema para agendar transferências entre contas, calcular a taxa conforme a antecedência e consultar o extrato dos agendamentos.

- **backend/** — API REST em Java 11 + Spring Boot, persistência em H2 (memória).
- **frontend/** — SPA em Angular + Bootstrap.

## Como executar

Há duas formas: com **Docker Compose** (só precisa do Docker) ou **localmente** (JDK + Node).

### Opção 1 — Docker Compose (recomendado)

Pré-requisito: Docker com Compose v2 (Docker Desktop ou Docker Engine).

```bash
docker compose up --build -d
```

| Serviço | URL |
|---|---|
| Aplicação | http://localhost:4200 |
| API | http://localhost:8080/api/transferencias |
| Console do H2 | http://localhost:8080/h2-console |
| Health check | http://localhost:8080/actuator/health |

O frontend só sobe depois que a API responde ao health check. Para acompanhar e encerrar:

```bash
docker compose ps        # estado e saúde dos containers
docker compose logs -f   # logs
docker compose down      # para e remove os containers
```

As portas podem ser trocadas por variáveis de ambiente: `FRONTEND_PORT=3000 API_PORT=9090 docker compose up -d`.

### Opção 2 — Localmente

#### Pré-requisitos

| Ferramenta | Versão |
|---|---|
| JDK | 11 |
| Node.js | 22.22+ / 24.15+ (usado: 24.21) — exigência do Angular 22 |
| npm | 10+ (usado: 11.19) |

O Maven não precisa estar instalado: o projeto usa o Maven Wrapper (`mvnw`).

#### 1. API (porta 8080)

```bash
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Console do H2: http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:transferencias`, usuário `sa`, senha em branco.

#### 2. Frontend (porta 4200)

```bash
cd frontend
npm install
npm start
```

Acesse http://localhost:4200. O servidor de desenvolvimento encaminha `/api` para `http://localhost:8080` (`proxy.conf.json`).

### Testes

```bash
cd backend  && ./mvnw test                # 39 testes: domínio, service, repositório (H2) e controller (MockMvc)
cd frontend && npm test -- --watch=false  # 23 testes: service HTTP, validadores, formulário e extrato (Vitest)
cd frontend && npm run lint               # ESLint (angular-eslint), incluindo regras de acessibilidade
```

## Regras de negócio

A taxa é calculada a partir da quantidade de **dias corridos** entre a data de agendamento (hoje) e a data da transferência:

| Dias | Taxa fixa | Percentual sobre o valor |
|---|---|---|
| 0 | R$ 3,00 | 2,5% |
| 1 a 10 | R$ 12,00 | 0,0% |
| 11 a 20 | R$ 0,00 | 8,2% |
| 21 a 30 | R$ 0,00 | 6,9% |
| 31 a 40 | R$ 0,00 | 4,7% |
| 41 a 50 | R$ 0,00 | 1,7% |

`taxa = taxa fixa + valor × percentual`, arredondada para 2 casas (`HALF_UP`).

Interpretações adotadas:

- **Sem taxa aplicável** (data no passado ou mais de 50 dias à frente): a API responde `422` com a mensagem de erro e nada é gravado. O front exibe o alerta assim que a data é escolhida e desabilita o botão de agendar.
- **Contas**: exatamente 10 dígitos numéricos (`XXXXXXXXXX`), e a conta de destino precisa ser diferente da de origem.
- **Valor**: maior que zero, com no máximo 2 casas decimais.
- **"Hoje"** é sempre calculado no fuso `America/Sao_Paulo`, independente do fuso do servidor.

## API

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/transferencias` | Agenda uma transferência. `201` + `Location` |
| `GET` | `/api/transferencias` | Extrato: todos os agendamentos, do mais recente para o mais antigo |
| `GET` | `/api/transferencias/taxa?valor=&dataTransferencia=` | Simula a taxa sem gravar (usado na prévia do formulário) |

Exemplo:

```bash
curl -X POST http://localhost:8080/api/transferencias \
  -H 'Content-Type: application/json' \
  -d '{"contaOrigem":"1234567890","contaDestino":"0987654321","valor":1000.00,"dataTransferencia":"2026-10-20"}'
```

Todos os erros seguem o mesmo formato:

```json
{
  "status": 400,
  "mensagem": "Dados inválidos. Verifique os campos informados.",
  "campos": [{ "campo": "contaOrigem", "mensagem": "deve conter exatamente 10 dígitos" }]
}
```

- `400` — dados inválidos ou JSON mal formado (Bean Validation).
- `422` — violação de regra de negócio (sem taxa aplicável, contas iguais).

## Decisões de arquitetura

### Backend

```
br.com.agendamento.transferencias
├── BackendApplication — inicialização e bean Clock (fuso America/Sao_Paulo)
├── controller/  TransferenciaController — endpoints REST
├── domain/      CalculadoraTaxa, FaixaTaxa — regras de negócio do cálculo da taxa
├── dto/         contratos de entrada e saída da API (request, response, erro)
├── entity/      Transferencia — entidade JPA
├── exception/   exceções de negócio e ApiExceptionHandler
├── repository/  TransferenciaRepository — Spring Data JPA
└── service/     TransferenciaService — orquestra o caso de uso e controla a transação
```

Fluxo: `controller` → `service` → `domain` (cálculo da taxa) e `repository` (persistência).

- **Regras de taxa isoladas em `domain`**: o `service` só orquestra (obtém a data de hoje, valida contas, calcula a taxa e salva). A regra de cálculo não depende de banco nem de HTTP e é testada com testes unitários puros, sem subir o Spring.
- **Tabela de taxas como `enum` (`FaixaTaxa`)**: cada faixa conhece seu intervalo de dias, taxa fixa e percentual. Incluir ou alterar uma faixa é uma mudança em um único lugar, sem cadeias de `if`.
- **`BigDecimal` para valores monetários**, construído a partir de `String` para evitar imprecisão de ponto flutuante, com escala 2 no banco (`NUMERIC(15,2)`).
- **`Clock` injetado**: "hoje" é fixado no fuso de São Paulo e os testes controlam a data, sem dependência do relógio da máquina.
- **DTOs separados da entidade**: o contrato da API não expõe a entidade JPA, e a entidade não tem setters — só é criada em estado válido.
- **Validação em duas camadas**: formato com Bean Validation no DTO (`400`) e regras de negócio no domínio/service via `RegraNegocioException` (`422`).
- **`@RestControllerAdvice` estendendo `ResponseEntityExceptionHandler`**: até as exceções padrão do Spring MVC (JSON inválido, método não suportado etc.) saem no mesmo formato de erro.
- **Endpoint de simulação de taxa**: o front mostra a taxa antes de confirmar sem reimplementar a regra no cliente; a regra fica em um único lugar.
- **`spring.jpa.open-in-view=false`**: evita consultas lazy fora da transação.

### Frontend

- **Componentes standalone + signals** (padrão atual do Angular), **Reactive Forms** para o formulário e validadores customizados (2 casas decimais, contas diferentes).
- **Página container + componentes de apresentação**: `TransferenciasPage` carrega o extrato e o recarrega quando o `AgendamentoForm` emite `agendada`; o `Extrato` só recebe dados por `input`.
- **Prévia da taxa** com `debounceTime` + `switchMap`: só a última simulação é considerada, sem condições de corrida entre requisições.
- **Proxy de desenvolvimento** em vez de liberar CORS na API: o front usa caminhos relativos (`/api/...`), como ficaria em produção atrás do mesmo domínio ou de um reverse proxy.
- **Locale `pt-BR` e moeda `BRL`** configurados globalmente para os pipes de data e moeda.
- **Bootstrap 5** para layout responsivo e componentes visuais, com a paleta do design system da Tokio Marine Brasil (verde `#006551` / `#004d3a` e dourado `#c7800b`) aplicada via variáveis CSS em `styles.scss`, sem recompilar o Bootstrap.
- **Change detection `OnPush`**: padrão a partir do Angular 22; o estado da tela é todo baseado em signals.
- **ESLint (angular-eslint)** com as regras recomendadas de TypeScript, templates e acessibilidade; **Prettier** para formatação.

### Docker

```
docker-compose.yaml
├── backend    eclipse-temurin:11 (JRE) — Spring Boot, porta 8080
└── frontend   nginx-unprivileged — SPA + reverse proxy de /api para o backend
```

- **Multi-stage build**: a imagem final leva só o necessário para rodar (JRE e jar; Nginx e arquivos estáticos), sem JDK, Maven, Node nem código-fonte.
- **Cache de dependências**: `pom.xml` e `package-lock.json` são copiados antes do código, e o BuildKit guarda `~/.m2` e `~/.npm` entre builds. Alterar só o código não baixa as dependências de novo.
- **Camadas do Spring Boot** (`layertools`): as dependências ficam numa camada separada do código da aplicação, então um rebuild muda só a camada pequena.
- **Containers sem root**: usuário `app` no backend e `nginx-unprivileged` no frontend, com `no-new-privileges`.
- **Health checks**: o backend expõe `/actuator/health` (Spring Boot Actuator, só o endpoint de health) e o frontend só sobe quando a API está saudável (`depends_on: condition: service_healthy`).
- **Mesmo modelo do desenvolvimento**: o Nginx faz em produção o papel do `proxy.conf.json`, então o front continua usando `/api` relativo e a API não precisa de CORS.
- **Nginx**: fallback de rotas do SPA para `index.html`, cache longo para arquivos com hash, `no-cache` para o `index.html`, gzip e headers de segurança.
- **JVM ciente do container**: `MaxRAMPercentage` dimensiona o heap pelo limite de memória do container, e o encerramento gracioso (`server.shutdown=graceful`) conclui as requisições em andamento no `docker compose down`.
- **Portas publicadas só em `127.0.0.1`**: o console do H2 fica acessível apenas na própria máquina.
- Os testes não rodam no build da imagem: ficam no fluxo de desenvolvimento/CI (`./mvnw test`, `npm test`), para o build ser rápido e reproduzível.

## Stack e versões

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 11 |
| Framework | Spring Boot 2.7.18 (Web, Data JPA, Validation, Actuator) |
| ORM | Hibernate 5.6 |
| Banco | H2 2.1 em memória |
| Build | Maven 3.9 (wrapper) |
| Testes backend | JUnit 5, Mockito, AssertJ, MockMvc, `@DataJpaTest` |
| Frontend | Angular 22, TypeScript 6, RxJS 7.8 |
| UI | Bootstrap 5.3 |
| Testes frontend | Vitest |
| Qualidade frontend | ESLint (angular-eslint), Prettier |
| Containers | Docker (multi-stage), Docker Compose, Nginx |
