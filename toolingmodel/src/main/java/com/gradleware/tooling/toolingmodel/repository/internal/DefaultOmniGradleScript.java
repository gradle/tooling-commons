package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.GradleScriptFields;
import com.gradleware.tooling.toolingmodel.OmniGradleScript;
import com.gradleware.tooling.toolingmodel.generic.Model;

import java.io.File;

/**
 * Default implementation of the {@link OmniGradleScript} interface.
 */
public final class DefaultOmniGradleScript implements OmniGradleScript {

    private final File sourceFile;

    public DefaultOmniGradleScript(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public File getSourceFile() {
        return this.sourceFile;
    }

    public static DefaultOmniGradleScript from(Model<GradleScriptFields> gradleScript) {
        return new DefaultOmniGradleScript(gradleScript.get(GradleScriptFields.SOURCE_FILE));
    }

}
