package org.intellij.stan.psi;

import com.intellij.psi.tree.IElementType;
import org.intellij.stan.StanLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class StanTokenType extends IElementType {
    public StanTokenType(@NotNull @NonNls String debugName) {
        super(debugName, StanLanguage.INSTANCE);
    }
}
