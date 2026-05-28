package org.intellij.stan;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class StanFileType extends LanguageFileType {
    public static final StanFileType INSTANCE = new StanFileType();

    private StanFileType() {
        super(StanLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Stan";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Stan language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "stan";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return StanIcons.FILE;
    }
}
