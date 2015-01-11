package com.gradleware.tooling.domain.model;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.domain.model.generic.DomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObjectField;
import com.gradleware.tooling.domain.model.generic.EmptyDomainObject;
import com.gradleware.tooling.domain.model.generic.TypeTokens;

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
    public static final DomainObjectField<String, GradleProjectFields> NAME =
            new DomainObjectField<String, GradleProjectFields>(TypeToken.of(String.class), TypeToken.of(GradleProjectFields.class));

    /**
     * The description of this project, or {@code null} if it has no description.
     */
    public static final DomainObjectField<String, GradleProjectFields> DESCRIPTION =
            new DomainObjectField<String, GradleProjectFields>(TypeToken.of(String.class), TypeToken.of(GradleProjectFields.class));

    /**
     * The path of this project. The path can be used as a unique identifier for the project within a given build.
     */
    public static final DomainObjectField<String, GradleProjectFields> PATH =
            new DomainObjectField<String, GradleProjectFields>(TypeToken.of(String.class), TypeToken.of(GradleProjectFields.class));

    /**
     * The project directory of this project.
     */
    public static final DomainObjectField<File, GradleProjectFields> PROJECT_DIRECTORY =
            new DomainObjectField<File, GradleProjectFields>(TypeToken.of(File.class), TypeToken.of(GradleProjectFields.class), Suppliers.<File>ofInstance(null));

    /**
     * The build directory of this project.
     */
    public static final DomainObjectField<File, GradleProjectFields> BUILD_DIRECTORY =
            new DomainObjectField<File, GradleProjectFields>(TypeToken.of(File.class), TypeToken.of(GradleProjectFields.class), Suppliers.<File>ofInstance(null));

    /**
     * The build script of this project.
     */
    public static final DomainObjectField<DomainObject<GradleScriptFields>, GradleProjectFields> BUILD_SCRIPT =
            new DomainObjectField<DomainObject<GradleScriptFields>, GradleProjectFields>(TypeTokens.domainObjectToken(GradleScriptFields.class), TypeToken.of(GradleProjectFields.class), Suppliers.<DomainObject<GradleScriptFields>>ofInstance(new EmptyDomainObject<GradleScriptFields>()));

    /**
     * The tasks of this project.
     */
    public static final DomainObjectField<List<DomainObject<ProjectTaskFields>>, GradleProjectFields> PROJECT_TASKS =
            new DomainObjectField<List<DomainObject<ProjectTaskFields>>, GradleProjectFields>(TypeTokens.domainObjectListToken(ProjectTaskFields.class), TypeToken.of(GradleProjectFields.class));

    private GradleProjectFields() {
    }

}
