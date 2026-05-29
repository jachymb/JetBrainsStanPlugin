package org.intellij.stan.psi;

import com.intellij.psi.tree.IElementType;
import org.intellij.stan.StanLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class StanElementType extends IElementType {
    public StanElementType(@NotNull @NonNls String debugName) {
        super(debugName, StanLanguage.INSTANCE);
    }
}
