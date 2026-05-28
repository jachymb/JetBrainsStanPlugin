package org.intellij.stan;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class StanTokenType extends IElementType {
    public StanTokenType(@NotNull @NonNls String debugName) {
        super(debugName, StanLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "StanTokenType." + super.toString();
    }
}
