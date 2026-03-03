# Orchestra Dashboard

> **A world-class, open-source monitoring dashboard for AI agents** — built with Kotlin Multiplatform (Android · iOS · Desktop) and Spring Boot 3.

[![Build](https://github.com/manjees/orchestra-dashboard/actions/workflows/ci.yml/badge.svg)](https://github.com/manjees/orchestra-dashboard/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.10-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-green.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## Overview

**Orchestra Dashboard** gives you real-time visibility into your AI agent fleet — orchestrators, workers, reviewers, planners — from a single, unified interface. Whether you're running a multi-agent pipeline in production or experimenting locally, Orchestra Dashboard surfaces the metrics that matter.

```
┌─────────────────────────────────────────────┐
│         Orchestra Dashboard                  │
│                                             │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐    │
│  │  Agent  │  │ Metrics │  │  Logs   │    │
│  │  Fleet  │  │ Charts  │  │ Stream  │    │
│  └─────────┘  └─────────┘  └─────────┘    │
│                    │                        │
│         Spring Boot 3 Backend               │
│          WebSocket + REST API               │
└─────────────────────────────────────────────┘
         ↑              ↑              ↑
     Android App    Desktop App     iOS App
   (Compose UI)  (Compose Desktop) (SwiftUI)
```

---

## Features

- **Real-time Agent Monitoring** — Live status updates via WebSocket for every agent in your pipeline
- **Cross-platform Clients** — Native Android, iOS, and Desktop apps sharing a single Kotlin codebase
- **REST + WebSocket API** — Spring Boot 3 backend with a clean, versioned REST API
- **Agent Lifecycle Tracking** — Heartbeat monitoring, status transitions, error detection
- **Metric Aggregation** — Per-agent and fleet-wide metrics with historical charts
- **Secure by Default** — All secrets via environment variables; zero credentials in code

---

## Architecture

### Module Structure

```
orchestra-dashboard/
├── shared/           # Kotlin Multiplatform — domain, data, utilities
│   └── src/
│       ├── commonMain/   # Platform-agnostic business logic
│       ├── androidMain/  # Android-specific implementations
│       ├── iosMain/      # iOS-specific implementations
│       └── desktopMain/  # Desktop-specific implementations
├── androidApp/       # Android app (Jetpack Compose)
├── iosApp/           # iOS app (SwiftUI + KMP bridge)
├── desktopApp/       # Desktop app (Compose Multiplatform)
└── server/           # Spring Boot 3 REST + WebSocket backend
```

### Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Kotlin | 2.0.10 |
| Multiplatform | Kotlin Multiplatform | 2.0.10 |
| UI (Android/Desktop) | Compose Multiplatform | 1.7.x |
| UI (iOS) | SwiftUI | iOS 14+ |
| Backend | Spring Boot | 3.3.x |
| Build | Gradle (Kotlin DSL) | 8.7+ |
| JDK | OpenJDK | 21 LTS |
| Serialization | kotlinx.serialization | 1.7.x |
| Coroutines | kotlinx.coroutines | 1.9.x |
| Database | H2 (dev) / PostgreSQL (prod) | — |
| Testing (shared) | kotlin.test + Coroutines Test | — |
| Testing (server) | JUnit 5 + Spring Boot Test + Testcontainers | — |

---

## Quick Start

### Prerequisites

| Tool | Minimum Version | Install |
|------|----------------|---------|
| JDK | 21 LTS | `brew install openjdk@21` |
| Android Studio | Hedgehog+ | [developer.android.com](https://developer.android.com/studio) |
| Xcode | 15.1+ | Mac App Store |
| CocoaPods | 1.14+ | `brew install cocoapods` |

### Clone & Build

```bash
git clone https://github.com/manjees/orchestra-dashboard.git
cd orchestra-dashboard

# Run the server
./gradlew :server:bootRun

# Run the desktop app
./gradlew :desktopApp:run

# Build the Android app
./gradlew :androidApp:assembleDebug
```

### Environment Setup

Copy the example environment file and fill in your values:

```bash
cp .env.example .env
```

```dotenv
# .env  ← never commit this file
DB_URL=jdbc:postgresql://localhost:5432/orchestra
DB_USER=your_db_user
DB_PASS=your_db_password
SERVER_PORT=8080
ORCHESTRATOR_API_URL=http://localhost:9000
ORCHESTRATOR_API_KEY=your_api_key_here
```

The server reads these via Spring's `${ENV_VAR:default}` placeholders in `application.yml`.

---

## Development Setup

### JDK 21

```bash
# macOS
brew install openjdk@21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Add to ~/.zshrc or ~/.bash_profile
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 21)' >> ~/.zshrc
```

### Android SDK

1. Open Android Studio → SDK Manager
2. Install **Android SDK API 34** (target) and **API 24** (minimum)
3. The `local.properties` file (gitignored) is created automatically by Android Studio:
   ```properties
   sdk.dir=/Users/yourname/Library/Android/sdk
   ```

### iOS (macOS only)

```bash
# Install CocoaPods
brew install cocoapods

# Open the iOS app in Xcode
open iosApp/iosApp.xcworkspace
```

### Server (Spring Boot)

```bash
# Start with H2 in-memory database (no setup needed)
./gradlew :server:bootRun

# API available at:
# http://localhost:8080/api/v1/agents
# ws://localhost:8080/ws/agents
```

---

## Building & Testing

### Build Commands

```bash
# Full build (all modules)
./gradlew clean build

# Per-module
./gradlew :server:build
./gradlew :androidApp:assembleDebug
./gradlew :desktopApp:createDistributable

# Package for release
./gradlew :androidApp:bundleRelease    # Android AAB
./gradlew :server:bootJar              # Executable JAR (Docker-ready)
```

### Test Commands

```bash
# All tests
./gradlew test

# Shared KMP tests
./gradlew :shared:allTests

# Server tests (unit + integration)
./gradlew :server:test

# With Testcontainers (requires Docker)
./gradlew :server:integrationTest
```

### Code Quality

```bash
# Static analysis (Detekt)
./gradlew detekt

# Formatting check (ktlint)
./gradlew ktlintCheck

# Apply formatting fixes
./gradlew ktlintFormat
```

---

## API Reference

### Agents

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/agents` | List all monitored agents |
| `GET` | `/api/v1/agents/{id}` | Get agent by ID |
| `POST` | `/api/v1/agents` | Register a new agent |
| `DELETE` | `/api/v1/agents/{id}` | Deregister an agent |

### Metrics

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/metrics` | Get latest fleet metrics |
| `GET` | `/api/v1/metrics/{agentId}` | Get metrics for a specific agent |

### WebSocket

```
ws://localhost:8080/ws/agents
```

Receives real-time agent status events as JSON:

```json
{
  "type": "AGENT_STATUS_CHANGED",
  "agentId": "worker-42",
  "status": "RUNNING",
  "timestamp": 1700000000000
}
```

---

## Contributing

We welcome contributions from the community! Here's how to get involved:

### Getting Started

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/<issue-number>-short-description`
3. Make your changes following the patterns in `.claude/CLAUDE.md`
4. Run tests: `./gradlew test`
5. Run lint: `./gradlew detekt`
6. Open a pull request against `main`

### Code Style

- Follow Kotlin official coding conventions
- All code comments and KDoc must be in **English**
- Commit messages use the format: `type(scope): Subject` (e.g., `feat(agents): Add heartbeat timeout detection`)
- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

### Pull Request Checklist

- [ ] Tests added/updated for new functionality
- [ ] All existing tests pass (`./gradlew test`)
- [ ] Detekt passes (`./gradlew detekt`)
- [ ] No secrets or credentials committed
- [ ] KDoc added for new public APIs
- [ ] CLAUDE.md architecture patterns followed

---

## Security

### Reporting Vulnerabilities

Please **do not** open a public GitHub issue for security vulnerabilities. Instead, report them via [GitHub Security Advisories](https://github.com/manjees/orchestra-dashboard/security/advisories/new).

### Secrets Policy

- **Zero secrets in code** — all sensitive values come from environment variables
- `local.properties`, `.env`, and `secrets.json` are permanently gitignored
- CI/CD secrets are managed via GitHub Actions Secrets

---

## Deployment

### Docker (Spring Boot Server)

```dockerfile
# Dockerfile is provided in server/
docker build -t orchestra-dashboard-server ./server
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://db:5432/orchestra \
  -e DB_USER=postgres \
  -e DB_PASS=secret \
  orchestra-dashboard-server
```

### Docker Compose (Full Stack)

```bash
docker compose up -d
```

This starts:
- Spring Boot server on port `8080`
- PostgreSQL database on port `5432`
- Grafana dashboard on port `3000`

---

## License

This project is licensed under the **Apache License 2.0**.
See [LICENSE](LICENSE) for the full text.

---

## Acknowledgments

Built with Kotlin Multiplatform, Spring Boot, and Jetpack Compose — open-source technologies from JetBrains, Pivotal/VMware, and Google.

---

<p align="center">
  <strong>Orchestra Dashboard</strong> — bringing clarity to your AI agent fleet
</p>
