package org.intellij.stan;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.application.options.CodeStyle;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class StanCreateFileAction extends DumbAwareAction {

    private static final String TEMPLATE =
            "data {\n\n}\n\nparameters {\n\n}\n\nmodel {\n\n}\n";

    public StanCreateFileAction() {
        super("Stan File", "Create a new Stan model file", StanIcons.FILE);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        IdeView view = e.getData(LangDataKeys.IDE_VIEW);
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(
                project != null && view != null && view.getDirectories().length > 0);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        IdeView view = e.getData(LangDataKeys.IDE_VIEW);
        if (project == null || view == null) return;

        PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null) return;

        String name = Messages.showInputDialog(
                project, "File name:", "New Stan File", StanIcons.FILE);
        if (name == null || name.isBlank()) return;

        if (!name.endsWith(".stan")) name = name + ".stan";
        final String fileName = name;

        // Indent the empty line inside the first block and put the caret there.
        CommonCodeStyleSettings.IndentOptions opts =
                CodeStyle.getSettings(project).getIndentOptions(StanFileType.INSTANCE);
        String indent = opts.USE_TAB_CHARACTER ? "\t" : " ".repeat(opts.INDENT_SIZE);
        int lineStart = TEMPLATE.indexOf('\n', TEMPLATE.indexOf('{')) + 1;
        String content = TEMPLATE.substring(0, lineStart) + indent + TEMPLATE.substring(lineStart);

        WriteCommandAction.runWriteCommandAction(project, "Create Stan File", null, () -> {
            try {
                VirtualFile vFile = dir.getVirtualFile().createChildData(this, fileName);
                vFile.setBinaryContent(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                ApplicationManager.getApplication().invokeLater(() ->
                        new OpenFileDescriptor(project, vFile, lineStart + indent.length()).navigate(true));
            } catch (java.io.IOException ex) {
                Messages.showErrorDialog(project, ex.getMessage(), "Could Not Create File");
            }
        });
    }
}
