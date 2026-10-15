package io.github.springconfigguard;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpringConfigAnalyzerTest {

    private final SpringConfigAnalyzer analyzer =
            new SpringConfigAnalyzer();

    @Test
    void shouldDetectLocalhostAndUnsafeDdlAuto() {
        String config = """
                spring:
                  datasource:
                    url: jdbc:postgresql://localhost:5432/app
                  jpa:
                    hibernate:
                      ddl-auto: create-drop
                """;

        List<ConfigFinding> findings =
                analyzer.analyze("application-prod.yml", config);

        assertEquals(2, findings.size());
        assertTrue(findings.stream()
                .anyMatch(f -> f.severity().equals("WARNING")));
        assertTrue(findings.stream()
                .anyMatch(f -> f.severity().equals("HIGH")));
    }

    @Test
    void shouldIgnoreFullLineComments() {
        String config = """
                # url: localhost
                # ddl-auto: create
                spring:
                  application:
                    name: example
                """;

        List<ConfigFinding> findings =
                analyzer.analyze("application-prod.yml", config);

        assertTrue(findings.isEmpty());
    }
}