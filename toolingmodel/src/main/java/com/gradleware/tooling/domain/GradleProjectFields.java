package com.gradleware.tooling.domain;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.domain.generic.DefaultModelField;
import com.gradleware.tooling.domain.generic.Model;
import com.gradleware.tooling.domain.generic.ModelField;
import com.gradleware.tooling.domain.generic.EmptyModel;
import com.gradleware.tooling.domain.generic.TypeTokens;

import java.io.File;
import java.util.List;

/**
 * Enumerates the information available on a Gradle project without having to parse its build script.
 *
 * @see org.gradle.tooling.model.gradle.BasicGradleProject
 */
public final class GradleProjectFields {

    /**
     * The name of this project. Note that the name is not a unique identifier for the project.
     */
    public static final ModelField<String, GradleProjectFields> NAME =
            new DefaultModelField<String, GradleProjectFields>(TypeToken.of(String.class), TypeToken.of(GradleProjectFields.class));

    /**
     * The description of this project, or {@code null} if it has no description.
     */
    public static final ModelField<String, GradleProjectFields> DESCRIPTION =
            new DefaultModelField<String, GradleProjectFields>(TypeToken.of(String.class), TypeToken.of(GradleProjectFields.class));

    /**
     * The path of this project. The path can be used as a unique identifier for the project within a given build.
     */
    public static final ModelField<String, GradleProjectFields> PATH =
            new DefaultModelField<String, GradleProjectFields>(TypeToken.of(String.class), TypeToken.of(GradleProjectFields.class));

    /**
     * The project directory of this project.
     */
    public static final ModelField<File, GradleProjectFields> PROJECT_DIRECTORY =
            new DefaultModelField<File, GradleProjectFields>(TypeToken.of(File.class), TypeToken.of(GradleProjectFields.class), Suppliers.<File>ofInstance(null));

    /**
     * The build directory of this project.
     */
    public static final ModelField<File, GradleProjectFields> BUILD_DIRECTORY =
            new DefaultModelField<File, GradleProjectFields>(TypeToken.of(File.class), TypeToken.of(GradleProjectFields.class), Suppliers.<File>ofInstance(null));

    /**
     * The build script of this project.
     */
    public static final ModelField<Model<GradleScriptFields>, GradleProjectFields> BUILD_SCRIPT =
            new DefaultModelField<Model<GradleScriptFields>, GradleProjectFields>(TypeTokens.domainObjectToken(GradleScriptFields.class), TypeToken.of(GradleProjectFields.class), Suppliers.<Model<GradleScriptFields>>ofInstance(new EmptyModel<GradleScriptFields>()));

    /**
     * The tasks of this project.
     */
    public static final ModelField<List<Model<ProjectTaskFields>>, GradleProjectFields> PROJECT_TASKS =
            new DefaultModelField<List<Model<ProjectTaskFields>>, GradleProjectFields>(TypeTokens.domainObjectListToken(ProjectTaskFields.class), TypeToken.of(GradleProjectFields.class));

    /**
     * The task selectors of this project.
     */
    public static final ModelField<List<Model<TaskSelectorsFields>>, GradleProjectFields> TASK_SELECTORS =
            new DefaultModelField<List<Model<TaskSelectorsFields>>, GradleProjectFields>(TypeTokens.domainObjectListToken(TaskSelectorsFields.class), TypeToken.of(GradleProjectFields.class));

    private GradleProjectFields() {
    }

}
