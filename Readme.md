# SimPlan - Simple Execution Planner (Unified Processing Platform)

Unified Platform to process data in both batch and streaming as a unified platform with low code/no code capabilities. Provides a common abstraction to define pluggable operators for processing both batch and streaming data and run on different execution engines with ability to integrate with various source/sinks.

## Simplan Framework Documentation

## Simplan Implementations
- [Simplan Spark](https://github.com/simplanio/simplan-spark/)

## Build and Publish to artifactory

To build and run the unit tests:

```bash
./build.sh
```

When planning to upload a release build to maven central it's needed to update the artifact version in **"library-config.yaml"** file, in artifactVersion property.

The merge needs to be done to **master**, other branches and Pull Requests won't be uploaded to artifactory, they just get built.

The artifact versioning syntax in artifactory will be below.

### Master branch versioning (release):
```
${config.artifactVersion}.${BUILD_NUMBER}
```

Example: 1.0.0.100