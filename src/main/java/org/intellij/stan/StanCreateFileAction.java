package org.intellij.stan;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class StanCreateFileAction extends CreateFileFromTemplateAction {

    public StanCreateFileAction() {
        super("Stan File", "Create a new Stan model file", StanIcons.FILE);
    }

    @Override
    protected void buildDialog(@NotNull Project project,
                               @NotNull PsiDirectory directory,
                               @NotNull CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle("New Stan File")
               .addKind("Stan model", StanIcons.FILE, "Stan File");
    }

    @Override
    protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
        return "Create Stan file " + newName;
    }

    @Override
    protected PsiFile createFileFromTemplate(String name, FileTemplate template, PsiDirectory dir) {
        PsiFile file = super.createFileFromTemplate(name, template, dir);
        if (file != null) {
            Project project = dir.getProject();
            VirtualFile vFile = file.getVirtualFile();
            if (vFile != null) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    Editor editor = FileEditorManager.getInstance(project)
                            .openTextEditor(new OpenFileDescriptor(project, vFile), true);
                    if (editor != null) {
                        String text = editor.getDocument().getText();
                        // Place cursor on the blank line inside the first block (data {<newline>HERE})
                        int brace = text.indexOf('{');
                        if (brace >= 0) {
                            int newline = text.indexOf('\n', brace);
                            int target = (newline >= 0) ? newline + 1 : brace + 1;
                            editor.getCaretModel().moveToOffset(target);
                        }
                    }
                });
            }
        }
        return file;
    }
}
