package com.aosinin.intellij.tasks.cas.resource;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class CASTaskBundle {
    private static final @NonNls String BUNDLE = "messages.CASTaskBundle";
    private static final DynamicBundle INSTANCE = new DynamicBundle(CASTaskBundle.class, BUNDLE);

    private CASTaskBundle() {
    }

    public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }
}