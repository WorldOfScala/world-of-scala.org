# AGENTS.md - World Of Scala

A full-stack Scala web application: Scala 3 backend (ZIO, Tapir, ZIO-Magnum, OpenTelemetry) and Scala.js frontend (Laminar, Three.js, frontroute).

## Build & Test

**Build tool:** sbt 1.13.0 (Scala 3.9.0). The project uses `sbt-fullstack-js` plugin with managed scripts in `scripts-managed/`.

| Command | Description |
|---|---|
| `sbt compile` | Compile all main sources |
| `sbt test` | Compile and run all tests across all modules |
| `sbt Test/compile` | Compile test sources only |
| `sbt "server/test"` | Run backend/server tests only |
| `sbt "shared/test"` | Run shared (JVM) tests |
| `sbt "sharedJS/test"` | Run shared (Scala.js) tests |
| `sbt "client/test"` | Run client tests |
| `sbt "testOnly org.worldofscala.service.HasherSuite"` | Run a single test suite |
| `sbt "server/testOnly org.worldofscala.earth.MeshRepositorySpec"` | Single test in a module |
| `sbt -batch -Dsbt.supershell=false "server/test"` | Non-interactive test run (good for CI) |

**Run the app:**

| Command | Description |
|---|---|
| `./scripts/fullstackRun.sh` | Full-stack dev (server + client + Vite hot-reload) |
| `MOD=dev sbt server/run` | Run server manually |
| `MOD=dev sbt ~client/fastLinkJS` | Fast-compile client (watch mode) |
| `./scripts/ci-build.sh` | CI build: compiles server + bundles client |
| `./scripts/fullstackBuild.sh` | Production build (ESModule) |

**Docker:**
- `./scripts/dockerPublishLocal.sh` - Build & publish Docker image locally (requires `VERSION` env var)
- Database: `docker-compose up -d` (starts PostgreSQL on 5432)

## Formatting & Linting

| Command | Description |
|---|---|
| `sbt scalafmtAll` | Format all Scala sources |
| `sbt scalafmtCheckAll` | Check formatting (fails if unformatted) |
| `sbt scalafmtSbt` | Format sbt/scala source files |
| `sbt scalafix` | Run Scalafix (OrganizeImports, RemoveUnused) |
| `sbt scalafix --check` | Check Scalafix rules without applying |

VS Code auto-formats on save (`.vscode/settings.json`). The bloop build server and Metals IDE are configured.

## Project Structure

```
modules/
  backend/
    domain/          # Backend domain: ports/traits (UserPersistencePort, etc.)
    persistence/magnum/  # ZIO-Magnum persistence adapters
    server/          # HTTP server (controllers, services, config, Main.scala)
  client/            # Scala.js frontend (Laminar, Three.js, frontroute)
  domain/            # Cross-project domain models (crossType: Pure)
  shared/            # Cross-project shared code: endpoints, views, auth, errors
build.sbt            # Multi-project sbt build
project/
  Dependencies.scala # All library versions & dependency definitions
  plugins.sbt        # sbt plugins
  DeploymentSettings.scala  # Docker/deployment config
```

Key sbt subproject IDs: `backend`, `server`, `persistenceMagnum`, `domainBackend`, `client`, `shared`, `sharedJvm`, `sharedJs`, `domain`, `domainJvm`, `domainJs`.

## Code Style

### General
- **Language:** Scala 3.9.0 with `-source 3.4-migration`, `-new-syntax`, `-rewrite`.
- **Scala 3 syntax:** Use indentation-based syntax (significant indentation) where natural. `end` markers are used for large blocks. Braces are allowed for short expressions.
- **Max line width:** 120 characters (scalafmt `maxColumn = 120`). Docstrings wrap at 80 (`docstrings.wrapMaxColumn = 80`).
- **Indentation:** 2 spaces.
- **Compiler flags:** `-deprecation`, `-feature`, `-Wunused:all`, `-Xmax-inlines:64`, `-encoding utf8`, `-unchecked`.
- **No unnecessary comments.** Comments are only added when they clarify non-obvious logic. Avoid block comments.

