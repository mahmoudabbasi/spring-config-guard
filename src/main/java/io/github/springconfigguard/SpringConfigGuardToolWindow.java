package io.github.springconfigguard;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.List;

public class SpringConfigGuardToolWindow implements ToolWindowFactory {

    private static final String TITLE = "Spring Config Guard";

    @Override
    public void createToolWindowContent(
            @NotNull Project project,
            @NotNull ToolWindow toolWindow
    ) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(8));

        DefaultListModel<ConfigFinding> model = new DefaultListModel<>();
        JBList<ConfigFinding> list = new JBList<>(model);

        list.setCellRenderer((items, finding, index, selected, focused) -> {
            JLabel label = new JLabel(
                    "[" + finding.severity() + "] "
                            + finding.fileName() + ":"
                            + finding.lineNumber() + " — "
                            + finding.message()
            );

            label.setBorder(JBUI.Borders.empty(6, 8));
            label.setOpaque(true);

            if (selected) {
                label.setBackground(items.getSelectionBackground());
                label.setForeground(items.getSelectionForeground());
            } else {
                label.setBackground(items.getBackground());

                switch (finding.severity().toUpperCase(java.util.Locale.ROOT)) {
                    case "HIGH" -> label.setForeground(
                            new java.awt.Color(220, 53, 69)
                    );
                    case "WARNING" -> label.setForeground(
                            new java.awt.Color(230, 126, 34)
                    );
                    default -> label.setForeground(
                            items.getForeground()
                    );
                }
            }

            return label;
        });


        list.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }

            ConfigFinding finding = list.getSelectedValue();

            if (finding == null) {
                return;
            }

            panel.setToolTipText(
                    finding.message() + " | Matched: " + finding.matchedText()
            );
        });

        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() != 2) {
                    return;
                }

                ConfigFinding finding = list.getSelectedValue();

                if (finding == null) {
                    return;
                }

                File baseDir = new File(project.getBasePath());
                File target = new File(baseDir, finding.fileName());

                VirtualFile virtualFile =
                        LocalFileSystem.getInstance()
                                .refreshAndFindFileByIoFile(target);

                if (virtualFile == null) {
                    return;
                }

                FileEditorManager.getInstance(project)
                        .openFile(virtualFile, true);

                Editor editor = FileEditorManager.getInstance(project)
                        .getSelectedTextEditor();

                if (editor != null) {
                    int line = Math.max(
                            0,
                            Math.min(
                                    finding.lineNumber() - 1,
                                    editor.getDocument().getLineCount() - 1
                            )
                    );

                    editor.getCaretModel().moveToLogicalPosition(
                            new com.intellij.openapi.editor.LogicalPosition(line, 0)
                    );

                    editor.getScrollingModel()
                            .scrollToCaret(ScrollType.CENTER);
                }
            }
        });

        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JLabel hint = new JLabel(
                "Double-click a finding to open its source line."
        );
        hint.setBorder(JBUI.Borders.emptyTop(8));
        panel.add(hint, BorderLayout.SOUTH);

        Content content = ContentFactory.getInstance()
                .createContent(panel, "", false);

        toolWindow.getContentManager().addContent(content);

        // Keep the list available to the analyzer action.
        project.putUserData(
                SpringConfigGuardKeys.FINDINGS_MODEL,
                model
        );
    }

    public static void showFindings(
            @NotNull Project project,
            @NotNull List<ConfigFinding> findings
    ) {
        DefaultListModel<ConfigFinding> model =
                project.getUserData(SpringConfigGuardKeys.FINDINGS_MODEL);

        if (model == null) {
            return;
        }

        model.clear();

        for (ConfigFinding finding : findings) {
            model.addElement(finding);
        }
    }
}