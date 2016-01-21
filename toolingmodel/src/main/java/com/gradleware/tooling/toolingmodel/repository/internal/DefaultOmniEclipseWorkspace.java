package com.gradleware.tooling.toolingmodel.repository.internal;

import java.util.List;

import org.gradle.api.specs.Spec;

import com.google.common.base.Optional;

import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;

public class DefaultOmniEclipseWorkspace implements OmniEclipseWorkspace {

    private OmniEclipseProject rootProject;

    private DefaultOmniEclipseWorkspace(OmniEclipseProject rootProject) {
        this.rootProject = rootProject;
    }

    @Override
    public List<OmniEclipseProject> getOpenEclipseProjects() {
        return this.rootProject.getAll();
    }

    @Override
    public Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate) {
        return this.rootProject.tryFind(predicate);
    }

    @Override
    public List<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate) {
        return this.rootProject.filter(predicate);
    }

    public static OmniEclipseWorkspace from(OmniEclipseProject rootProject) {
        return new DefaultOmniEclipseWorkspace(rootProject);
    }
}
