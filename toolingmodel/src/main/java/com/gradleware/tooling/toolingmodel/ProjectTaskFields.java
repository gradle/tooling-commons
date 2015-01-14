package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.toolingmodel.generic.DefaultModelField;
import com.gradleware.tooling.toolingmodel.generic.ModelField;

/**
 * Enumerates the information available on a Gradle task.
 *
 * @see org.gradle.tooling.model.GradleTask
 */
public final class ProjectTaskFields {

    /**
     * The name of this task. Note that the name is not a unique identifier for the task.
     */
    public static final ModelField<String, ProjectTaskFields> NAME =
            new DefaultModelField<String, ProjectTaskFields>(TypeToken.of(String.class), TypeToken.of(ProjectTaskFields.class));

    /**
     * The description of this task, or {@code null} if it has no description.
     */
    public static final ModelField<String, ProjectTaskFields> DESCRIPTION =
            new DefaultModelField<String, ProjectTaskFields>(TypeToken.of(String.class), TypeToken.of(ProjectTaskFields.class));

    /**
     * The path of this task. The path can be used as a unique identifier for the task within a given build.
     */
    public static final ModelField<String, ProjectTaskFields> PATH =
            new DefaultModelField<String, ProjectTaskFields>(TypeToken.of(String.class), TypeToken.of(ProjectTaskFields.class));

    /**
     * Flag to signal whether this task is public or not. Public tasks are those that have a non-null {@code group} property.
     */
    public static final ModelField<Boolean, ProjectTaskFields> IS_PUBLIC =
            new DefaultModelField<Boolean, ProjectTaskFields>(TypeToken.of(Boolean.class), TypeToken.of(ProjectTaskFields.class), Suppliers.ofInstance(true));

    private ProjectTaskFields() {
    }

}
