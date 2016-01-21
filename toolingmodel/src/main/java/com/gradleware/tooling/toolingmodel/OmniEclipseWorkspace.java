package com.gradleware.tooling.toolingmodel;

import java.util.Set;

import org.gradle.api.specs.Spec;

import com.google.common.base.Optional;

public interface OmniEclipseWorkspace {
    Set<OmniEclipseProject> getOpenEclipseProjects();

    Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate);

    Set<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate);
}
