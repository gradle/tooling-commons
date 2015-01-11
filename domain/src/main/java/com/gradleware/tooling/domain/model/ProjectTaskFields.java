package com.gradleware.tooling.domain.model;

import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.domain.model.generic.DomainObjectField;

/**
 * Enumerates the information available on a Gradle task.
 *
 * @see org.gradle.tooling.model.GradleTask
 */
public final class ProjectTaskFields {

    /**
     * The name of this task. Note that the name is not a unique identifier for the task.
     */
    public static final DomainObjectField<String, ProjectTaskFields> NAME =
            new DomainObjectField<String, ProjectTaskFields>(TypeToken.of(String.class), TypeToken.of(ProjectTaskFields.class));

    /**
     * The description of this task, or {@code null} if it has no description.
     */
    public static final DomainObjectField<String, ProjectTaskFields> DESCRIPTION =
            new DomainObjectField<String, ProjectTaskFields>(TypeToken.of(String.class), TypeToken.of(ProjectTaskFields.class));

    /**
     * The path of this task. The path can be used as a unique identifier for the task within a given build.
     */
    public static final DomainObjectField<String, ProjectTaskFields> PATH =
            new DomainObjectField<String, ProjectTaskFields>(TypeToken.of(String.class), TypeToken.of(ProjectTaskFields.class));

    /**
     * Flag to signal whether this task is public or not. Public tasks are those that have a non-null {@code group} property.
     */
    public static final DomainObjectField<Boolean, ProjectTaskFields> IS_PUBLIC =
            new DomainObjectField<Boolean, ProjectTaskFields>(TypeToken.of(Boolean.class), TypeToken.of(ProjectTaskFields.class));

    private ProjectTaskFields() {
    }

}
