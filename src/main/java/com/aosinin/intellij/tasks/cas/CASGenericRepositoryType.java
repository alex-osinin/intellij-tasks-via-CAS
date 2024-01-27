package com.aosinin.intellij.tasks.cas;

import com.aosinin.intellij.tasks.cas.resource.CASPluginIcons;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.config.TaskRepositoryEditor;
import com.intellij.tasks.impl.BaseRepositoryType;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CASGenericRepositoryType extends BaseRepositoryType<CASGenericRepository> {

    @NotNull
    @Override
    public String getName() {
        return "CASGeneric";
    }

    @Override
    public @NotNull Icon getIcon() {
        return CASPluginIcons.TaskIcon;
    }

    @NotNull
    @Override
    public TaskRepository createRepository() {
        return new CASGenericRepository(this);
    }

    @Override
    public Class<CASGenericRepository> getRepositoryClass() {
        return CASGenericRepository.class;
    }

    @NotNull
    @Override
    public TaskRepositoryEditor createEditor(final CASGenericRepository repository,
                                             final Project project,
                                             final Consumer<? super CASGenericRepository> changeListener) {
        return new CASGenericRepositoryEditor<>(project, repository, changeListener);
    }
}
