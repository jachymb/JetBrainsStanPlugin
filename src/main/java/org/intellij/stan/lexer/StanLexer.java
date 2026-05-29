package org.intellij.stan.lexer;

import com.intellij.lexer.FlexAdapter;

public class StanLexer extends FlexAdapter {
    public StanLexer() {
        super(new _StanLexer(null));
    }
}
