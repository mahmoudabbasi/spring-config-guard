package io.github.springconfigguard;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalyzeSpringConfigAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

//        Messages.showInfoMessage(
//                "The action was executed.",
//                "Spring Config Guard Debug"
//        );
//
//        System.out.println("Spring Config Guard: actionPerformed started");
        Project project = event.getProject();

        if (project == null) {
            Messages.showWarningDialog(
                    "Please open a project first.",
                    "Spring Config Guard"
            );
            return;
        }

        VirtualFile baseDir = project.getBaseDir();

        if (baseDir == null) {
            Messages.showWarningDialog(
                    project,
                    "Project base directory was not found.",
                    "Spring Config Guard"
            );
            return;
        }

        List<ConfigFinding> findings = new ArrayList<>();

        SpringConfigAnalyzer analyzer = new SpringConfigAnalyzer();

        VfsUtil.iterateChildrenRecursively(
                baseDir,
                file -> !file.getName().equals(".git")
                        && !file.getName().equals("build")
                        && !file.getName().equals("target")
                        && !file.getName().equals(".gradle"),
                file -> {

                    if (file.isDirectory()) {
                        return true;
                    }

                    String name = file.getName()
                            .toLowerCase(Locale.ROOT);

                    boolean isConfigFile =
                            (name.startsWith("application")
                                    || name.startsWith("bootstrap"))
                                    && (name.endsWith(".properties")
                                    || name.endsWith(".yml")
                                    || name.endsWith(".yaml"));

                    if (!isConfigFile) {
                        return true;
                    }

                    String path = VfsUtilCore.getRelativePath(
                            file,
                            baseDir,
                            '/'
                    );

                    if (path == null) {
                        path = file.getPath();
                    }

                    String normalizedPath =
                            path.toLowerCase(Locale.ROOT);

                    // Analyze production-related configuration files only.
//                    if (!normalizedPath.contains("prod")
//                            && !normalizedPath.contains("production")) {
//                        return true;
//                    }

                    try {
                        String content = new String(
                                file.contentsToByteArray(),
                                StandardCharsets.UTF_8
                        );

                        findings.addAll(
                                analyzer.analyze(path, content)
                        );

                    } catch (IOException exception) {
                        // Skip files that cannot be read.
                    }

                    return true;
                }
        );

        ToolWindow toolWindow =
                ToolWindowManager.getInstance(project)
                        .getToolWindow("Spring Config Guard");

        if (toolWindow == null) {
            Messages.showErrorDialog(
                    project,
                    "Spring Config Guard tool window was not registered. "
                            + "Check plugin.xml configuration.",
                    "Spring Config Guard"
            );
            return;
        }

        toolWindow.show();


        SpringConfigGuardToolWindow.showFindings(
                project,
                findings
        );

        if (findings.isEmpty()) {
            Messages.showInfoMessage(
                    project,
                    "No findings were detected. Check whether production "
                            + "configuration files were scanned.",
                    "Spring Config Guard"
            );
        }

    }
}
