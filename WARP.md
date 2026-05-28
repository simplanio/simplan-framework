# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

SimPlan is a unified data processing framework that provides a common abstraction for defining pluggable operators to process both batch and streaming data. It enables low-code/no-code execution plans that can run on different execution engines (Spark, Flink, Storm, Beam, or console applications).

This repository contains the **framework core**, which is execution-engine-agnostic. Concrete implementations (simplan-spark, simplan-flink, etc.) are maintained in separate repositories.

## Build System

This is a Maven-based Scala 2.12 project with multiple modules.

### Build Commands

```bash
# Build and run all tests
./build.sh

# Or directly with Maven
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests only
mvn test

# Check code coverage (Scoverage)
mvn scoverage:report

# Format code with Scalafmt
mvn scala-maven-plugin:compile
```

### Module Structure

The project follows a multi-module Maven structure:

- **global**: Global utilities and JSON mapping infrastructure
- **logging**: Framework-specific logging events and structures
- **common**: Common configuration, file utilities, scripting support
- **parser-dsl**: DSL parsing using ANTLR4 and Scala parser combinators
- **core**: Core operator abstractions, execution context, and built-in operators

Dependencies flow: `global` → `logging` → `common` → `parser-dsl` → `core`

## Architecture

### Operator System

The framework is built around **Operators**, which are units of work that can be composed into execution plans.

- **`Operator`** (`core/src/main/scala/com/intuit/data/simplan/core/domain/operator/Operator.scala`): Base abstract class for all operators
  - `process(request: OperatorRequest): OperatorResponse` - main execution method
  - `validateDefinition(definition: OperatorDefinition): Boolean` - validates operator configuration

- **`BaseOperator[T]`**: Type-safe operator base class that handles config parsing automatically

- **Operator Registration**: Operators are registered via HOCON configuration in `common-operator-mappings.conf`
  ```hocon
  simplan.application.operatorMappings {
    OperatorName = fully.qualified.ClassName
  }
  ```

### AppContext & OperatorContext

- **`AppContext`**: Maintains application-level initialization and execution context (analogous to SparkSession in Spark or StreamExecutionEnvironment in Flink). Provides:
  - Configuration management (HOCON-based via PureConfig)
  - File utilities (local, Hadoop, S3 abstractions)
  - OpsMetrics emission
  - Qualified string parameter resolution
  - Custom Jackson deserializers

- **`OperatorContext`**: Scoped context for individual operator execution with operator-specific configuration

### XCom (Cross-Communication)

**OperatorResponseManager** maintains operator response states in a mutable map of `XComWrapper` objects. This allows operators to:
- Access outputs from previously executed operators
- Pass data between operations without explicit data dependencies
- Query which operators produced specific responses

### Configuration System

- Uses **HOCON** format via PureConfig
- Base configuration: `common/src/main/resources/simplan-config-base.conf`
- Operator mappings: `common/src/main/resources/common-operator-mappings.conf`
- Qualified strings support variable interpolation with custom handlers

### Key Design Patterns

1. **Pluggable Operators**: New operators extend `Operator` or `BaseOperator[T]` and register via config
2. **Trait-based Capabilities**: System features added via Scala traits (e.g., `IDPSSupport`)
3. **Config-Driven**: Execution plans defined in HOCON, not hardcoded
4. **Engine-Agnostic**: Framework defines abstractions; implementations provide engine-specific execution

## Testing

- Framework uses **ScalaTest** (version 3.0.4)
- Test runner: `scalatest-maven-plugin`
- Run tests: `mvn test` (integrated into `./build.sh`)
- Tests located in `*/src/test/scala/` directories

## Code Style

- **Scalafmt** configuration: `.scalafmt.conf`
- Max line length: 300 characters
- Scala version: 2.12.12
- Java version: 1.8 (source/target compatibility)

## Release Process

- Main branch: `Imain` (note the unusual prefix)
- Version defined in: `library-config.yaml` (`artifactVersion` property)
- CI/CD: Jenkins pipeline (`Jenkinsfile`)
- Artifacts published to internal Intuit Artifactory
- Release builds: `${artifactVersion}.${BUILD_NUMBER}`
- Snapshot builds: `1.0.0-${branchName}-SNAPSHOT`
- **Only merges to `Imain` trigger artifact publication**

## Important Notes

- When creating commits, include co-author line: `Co-Authored-By: Warp <agent@warp.dev>`
- AWS credentials typically provided via IAM roles
- File operations abstract over Local/Hadoop/S3 via `FileUtils` interface
- Custom deserializers for complex config types registered in `AppContext`
- Framework documentation: https://simplanio.github.io/simplan-docs
