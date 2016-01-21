package com.gradleware.tooling.toolingmodel;

import java.util.List;

import org.gradle.api.specs.Spec;

import com.google.common.base.Optional;

public interface OmniEclipseWorkspace {
    List<OmniEclipseProject> getOpenEclipseProjects();

    Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate);

    List<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate);
}
