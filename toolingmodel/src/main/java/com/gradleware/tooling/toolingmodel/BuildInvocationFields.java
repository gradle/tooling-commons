package com.gradleware.tooling.toolingmodel;

import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.toolingmodel.generic.DefaultModelField;
import com.gradleware.tooling.toolingmodel.generic.Model;
import com.gradleware.tooling.toolingmodel.generic.ModelField;
import com.gradleware.tooling.toolingmodel.generic.TypeTokens;

import java.util.List;

/**
 * Enumerates the information available on build invocations.
 *
 * @see org.gradle.tooling.model.gradle.BuildInvocations
 */
public final class BuildInvocationFields {

    /**
     * The project tasks.
     */
    public static final ModelField<List<Model<ProjectTaskFields>>, BuildInvocationFields> PROJECT_TASKS =
            new DefaultModelField<List<Model<ProjectTaskFields>>, BuildInvocationFields>(TypeTokens.modelListToken(ProjectTaskFields.class), TypeToken.of(BuildInvocationFields.class));

    /**
     * The task selectors.
     */
    public static final ModelField<List<Model<TaskSelectorsFields>>, BuildInvocationFields> TASK_SELECTORS =
            new DefaultModelField<List<Model<TaskSelectorsFields>>, BuildInvocationFields>(TypeTokens.modelListToken(TaskSelectorsFields.class), TypeToken.of(BuildInvocationFields.class));

    private BuildInvocationFields() {
    }

}
