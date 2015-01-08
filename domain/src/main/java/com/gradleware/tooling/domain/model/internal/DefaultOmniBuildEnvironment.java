package com.gradleware.tooling.domain.model.internal;

import com.gradleware.tooling.domain.model.GradleEnvironmentFields;
import com.gradleware.tooling.domain.model.JavaEnvironmentFields;
import com.gradleware.tooling.domain.model.OmniBuildEnvironment;
import com.gradleware.tooling.domain.model.generic.DefaultDomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObject;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.build.GradleEnvironment;
import org.gradle.tooling.model.build.JavaEnvironment;

/**
 * Default implementation of the {@link OmniBuildEnvironment} interface.
 */
public final class DefaultOmniBuildEnvironment implements OmniBuildEnvironment {

    private final DomainObject<GradleEnvironmentFields> gradle;
    private final DomainObject<JavaEnvironmentFields> java;

    private DefaultOmniBuildEnvironment(DomainObject<GradleEnvironmentFields> gradle, DomainObject<JavaEnvironmentFields> java) {
        this.gradle = gradle;
        this.java = java;
    }

    @Override
    public DomainObject<GradleEnvironmentFields> getGradle() {
        return this.gradle;
    }

    @Override
    public DomainObject<JavaEnvironmentFields> getJava() {
        return this.java;
    }

    public static DefaultOmniBuildEnvironment from(BuildEnvironment buildEnvironment) {
        DefaultDomainObject<GradleEnvironmentFields> gradle = new DefaultDomainObject<GradleEnvironmentFields>();
        GradleEnvironment gradleOrigin = buildEnvironment.getGradle();
        gradle.put(GradleEnvironmentFields.GRADLE_VERSION, gradleOrigin.getGradleVersion());

        DefaultDomainObject<JavaEnvironmentFields> java = new DefaultDomainObject<JavaEnvironmentFields>();
        JavaEnvironment javaOrigin = buildEnvironment.getJava();
        java.put(JavaEnvironmentFields.JAVA_HOME, javaOrigin.getJavaHome());
        java.put(JavaEnvironmentFields.JVM_ARGS, javaOrigin.getJvmArguments());

        return new DefaultOmniBuildEnvironment(gradle, java);
    }

}
