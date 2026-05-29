package org.intellij.stan;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/** Shared utilities for Stan syntax checks used by the inspections. */
public final class StanSyntaxUtil {

    // Suffixes that require conditional (bar-notation) call syntax.
    public static final Set<String> CONDITIONING_SUFFIXES = Set.of(
        "_lpdf", "_lupdf", "_lpmf", "_lupmf", "_cdf", "_lcdf", "_lccdf"
    );

    // C++ reserved words that are forbidden as Stan identifiers.
    public static final Set<String> CPP_RESERVED = Set.of(
        "alignas", "alignof", "and", "and_eq", "asm", "auto",
        "bitand", "bitor", "bool", "catch", "char", "char8_t",
        "char16_t", "char32_t", "class", "compl", "concept", "const",
        "consteval", "constexpr", "constinit", "const_cast", "co_await",
        "co_return", "co_yield", "decltype", "default", "delete",
        "do", "double", "dynamic_cast", "enum", "explicit", "export",
        "extern", "false", "float", "friend", "goto", "inline", "long",
        "mutable", "namespace", "new", "noexcept", "not", "not_eq",
        "nullptr", "operator", "or", "or_eq", "private", "protected",
        "public", "register", "reinterpret_cast", "requires", "short",
        "signed", "sizeof", "static", "static_assert", "static_cast",
        "struct", "switch", "template", "this", "thread_local", "throw",
        "true", "try", "typedef", "typeid", "typename", "union",
        "unsigned", "using", "virtual", "void", "volatile", "wchar_t",
        "while", "xor", "xor_eq", "var", "fvar"
    );

    private StanSyntaxUtil() {}

    public static boolean hasConditioningSuffix(String name) {
        if (name == null) return false;
        for (String suffix : CONDITIONING_SUFFIXES) {
            if (name.endsWith(suffix)) return true;
        }
        return false;
    }

    /**
     * Walk to the first leaf (childless) node in the subtree rooted at {@code node}.
     * Useful for finding the raw token inside wrapper rules like {@code ident},
     * {@code decl_identifier}, etc.
     */
    public static @Nullable ASTNode findLeaf(@Nullable ASTNode node) {
        while (node != null && node.getFirstChildNode() != null)
            node = node.getFirstChildNode();
        return node;
    }
}
