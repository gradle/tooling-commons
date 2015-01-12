package com.gradleware.tooling.domain.model.internal;

import com.gradleware.tooling.domain.model.GradleEnvironmentFields;
import com.gradleware.tooling.domain.model.JavaEnvironmentFields;
import com.gradleware.tooling.domain.model.OmniBuildEnvironment;
import com.gradleware.tooling.domain.model.OmniGradleEnvironment;
import com.gradleware.tooling.domain.model.OmniJavaEnvironment;
import com.gradleware.tooling.domain.model.generic.DefaultDomainObject;
import com.gradleware.tooling.domain.model.generic.DomainObject;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.build.GradleEnvironment;
import org.gradle.tooling.model.build.JavaEnvironment;

import java.io.File;
import java.util.List;

/**
 * Default implementation of the {@link OmniBuildEnvironment} interface.
 */
public final class DefaultOmniBuildEnvironment implements OmniBuildEnvironment {

    private final DomainObject<GradleEnvironmentFields> gradleModel;
    private final DomainObject<JavaEnvironmentFields> javaModel;
    private final OmniGradleEnvironment gradle;
    private final OmniJavaEnvironment java;

    private DefaultOmniBuildEnvironment(DomainObject<GradleEnvironmentFields> gradleModel, DomainObject<JavaEnvironmentFields> javaModel) {
        this.gradleModel = gradleModel;
        this.javaModel = javaModel;
        this.gradle = new OmniGradleEnvironment() {
            @Override
            public String getGradleVersion() {
                return getGradleModel().get(GradleEnvironmentFields.GRADLE_VERSION);
            }
        };
        this.java = new OmniJavaEnvironment() {
            @Override
            public File getJavaHome() {
                return getJavaModel().get(JavaEnvironmentFields.JAVA_HOME);
            }

            @Override
            public List<String> getJvmArguments() {
                return getJavaModel().get(JavaEnvironmentFields.JVM_ARGS);
            }
        };
    }

    @Override
    public OmniGradleEnvironment getGradle() {
        return this.gradle;
    }

    @Override
    public DomainObject<GradleEnvironmentFields> getGradleModel() {
        return this.gradleModel;
    }

    @Override
    public OmniJavaEnvironment getJava() {
        return this.java;
    }

    @Override
    public DomainObject<JavaEnvironmentFields> getJavaModel() {
        return this.javaModel;
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
