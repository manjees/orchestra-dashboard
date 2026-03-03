# orchestra-dashboard — CLAUDE.md

> **Context Poisoning Prevention (Absolute Rule)**
> All generated code must reference ONLY the patterns in Section 3. Do not introduce external DI frameworks (Koin, Hilt, Dagger) or test frameworks (MockK, Mockito) unless explicitly approved by the maintainer. If a new dependency is required, ask first.

---

## Section 1: Project Overview

**orchestra-dashboard** is a public open-source monitoring dashboard for AI agents, built with Kotlin Multiplatform (Android, iOS, Desktop) and Spring Boot 3.

| Item | Value |
|------|-------|
| Tech | Kotlin 2.0.10, Compose Multiplatform 1.7.x, Spring Boot 3.3.x, Coroutines 1.9.x |
| JDK | 21 LTS |
| Build System | Gradle 8.7+ with Kotlin DSL (`.kts`) |
| Version Catalog | `gradle/libs.versions.toml` |
| Min Android SDK | 24 |
| Target Android SDK | 34 |
| iOS Minimum | 14.0 |
| Base Package | `com.orchestradashboard` |
| Build (full) | `./gradlew clean build` |
| Build (server) | `./gradlew :server:build` |
| Build (shared tests) | `./gradlew :shared:allTests` |
| Build (android) | `./gradlew :androidApp:assembleDebug` |
| Build (desktop) | `./gradlew :desktopApp:run` |
| Test (all) | `./gradlew test` |
| Test (server) | `./gradlew :server:test` |
| Lint | `./gradlew detekt` |

---

## Section 2: Architecture

### 2.1 Module Structure

```
orchestra-dashboard/
├── shared/                          # KMP shared module
│   └── src/
│       ├── commonMain/kotlin/com/orchestradashboard/shared/
│       │   ├── domain/              # Models, repository interfaces, usecases
│       │   ├── data/                # Repository impls, DTOs, mappers
│       │   └── util/                # Pure Kotlin utilities
│       ├── androidMain/
│       ├── iosMain/
│       └── desktopMain/
├── androidApp/                      # Android Compose app
│   └── src/main/kotlin/com/orchestradashboard/android/
│       ├── ui/                      # Screens, components, theme, navigation
│       ├── di/                      # AppContainer (manual DI)
│       └── App.kt
├── desktopApp/                      # Compose Desktop app
│   └── src/main/kotlin/com/orchestradashboard/desktop/
│       ├── ui/
│       ├── di/
│       └── Main.kt
├── iosApp/                          # Xcode project (SwiftUI wrapper)
├── server/                          # Spring Boot 3 backend
│   └── src/
│       ├── main/kotlin/com/orchestradashboard/server/
│       │   ├── config/              # Spring configuration
│       │   ├── controller/          # REST controllers
│       │   ├── service/             # Business logic
│       │   ├── repository/          # Data access (Spring Data)
│       │   ├── model/               # Entity & DTO classes
│       │   ├── websocket/           # Real-time agent events
│       │   └── Application.kt
│       └── test/kotlin/com/orchestradashboard/server/
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── .gitignore
└── README.md
```

### 2.2 Layer Rules

#### Shared Module (KMP)

| Layer | Path | May Depend On | Must NOT Depend On |
|-------|------|---------------|---------------------|
| Domain | `shared/.../domain/` | Pure Kotlin + `kotlinx.coroutines.flow.Flow` only | data, util, platform APIs, `java.*`, `javax.*`, `android.*`, `io.ktor.*` |
| Data | `shared/.../data/` | domain | Platform APIs directly, util internals |
| Util | `shared/.../util/` | Nothing | domain, data |

**Dependency direction:** Platform Apps → shared/domain ← shared/data

#### Server Module (Spring Boot)

| Layer | Path | May Depend On | Must NOT Depend On |
|-------|------|---------------|---------------------|
| Controller | `server/.../controller/` | service, model | repository directly |
| Service | `server/.../service/` | repository, model | controller |
| Repository | `server/.../repository/` | model | controller, service |
| Model | `server/.../model/` | Nothing | Any other layer |
| Config | `server/.../config/` | All layers | — |
| WebSocket | `server/.../websocket/` | service, model | repository directly |

---

## Section 3: Code Pattern Reference

> **Language Rule for Code:** All KDoc, inline comments (`//`), section comments, and test method names (backtick) must be written in **English**. No non-English comments in code.

### 3.1 Domain Model (Shared)

