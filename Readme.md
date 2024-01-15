# SimPlan - Simple Execution Planner (Unified Processing Platform)

[![Build Status](https://build.intuit.com/tech-ea/buildStatus/buildIcon?job=Simplan/simplan-framework/simplan-framework/master/)](https://build.intuit.com/tech-ea/job/Simplan/job/simplan-framework/job/simplan-framework/job/master/) 

Unified Platform to process data in both batch and streaming as a unified platform with low code/no code capabilities. Provides a common abstraction to define pluggable operators for processing both batch and streaming data and run on different execution engines with ability to integrate with various source/sinks.

## Simplan Framework Documentation
- For framework documentation : [Simplan Framework](https://github.intuit.com/pages/Simplan/simplan-framework/)

## Simplan Implementations
- [Simplan Spark](https://github.intuit.com/pages/Simplan/simplan-spark/)
- [Simplan Flink](https://github.intuit.com/pages/tabraham1/simplan-flink/)
- [Simplan Presto](https://github.intuit.com/pages/Simplan/simplan-presto/)

## Build and Publish to artifactory

To build and run the unit tests:

```bash
./build.sh
```

When planning to upload a release build to artifactory it's needed to update the artifact version in **"library-config.yaml"** file, in artifactVersion property.

The merge needs to be done to **master**, other branches and Pull Requests won't be uploaded to artifactory, they just get built.

The artifact versioning syntax in artifactory will be below.

### Master branch versioning (release):
```
${config.artifactVersion}.${BUILD_NUMBER}
```

Example: 1.0.0.100
