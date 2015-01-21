package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniGradleScript;
import org.gradle.tooling.model.gradle.GradleScript;

import java.io.File;

/**
 * Default implementation of the {@link OmniGradleScript} interface.
 */
public final class DefaultOmniGradleScript implements OmniGradleScript {

    private final File sourceFile;

    private DefaultOmniGradleScript(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public File getSourceFile() {
        return this.sourceFile;
    }

    public static DefaultOmniGradleScript from(GradleScript gradleScript) {
        return new DefaultOmniGradleScript(gradleScript.getSourceFile());
    }

}
