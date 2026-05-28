package org.intellij.stan;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
        return "/*\n" +
               " * Hierarchical normal model — Stan demo\n" +
               " */\n" +
               "functions {\n" +
               "  // User-defined log-likelihood\n" +
               "  real partial_sum(array[] real y_slice, int start, int end,\n" +
               "                   real mu, real sigma) {\n" +
               "    return normal_lpdf(y_slice | mu, sigma);\n" +
               "  }\n" +
               "}\n" +
               "\n" +
               "data {\n" +
               "  int<lower=1> N;        // number of observations\n" +
               "  int<lower=1> J;        // number of groups\n" +
               "  array[N] real y;       // observations\n" +
               "  array[N] int<lower=1, upper=J> group;\n" +
               "}\n" +
               "\n" +
               "parameters {\n" +
               "  real mu_hyper;\n" +
               "  real<lower=0> sigma_hyper;\n" +
               "  vector[J] mu;\n" +
               "  real<lower=0> sigma;\n" +
               "}\n" +
               "\n" +
               "transformed parameters {\n" +
               "  vector[N] mu_obs = mu[group];\n" +
               "}\n" +
               "\n" +
               "model {\n" +
               "  // Hyper-priors\n" +
               "  mu_hyper ~ normal(0, 10);\n" +
               "  sigma_hyper ~ cauchy(0, 2.5);\n" +
               "\n" +
               "  // Group-level priors\n" +
               "  mu ~ normal(mu_hyper, sigma_hyper);\n" +
               "  sigma ~ cauchy(0, 2.5);\n" +
               "\n" +
               "  // Likelihood via reduce_sum\n" +
               "  target += reduce_sum(partial_sum, y, 1, mu_obs, sigma);\n" +
               "}\n" +
               "\n" +
               "generated quantities {\n" +
               "  real y_rep = normal_rng(mu[1], sigma);\n" +
               "  real log_lik = normal_lpdf(y | mu_obs, sigma);\n" +
               "}\n";
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
