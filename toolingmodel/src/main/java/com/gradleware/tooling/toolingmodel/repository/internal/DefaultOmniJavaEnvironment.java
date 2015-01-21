package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniJavaEnvironment;
import org.gradle.tooling.model.build.JavaEnvironment;

import java.io.File;
import java.util.List;

/**
 * Default implementation of the {@link OmniJavaEnvironment} interface.
 */
public final class DefaultOmniJavaEnvironment implements OmniJavaEnvironment {

    private final File javaHome;
    private final ImmutableList<String> jvmArguments;

    private DefaultOmniJavaEnvironment(File javaHome, List<String> jvmArguments) {
        this.javaHome = javaHome;
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
    }

    @Override
    public File getJavaHome() {
        return this.javaHome;
    }

    @Override
    public ImmutableList<String> getJvmArguments() {
        return this.jvmArguments;
    }

    public static DefaultOmniJavaEnvironment from(JavaEnvironment javaEnvironment) {
        return new DefaultOmniJavaEnvironment(javaEnvironment.getJavaHome(), javaEnvironment.getJvmArguments());
    }

}