```kotlin
/**
 * Represents an AI agent being monitored
 */
data class Agent(
    val id: String,
    val name: String,
    val type: AgentType,
    val status: AgentStatus,
    val lastHeartbeat: Long,
    val metadata: Map<String, String> = emptyMap()
) {
    enum class AgentType { ORCHESTRATOR, WORKER, REVIEWER, PLANNER }
    enum class AgentStatus { RUNNING, IDLE, ERROR, OFFLINE }

    val displayName: String get() = "$name (${type.name.lowercase()})"
    val isHealthy: Boolean get() = status == AgentStatus.RUNNING || status == AgentStatus.IDLE
}
```

### 3.2 Repository Interface (Shared Domain)

```kotlin
interface AgentRepository {
    fun observeAgents(): Flow<List<Agent>>
    suspend fun getAgent(agentId: String): Result<Agent>
}
```

### 3.3 UseCase (Shared Domain)

```kotlin
class ObserveAgentsUseCase(
    private val agentRepository: AgentRepository
) {
    operator fun invoke(): Flow<List<Agent>> = agentRepository.observeAgents()
}
```

### 3.4 ViewModel (Platform Apps)

```kotlin
class DashboardViewModel(
    private val observeAgentsUseCase: ObserveAgentsUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun startObserving() {
        viewModelScope.launch {
            observeAgentsUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { agents -> _uiState.update { it.copy(agents = agents, isLoading = false) } }
        }
    }

    fun onCleared() { viewModelScope.cancel() }
}
```

### 3.5 Spring Boot Controller (Server)

```kotlin
@RestController
@RequestMapping("/api/v1/agents")
class AgentController(private val agentService: AgentService) {
    @GetMapping
    fun getAgents(): ResponseEntity<List<AgentResponse>> = ResponseEntity.ok(agentService.getAllAgents())
}
```

### 3.6 Spring Boot Configuration (Server)

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:h2:mem:dashboard}
    username: ${DB_USER:sa}
    password: ${DB_PASS:}
```

---

## Section 4: Autonomous Agent Pipeline

All code modification requests execute these 4 steps sequentially:

1. **Planning (Architect's Eye):** Impact analysis across all modules, ordered file list
2. **Development (Senior Implementation):** Section 3 patterns, 100% compliance
3. **Self-Review (Clean Code Guard):** Architecture purity, state management, security checks
4. **Automated Testing:** Compile verification → unit tests → regression run

---

## Section 5: Naming Conventions

| Element | Pattern | Example |
|---------|---------|---------|
| Domain Model | PascalCase data class | `Agent`, `PipelineRun` |
| Repository Interface | `{Feature}Repository` | `AgentRepository` |
| Repository Impl | `{Feature}RepositoryImpl` | `AgentRepositoryImpl` |
| UseCase | `{Verb}{Noun}UseCase` | `ObserveAgentsUseCase` |
| DTO | `{Model}Dto` | `AgentDto` |
| Mapper | `{Model}Mapper` | `AgentMapper` |
| ViewModel | `{Screen}ViewModel` | `DashboardViewModel` |
| UiState | `{Screen}UiState` | `DashboardUiState` |
| Screen | `{Name}Screen` | `DashboardScreen` |
| Controller | `{Resource}Controller` | `AgentController` |
| Service | `{Feature}Service` | `AgentService` |
| Spring Entity | `{Name}Entity` | `AgentEntity` |
| Response DTO | `{Name}Response` | `AgentResponse` |

---

## Section 6: Commit, Branch & PR

- **Commit messages:** `type(scope): subject` — always in **English**
- **Branches:** `feature/<issue>-<description>`

### AI Attribution — Forbidden (Absolute Rule)

- **Never** add `Co-Authored-By: Claude ...` to commits
- **Never** include "Generated with Claude Code" in PR bodies
- **Never** leave AI attribution in code comments

### Git Workflow

1. Branch from `main` before work
2. All commits on feature branch
3. Create PR via `gh pr create`
4. **Never push directly to `main`**

---

## Section 7: Security

### Environment Variables

| Variable | Purpose | Default |
|----------|---------|---------|
| `DB_URL` | Database connection string | `jdbc:h2:mem:dashboard` |
| `DB_USER` | Database username | `sa` |
| `DB_PASS` | Database password | (empty) |
| `SERVER_PORT` | Spring Boot server port | `8080` |
| `ORCHESTRATOR_API_URL` | AI orchestrator endpoint | `http://localhost:9000` |
| `ORCHESTRATOR_API_KEY` | API authentication key | (empty) |

### Rules

- Never commit secrets, API keys, or credentials
- Use `${ENV_VAR:default}` pattern in `application.yml`
- `local.properties` always gitignored

---

## Section 8: CI/CD

```yaml
# .github/workflows/ci.yml
jobs:
  build:
    steps:
      - gradle build --parallel
      - gradle test
      - gradle detekt
```
