package org.intellij.stan;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class StanParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(StanLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new StanLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new StanParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.create(StanTokenTypes.LINE_COMMENT, StanTokenTypes.BLOCK_COMMENT);
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.create(StanTokenTypes.STRING_LITERAL);
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return new ASTWrapperPsiElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new PsiFileBase(viewProvider, StanLanguage.INSTANCE) {
            @Override
            public @NotNull FileType getFileType() {
                return StanFileType.INSTANCE;
            }
        };
    }
}
