package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

import java.io.File;

/**
 * Default implementation of the {@link OmniEclipseSourceDirectory} interface.
 */
public final class DefaultOmniEclipseSourceDirectory implements OmniEclipseSourceDirectory {

    private final File directory;
    private final String path;

    private DefaultOmniEclipseSourceDirectory(File directory, String path) {
        this.directory = directory;
        this.path = path;
    }

    @Override
    public File getDirectory() {
        return this.directory;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public static DefaultOmniEclipseSourceDirectory from(EclipseSourceDirectory sourceDirectory) {
        return new DefaultOmniEclipseSourceDirectory(sourceDirectory.getDirectory(), sourceDirectory.getPath());
    }

}
