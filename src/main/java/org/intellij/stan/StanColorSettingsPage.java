package org.intellij.stan;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class StanColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = {
        new AttributesDescriptor("Keyword",                      StanSyntaxHighlighter.KEYWORD),
        new AttributesDescriptor("Block keyword",                StanSyntaxHighlighter.BLOCK_KEYWORD),
        new AttributesDescriptor("Type",                         StanSyntaxHighlighter.TYPE),
        new AttributesDescriptor("Built-in function/distribution", StanSyntaxHighlighter.BUILTIN_FUNCTION),
        new AttributesDescriptor("Number",                       StanSyntaxHighlighter.NUMBER),
        new AttributesDescriptor("String",                       StanSyntaxHighlighter.STRING),
        new AttributesDescriptor("Line comment",                 StanSyntaxHighlighter.LINE_COMMENT),
        new AttributesDescriptor("Block comment",                StanSyntaxHighlighter.BLOCK_COMMENT),
        new AttributesDescriptor("Operator",                     StanSyntaxHighlighter.OPERATOR),
        new AttributesDescriptor("Braces",                       StanSyntaxHighlighter.BRACES),
        new AttributesDescriptor("Brackets",                     StanSyntaxHighlighter.BRACKETS),
        new AttributesDescriptor("Parentheses",                  StanSyntaxHighlighter.PARENTHESES),
        new AttributesDescriptor("Semicolon",                    StanSyntaxHighlighter.SEMICOLON),
        new AttributesDescriptor("Comma",                        StanSyntaxHighlighter.COMMA),
        new AttributesDescriptor("Reserved word",                StanSyntaxHighlighter.RESERVED),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new StanSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        try (InputStream is = getClass().getResourceAsStream("/colorSettingsDemo.stan")) {
            if (is == null) return "";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Stan";
    }
}
