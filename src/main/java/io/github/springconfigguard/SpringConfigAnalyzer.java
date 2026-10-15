package io.github.springconfigguard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SpringConfigAnalyzer {

    private static final Pattern LOCALHOST = Pattern.compile(
            "(?i)\\b(localhost|127\\.0\\.0\\.1)\\b"
    );

    private static final Pattern DDL_AUTO = Pattern.compile(
            "(?i)\\bddl-auto\\s*[:=]\\s*[\"']?(create|create-drop)\\b"
    );

    private static final Pattern SECRET = Pattern.compile(
            "(?i)(?:^|[.\\-_])" +
                    "(password|passwd|client-secret|api-key|access-token|" +
                    "auth-token|private-key|secret-key)" +
                    "\\s*[:=]\\s*([^\\s#]+)"
    );


    private static final Pattern SHOW_SQL = Pattern.compile(
            "(?i)^\\s*spring\\.jpa\\.show-sql\\s*[:=]\\s*true\\b"
    );

    private static final Pattern ACTUATOR_EXPOSURE = Pattern.compile(
            "(?i)^\\s*management\\.endpoints\\.web\\.exposure\\.include\\s*[:=]\\s*[\"']?\\*"
    );

    private static final Pattern STACKTRACE_ALWAYS = Pattern.compile(
            "(?i)^\\s*server\\.error\\.include-stacktrace\\s*[:=]\\s*[\"']?always\\b"
    );

    private static final Pattern ERROR_MESSAGE_ALWAYS = Pattern.compile(
            "(?i)^\\s*server\\.error\\.include-message\\s*[:=]\\s*[\"']?always\\b"
    );

    public List<ConfigFinding> analyze(
            String fileName,
            String content
    ) {
        List<ConfigFinding> findings = new ArrayList<>();
        String[] lines = content.split("\\R", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Ignore full-line YAML and properties comments.
            String trimmed = line.trim();
            if (trimmed.startsWith("#") || trimmed.startsWith("!")) {
                continue;
            }

            findMatches(
                    findings,
                    fileName,
                    i + 1,
                    line,
                    LOCALHOST,
                    "WARNING",
                    "Localhost address found; verify this value for production."
            );

            findMatches(
                    findings,
                    fileName,
                    i + 1,
                    line,
                    DDL_AUTO,
                    "HIGH",
                    "Hibernate schema creation setting may be unsafe in production."
            );

            Matcher secretMatcher = SECRET.matcher(line);

            if (secretMatcher.find()) {
                String keyName = secretMatcher.group(1);

                findings.add(new ConfigFinding(
                        fileName,
                        i + 1,
                        "HIGH",
                        "Potential hardcoded secret found in key: "
                                + keyName + ". Move it to a secure secret store.",
                        keyName
                ));
            }

            addIfMatched(
                    findings, fileName, i + 1, line,
                    SHOW_SQL, "WARNING",
                    "SQL logging is enabled; review production logging."
            );

            addIfMatched(
                    findings, fileName, i + 1, line,
                    ACTUATOR_EXPOSURE, "HIGH",
                    "All web actuator endpoints may be exposed."
            );

            addIfMatched(
                    findings, fileName, i + 1, line,
                    STACKTRACE_ALWAYS, "HIGH",
                    "Stack traces may be exposed in error responses."
            );

            addIfMatched(
                    findings, fileName, i + 1, line,
                    ERROR_MESSAGE_ALWAYS, "WARNING",
                    "Detailed error messages may be exposed."
            );


        }

        return List.copyOf(findings);
    }

    private void findMatches(
            List<ConfigFinding> findings,
            String fileName,
            int lineNumber,
            String line,
            Pattern pattern,
            String severity,
            String message
    ) {
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            findings.add(new ConfigFinding(
                    fileName,
                    lineNumber,
                    severity,
                    message,
                    matcher.group()
            ));
        }
    }

    private void addIfMatched(
            List<ConfigFinding> findings,
            String fileName,
            int lineNumber,
            String line,
            Pattern pattern,
            String severity,
            String message
    ) {
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            findings.add(new ConfigFinding(
                    fileName,
                    lineNumber,
                    severity,
                    message,
                    matcher.group().trim()
            ));
        }
    }

}