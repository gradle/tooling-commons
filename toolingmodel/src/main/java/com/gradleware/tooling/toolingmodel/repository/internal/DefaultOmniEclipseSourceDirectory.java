package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.EclipseSourceDirectoryFields;
import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import com.gradleware.tooling.toolingmodel.generic.DefaultModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
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

    public static DefaultOmniEclipseSourceDirectory from(Model<EclipseSourceDirectoryFields> input) {
        return new DefaultOmniEclipseSourceDirectory(input.get(EclipseSourceDirectoryFields.PATH));
    }

    public static Model<EclipseSourceDirectoryFields> from(EclipseSourceDirectory input) {
        DefaultModel<EclipseSourceDirectoryFields> sourceDirectory = new DefaultModel<EclipseSourceDirectoryFields>();
        sourceDirectory.put(EclipseSourceDirectoryFields.PATH, input.getPath());
        return sourceDirectory;
    }

}
