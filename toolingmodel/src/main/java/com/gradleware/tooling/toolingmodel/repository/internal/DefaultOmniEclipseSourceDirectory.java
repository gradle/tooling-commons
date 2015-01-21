package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

/**
 * Default implementation of the {@link OmniEclipseSourceDirectory} interface.
 */
public final class DefaultOmniEclipseSourceDirectory implements OmniEclipseSourceDirectory {

    private final String path;

    private DefaultOmniEclipseSourceDirectory(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public static DefaultOmniEclipseSourceDirectory from(EclipseSourceDirectory sourceDirectory) {
        return new DefaultOmniEclipseSourceDirectory(sourceDirectory.getPath());
    }

}
