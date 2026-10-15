# Spring Config Guard

An IntelliJ IDEA plugin that detects potentially risky Spring Boot configuration.

## Features

- Detect localhost addresses in configuration files
- Detect risky Hibernate schema-generation settings
- Identify potential hardcoded secrets
- Flag potentially unsafe Spring Boot production settings
- Display findings in a dedicated Tool Window
- Color-code findings by severity
- Navigate to the source location by double-clicking a finding

## Requirements

- IntelliJ IDEA compatible with the configured IntelliJ Platform version
- Java 25 for building this project with the current Gradle configuration

## Build

```bash
./gradlew clean test buildPlugin
```

The plugin ZIP will be generated under `build/distributions/`.

## Install locally

In IntelliJ IDEA, open Settings → Plugins, select the settings menu, choose **Install Plugin from Disk**, and select the generated ZIP.

## Current status

Early development version. Detection rules are heuristic and may produce false positives or miss some risky configurations.

## License

Choose and add a license before distributing the project publicly.
