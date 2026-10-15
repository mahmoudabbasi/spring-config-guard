package io.github.springconfigguard;

public record ConfigFinding(
        String fileName,
        int lineNumber,
        String severity,
        String message,
        String matchedText
) {
}