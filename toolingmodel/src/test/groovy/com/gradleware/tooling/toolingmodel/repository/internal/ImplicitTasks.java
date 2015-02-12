package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class ImplicitTasks {

    public static final List<String> ALL = ImmutableList.of(
            "init", "wrapper", "help", "projects", "tasks", "properties", "components", "dependencies", "dependencyInsight", "setupBuild", "model");

    private ImplicitTasks() {
    }

}
