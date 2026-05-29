package org.intellij.stan;

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
import org.intellij.stan.lexer.StanLexer;
import org.intellij.stan.parser.StanParser;
import org.intellij.stan.psi.StanTypes;
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
        return TokenSet.create(StanTypes.LINE_COMMENT, StanTypes.BLOCK_COMMENT);
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.create(StanTypes.STRINGLITERAL);
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return StanTypes.Factory.createElement(node);
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