### Imports
- Organize imports alphabetically, grouped: stdlib, third-party, local. Blank lines separate groups.
- Wildcard imports (`import foo.*`) are used for DSLs (e.g., `zio.*`, `zio.test.*`, `sttp.tapir.*`).
- Scalafix `OrganizeImports` merges wildcards when 3+ imports share a prefix (`coalesceToWildcardImportThreshold=3`).
- Import `given` instances explicitly where needed (e.g., `import UserView.given`, `import org.worldofscala.user.UserView.given`).

### Types & Naming

| Convention | Examples |
|---|---|
| Classes/traits/defs in endpoint files | PascalCase types, camelCase methods |
| `case class` | PascalCase (e.g., `UserToken`, `MeshView`) |
| `object` companion | PascalCase (e.g., `UserEndpoint`, `BaseEndpoint`) |
| `def`/methods | camelCase (e.g., `isValidEmail`, `generatedHash`) |
| `val`/`var` | camelCase (e.g., `baseEndpoint`, `dataSourceLayer`) |
| Constants (private vals) | UPPER_SNAKE_CASE (e.g., `PBKDF2_ALGORITHM`, `SALT_BYTE_SIZE`) |
| Private fields | `private val name` or `private def` |
| Opaque types | `opaque type Password <: String = String` |
| Type aliases | camelCase (e.g., `type Task[A] = ZIO[Any, Throwable, A]`) |
| `ZLayer` vals | Named `layer` in companion objects (e.g., `UserServiceLive.layer`) |
| Test specs | `*Spec`, `*Suite` suffixes (e.g., `HasherSuite`, `NewUserSpec`) |

### Patterns

- **Dependency injection:** Define a `trait Service`, implement as `class ServiceLive private (...)`, expose `val layer: RLayer[Dependencies, Service] = ZLayer.derive[ServiceLive]` in companion.
- **Controllers:** Extend `SecuredBaseController` or `BaseController` from `dev.cheleb.ziotapir.server`.
- **Endpoints:** Extend `BaseEndpoint`; use `baseEndpoint` or `baseSecuredEndpoint`.
- **Error handling:** Map exceptions to HTTP via `HttpError.encode` in `BaseEndpoint`. Domain exceptions (e.g., `UserAlreadyExistsException`, `InvalidCredentialsException`) are defined as case classes/objects in `org.worldofscala.domain.errors`.
- **JSON:** Use `zio-json` (`derives JsonCodec`) and Tapir `Schema` derivation (`derives Schema`). `Debug` derivation via `zio-prelude` for sensitive types (e.g., `Password` shows `"*****"`).
- **Transformations:** Use `chimney` `.into[Target].transform` or `zio-chimney` `.mapInto[Target]`.

### Scala.js / Frontend
- Entry point: `@main def main: Unit =` in `org.worldofscala.app.Main`.
- UI: Laminar (`com.raquo.laminar.api.L.*`), render to DOM element `#app`.
- Routing: `frontroute.LinkHandler`.
- Three.js: `dev.cheleb.threesjs` (Scala.js facade).

## Testing

Two test frameworks are used:

### ZIO Test (`ZIOSpecDefault`)
```scala
package org.worldofscala.service

import zio.test.*
import zio.test.Assertion.*

object HasherSuite extends ZIOSpecDefault {
  override def spec = suite("Hasher")(
    test("generates different hashes") {
      assertTrue(Hasher.generatedHash("a") != Hasher.generatedHash("b"))
    }
  )
}
```

### MUnit (`FunSuite`)
```scala
package org.worldofscala.service

import munit.*
import org.worldofscala.auth.Hasher

class HasherSuite extends FunSuite {
  test("handles empty string") {
    assert(Hasher.generatedHash("").nonEmpty)
  }
}
```

Run a single test suite: `sbt "testOnly <full.package.ClassName>"`.

## Copilot / Editor Instructions

The `.github/copilot-instructions.md` file notes this is a Scala/ScalaJS project using SBT with ZIO, Laminar, and Tapir HTTP libraries.

## Environment

- JDK 23 (Zulu).
- Node.js via `bun` (used for client dev server and npm operations).
- PostgreSQL 18 (via docker-compose) for integration tests using Testcontainers.
- `.envrc` (direnv) sets `LOCAL_DOCKER_REGISTRY`, `OTEL_LOG_LEVEL`, `OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE`.
