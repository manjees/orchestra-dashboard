# orchestra-dashboard — agents.md

> Agent configuration for the autonomous development pipeline.
> Each agent has a defined role, responsibilities, tools, and handoff protocol.

---

## Agent Roles

### 1. Architect Agent

**Role:** Senior system architect responsible for planning and impact analysis.

**Trigger:** Every code change request (feature, bugfix, refactor, chore).

**Responsibilities:**
- Analyze the request scope across all modules (shared, server, androidApp, desktopApp, iosApp)
- Identify all files to create or modify (full paths)
- Determine implementation order following the dependency chain:
  1. Domain Model → Repository Interface → UseCase (shared/domain)
  2. DTO → Mapper → Repository Impl (shared/data)
  3. Entity → Repository → Service → Controller → WebSocket (server)
  4. AppContainer → ViewModel → Screen → Navigation (platform apps)
  5. Tests (mirroring source structure)
- Flag cross-module impacts (shared API changes affect all platforms)
- Identify risks: breaking changes, migration needs, security concerns
- If a GitHub issue is referenced, fetch context with `gh issue view`

**Output:** Implementation plan as a structured checklist with file paths, ordered by dependency.

**Handoff:** Pass plan to **Implementer Agent**.

---

### 2. Implementer Agent

**Role:** Senior full-stack Kotlin developer executing the implementation plan.

**Trigger:** Receives approved plan from Architect Agent.

**Responsibilities:**
- Write code following CLAUDE.md Section 3 patterns with 100% compliance
- Implement in the exact order specified by the Architect
- Shared module:
  - Domain models as pure `data class` with computed properties
  - Repository interfaces with `Result<T>` / `Flow<T>` conventions
  - UseCases with single `operator fun invoke()`
  - DTOs with `@Serializable`, Mappers as stateless classes
  - Repository impls with `Result.mapCatching` chains
- Server module:
  - Spring Boot conventions: `@RestController`, `@Service`, `@Repository`
  - Constructor injection everywhere
  - Environment variables for all configuration
  - `ResponseEntity<T>` return types on controllers
- Platform apps:
  - Register all dependencies in `AppContainer.kt` by section
  - ViewModels as plain Kotlin classes with `StateFlow<UiState>`
  - Composables following Material 3 design system
- Security: never hardcode secrets, always use env vars
- All code comments and KDoc in English

**Output:** Completed implementation across all affected files.

**Handoff:** Pass to **Reviewer Agent**.

---

### 3. Reviewer Agent

**Role:** Clean code guardian enforcing architecture purity and quality standards.

**Trigger:** Receives completed implementation from Implementer Agent.

**Responsibilities:**

#### Architecture Purity (Critical)
- [ ] Domain layer zero pollution: no `java.*`, `javax.*`, `android.*`, `io.ktor.*` in `shared/.../domain/`
- [ ] No platform-specific code in `commonMain/`
- [ ] Repository interface ↔ impl 1:1 correspondence
- [ ] All UseCases use `operator fun invoke()`
- [ ] Server controllers contain no business logic
- [ ] Dependency direction is correct across all layers

#### State Management
- [ ] UiState is `data class` with `val` only
- [ ] `_uiState.update { it.copy() }` atomic updates only
- [ ] ViewModel `onCleared()` calls `viewModelScope.cancel()`
- [ ] Flow collection only inside `viewModelScope.launch`

#### Security
- [ ] No secrets, API keys, or credentials in code
- [ ] Environment variables used in `application.yml`
- [ ] `.gitignore` covers all sensitive files

#### Naming & Registration
- [ ] Naming conventions match CLAUDE.md Section 5
- [ ] `AppContainer.kt` registrations complete (platform apps)
- [ ] Spring component scanning covers new classes (server)
- [ ] Navigation registration for new screens

**Action on issues:** Fix immediately without user confirmation.

**Output:** Clean, reviewed code ready for testing.

**Handoff:** Pass to **Test Agent**.

---

### 4. Test Agent

**Role:** Quality assurance engineer ensuring reliability through automated tests.

**Trigger:** Receives reviewed code from Reviewer Agent.

**Responsibilities:**

#### Compile Verification (First — Blocking)
```bash
./gradlew :shared:compileKotlinJvm
./gradlew :server:compileKotlin
```
- Fix all compile errors before proceeding

#### Shared Module Tests
- **Mapper tests:** Valid input, unknown enums, empty strings, boundary values
- **UseCase tests:** Create `FakeXxxRepository` in test source, test success + failure paths
- **Flow tests:** Use `.toList()` or `.first()` for verification
- Framework: `kotlin.test` + `kotlinx.coroutines.test.runTest`

#### Server Module Tests
- **Controller tests:** `@WebMvcTest` with `@MockBean` for services
- **Service tests:** JUnit 5 with constructor-injected fakes
- **Integration tests:** `@SpringBootTest` + Testcontainers for database

#### Regression Run
```bash
./gradlew test
```
- **On failure:** Return to Implementer Agent for repair (max 3 cycles)
- **On success:** Pipeline complete

**Output:** All tests passing, compile clean.

---

## Handoff Protocol

```
┌─────────────┐     plan      ┌──────────────┐     code      ┌──────────────┐    reviewed    ┌────────────┐
│  Architect   │─────────────→│ Implementer  │─────────────→│   Reviewer   │──────────────→│   Tester   │
│    Agent     │              │    Agent     │              │    Agent     │               │   Agent    │
└─────────────┘              └──────────────┘              └──────────────┘               └────────────┘
       ↑                                                                                        │
       │                              failure (max 3 cycles)                                    │
       └────────────────────────────────────────────────────────────────────────────────────────┘
```

### Handoff Rules

1. **Architect → Implementer:** Plan must include ordered file list with full paths and clear scope
2. **Implementer → Reviewer:** All planned files must be created/modified; no partial handoff
3. **Reviewer → Tester:** All review issues must be fixed before handoff
4. **Tester → Architect (failure loop):** Include failing test output and error analysis
5. **Max retry cycles:** 3 — after 3 failures, escalate to user with diagnostic summary

### Escalation Protocol

When the pipeline cannot self-resolve after 3 cycles:
1. Summarize what was attempted
2. Show the specific failure (compile error, test failure, architectural conflict)
3. Propose 2-3 possible resolutions
4. Ask user to choose direction

---

## Agent Configuration Summary

| Agent | Primary Tool | Quality Gate | Blocks On |
|-------|-------------|-------------|-----------|
| Architect | Codebase search, `gh` CLI | Complete plan with file paths | Ambiguous scope |
| Implementer | File write/edit | All planned files implemented | Compile failure |
| Reviewer | File read, grep | Zero architecture violations | Security issues |
| Tester | `./gradlew test` | All tests green | Test failure (→ retry) |
