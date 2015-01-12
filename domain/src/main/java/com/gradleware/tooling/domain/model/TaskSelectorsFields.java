package com.gradleware.tooling.domain.model;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.domain.model.generic.DomainObjectField;
import com.gradleware.tooling.domain.model.generic.TypeTokens;

import java.util.SortedSet;

/**
 * Enumerates the information available on a task selector.
 *
 * @see org.gradle.tooling.model.TaskSelector
 */
public final class TaskSelectorsFields {

    /**
     * The name used to select tasks.
     */
    public static final DomainObjectField<String, TaskSelectorsFields> NAME =
            new DomainObjectField<String, TaskSelectorsFields>(TypeToken.of(String.class), TypeToken.of(TaskSelectorsFields.class));

    /**
     * The description of this task selector, or {@code null} if it has no description.
     */
    public static final DomainObjectField<String, TaskSelectorsFields> DESCRIPTION =
            new DomainObjectField<String, TaskSelectorsFields>(TypeToken.of(String.class), TypeToken.of(TaskSelectorsFields.class));

    /**
     * Flag to signal whether this task selector is public or not. Public task selectors are those that select at least one public project task.
     */
    public static final DomainObjectField<Boolean, TaskSelectorsFields> IS_PUBLIC =
            new DomainObjectField<Boolean, TaskSelectorsFields>(TypeToken.of(Boolean.class), TypeToken.of(TaskSelectorsFields.class), Suppliers.ofInstance(true));

    /**
     * The tasks selected by this task selector, identified by their unique path.
     */
    public static final DomainObjectField<SortedSet<String>, TaskSelectorsFields> SELECTED_TASK_PATHS =
            new DomainObjectField<SortedSet<String>, TaskSelectorsFields>(TypeTokens.sortedSetToken(String.class), TypeToken.of(TaskSelectorsFields.class));

    private TaskSelectorsFields() {
    }

}
