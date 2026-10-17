# Spring Config Guard

**Spring Config Guard** is an IntelliJ IDEA plugin that helps developers identify potentially risky Spring Boot configuration before deploying applications to production.

<img width="1132" height="845" alt="Spring Config Guard findings displayed in IntelliJ IDEA" src="https://github.com/user-attachments/assets/92661f66-a17d-4bec-8697-c018ed78a795" />

## Features

- Detect `localhost` and loopback addresses in configuration files.
- Flag risky Hibernate schema-generation settings, including `create` and `create-drop`.
- Identify potential hardcoded secrets without displaying secret values in findings.
- Detect potentially unsafe Spring Boot settings, including:
    - `spring.jpa.show-sql=true`
    - `management.endpoints.web.exposure.include=*`
    - `server.error.include-stacktrace=always`
    - `server.error.include-message=always`
- Display findings in a dedicated IntelliJ IDEA Tool Window.
- Highlight findings by severity, including `HIGH` and `WARNING`.
- Navigate to the corresponding source location by double-clicking a finding.

## How It Works

Spring Config Guard scans supported Spring configuration files and reports patterns that may indicate production risks.

The findings are intended to help developers review their configuration. They do not replace a security audit or a complete production-readiness review.

## Requirements

- A compatible IntelliJ IDEA version matching the IntelliJ Platform version configured in this project.
- Java 25 to build the project with the current Gradle configuration.

## Build from Source

Clone the repository:

```bash
git clone https://github.com/mahmoudabbasi/spring-config-guard.git
cd spring-config-guard
```

Run the tests and build the plugin:

```bash
./gradlew clean test buildPlugin
```

The plugin distribution ZIP will be generated in:

```text
build/distributions/
```

## Install Locally

1. Build the plugin using the command above.
2. Open IntelliJ IDEA and navigate to **Settings → Plugins**.
3. Open the settings menu and select **Install Plugin from Disk...**.
4. Select the generated ZIP file.
5. Restart the IDE if prompted.

## Limitations

Spring Config Guard is an early-development project. Its detection rules are heuristic and may produce false positives or miss certain risks.

Always review the findings in the context of your application and deployment environment.

## Contributing

Bug reports, feature requests, and contributions are welcome. Please open an issue to discuss significant changes before submitting a pull request.

## License

This project is licensed under the MIT License.
See the [LICENSE](LICENSE) file for details.
