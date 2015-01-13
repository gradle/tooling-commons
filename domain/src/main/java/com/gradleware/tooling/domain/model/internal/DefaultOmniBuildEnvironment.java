package com.gradleware.tooling.domain.model.internal;

import com.gradleware.tooling.domain.model.GradleEnvironmentFields;
import com.gradleware.tooling.domain.model.JavaEnvironmentFields;
import com.gradleware.tooling.domain.model.OmniBuildEnvironment;
import com.gradleware.tooling.domain.model.OmniGradleEnvironment;
import com.gradleware.tooling.domain.model.OmniJavaEnvironment;
import com.gradleware.tooling.domain.generic.DefaultModel;
import com.gradleware.tooling.domain.generic.Model;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.build.GradleEnvironment;
import org.gradle.tooling.model.build.JavaEnvironment;

import java.io.File;
import java.util.List;

/**
 * Default implementation of the {@link OmniBuildEnvironment} interface.
 */
public final class DefaultOmniBuildEnvironment implements OmniBuildEnvironment {

    private final Model<GradleEnvironmentFields> gradleModel;
    private final Model<JavaEnvironmentFields> javaModel;
    private final OmniGradleEnvironment gradle;
    private final OmniJavaEnvironment java;

    private DefaultOmniBuildEnvironment(Model<GradleEnvironmentFields> gradleModel, Model<JavaEnvironmentFields> javaModel, OmniGradleEnvironment gradle, OmniJavaEnvironment java) {
        this.gradleModel = gradleModel;
        this.javaModel = javaModel;
        this.gradle = gradle;
        this.java = java;
    }

    @Override
    public OmniGradleEnvironment getGradle() {
        return this.gradle;
    }

    @Override
    public Model<GradleEnvironmentFields> getGradleModel() {
        return this.gradleModel;
    }

    @Override
    public OmniJavaEnvironment getJava() {
        return this.java;
    }

    @Override
    public Model<JavaEnvironmentFields> getJavaModel() {
        return this.javaModel;
    }

    public static DefaultOmniBuildEnvironment from(BuildEnvironment buildEnvironment) {
        final DefaultModel<GradleEnvironmentFields> gradleModel = new DefaultModel<GradleEnvironmentFields>();
        GradleEnvironment gradleOrigin = buildEnvironment.getGradle();
        gradleModel.put(GradleEnvironmentFields.GRADLE_VERSION, gradleOrigin.getGradleVersion());

        final DefaultModel<JavaEnvironmentFields> javaModel = new DefaultModel<JavaEnvironmentFields>();
        JavaEnvironment javaOrigin = buildEnvironment.getJava();
        javaModel.put(JavaEnvironmentFields.JAVA_HOME, javaOrigin.getJavaHome());
        javaModel.put(JavaEnvironmentFields.JVM_ARGS, javaOrigin.getJvmArguments());

        OmniGradleEnvironment gradle = new OmniGradleEnvironment() {
            @Override
            public String getGradleVersion() {
                return gradleModel.get(GradleEnvironmentFields.GRADLE_VERSION);
            }
        };

        OmniJavaEnvironment java = new OmniJavaEnvironment() {
            @Override
            public File getJavaHome() {
                return javaModel.get(JavaEnvironmentFields.JAVA_HOME);
            }

            @Override
            public List<String> getJvmArguments() {
                return javaModel.get(JavaEnvironmentFields.JVM_ARGS);
            }
        };

        return new DefaultOmniBuildEnvironment(gradleModel, javaModel, gradle, java);
    }

}
