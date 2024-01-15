# SimPlan - Simple Execution Planner

Simplan framework offers a way to auther LowCode/NoCode operations in a simple way. It is based on the concept of a plan, which is a set of operations that are executed in a specific order. The operations are defined in a HOCON file and can be executed by the SimPlan framework. 

The framework is built around the concept of operators. An operator is intended to perform an operation which is a unit of work the framework can perform. Operators can be written to abstract complex functionalities within themselves and expose certain configurations to customize their behavior. These operators are grouped to form tasks and it can be configured to run in a certain order to collectively perform the intended outcome. It provides dependency chaining, operatior/task/application level metrics, monitoring, and quality control with circuit breakers before and after performing each operation definition. A rich definition grammar enforces a quick and consistent way to define operations and its dependencies.

## Features/Benefits
- Config Driven (Low/No code)
- Configurable and Pluggable Operators
- Maintains and abstracts application initialization execution context.
    - Eg: In the case of Spark, SparkSession and SparkContext and in case of flink, StreamExecutionEnvironment and StreamTableEnvironment etc
- Connect to any JDBC sources and execute like Redshift, Athena, Presto, etc
- Built-In Quality control with circuit breakers pre/post each operator execution.
- Lineage, Observability, and Metrics tracking are built into the framework
- Metrics are emitted as a specific category within log4j which can be handled by any appender and published to an aggregation tool the user chooses like Elasticsearch/Opensearch/Splunk.
- Maintains XComs which maintains operator response states, and provides ability to cross communicate between operations.
- System-level features can be added as additive functions (extend Support trait)
    - Eg: IDPS Support can be added by just mentioning it as follows.
``` scala
val context = new ConsoleAppContext(config) with IDPSSupport
```

- A lot of utils for Config parsing, Managing Execution context, Json Mapping, Exception handling, Execution tracking, metrics publishing, AWS-specific services like STS and S3, abstractions to handle Local/Hadoop/S3 file operations, to name a few.

## Simplan for Data Process Authoring
[Simplan Spark](https://github.intuit.com/pages/Simplan/simplan-spark/) and [Simplan Flink](https://github.intuit.com/pages/tabraham1/simplan-flink/) are 2 different implementations of Simplan framework for data processing built on top of Apache Spark and Apache Flink respectively. These implementations are built to support the same set of features and functionalities on top of correspnding execution environments. Simplan's common configuration structure in thtended to allow easy migration of plans between different execution environments.

## Simplan for Orchestration
Superglue uses Simplan framework for orchestration all its jobs. [SG-Client](https://github.intuit.com/Superglue/sg-client) is a simplan based appliation which is used to orchestrate all the jobs in Superglue.


## Presentations
### Simplan @ DataAI Summit
<iframe width="560" height="315" src="https://www.youtube.com/embed/RrOKWN-wHac" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Community/Support
- Join Simplan Slack Channel [#simplan-community](https://intuit-teams.slack.com/archives/C041VUJ278X)