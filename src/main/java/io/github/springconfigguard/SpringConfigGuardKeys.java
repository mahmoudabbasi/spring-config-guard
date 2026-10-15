package io.github.springconfigguard;

import com.intellij.openapi.util.Key;

import javax.swing.DefaultListModel;

public final class SpringConfigGuardKeys {

    public static final Key<DefaultListModel<ConfigFinding>> FINDINGS_MODEL =
            Key.create("spring.config.guard.findings.model");

    private SpringConfigGuardKeys() {
    }
}