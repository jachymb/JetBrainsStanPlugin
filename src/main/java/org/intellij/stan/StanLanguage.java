package org.intellij.stan;

import com.intellij.lang.Language;

public class StanLanguage extends Language {
    public static final StanLanguage INSTANCE = new StanLanguage();

    private StanLanguage() {
        super("Stan");
    }
}
