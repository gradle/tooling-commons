package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.repository.BuildInvocationsContainer;
import com.gradleware.tooling.toolingmodel.repository.Environment;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.internal.BuildInvocationsContainerFactory;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import com.gradleware.tooling.toolingmodel.util.Pair;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.gradle.util.GradleVersion;

/**
 * Repository for Gradle build models that is aware of the different constraints given by the Environment in which the repository is used and given by the target Gradle version.
 * The actual fetch logic is delegated to the provided target repository.
 */
public final class ContextAwareModelRepository implements ModelRepository {

    private final ModelRepository target;
    private final Environment environment;

    public ContextAwareModelRepository(ModelRepository target, Environment environment) {
        this.target = Preconditions.checkNotNull(target);
        this.environment = Preconditions.checkNotNull(environment);
    }

    @Override
    public void register(Object listener) {
        this.target.register(listener);
    }

    @Override
    public void unregister(Object listener) {
        this.target.unregister(listener);
    }

    @Override
    public BuildEnvironment fetchBuildEnvironmentAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        // natively supported by all Gradle versions >= 1.0
        return this.target.fetchBuildEnvironmentAndWait(transientRequestAttributes, fetchStrategy);
    }

    @Override
    public GradleBuild fetchGradleBuildAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        // supported by all Gradle versions either natively (>=1.8) or through conversion from a GradleProject (<1.8)
        // note that for versions <1.8, the conversion from GradleProject to GradleBuild happens outside of our control and
        // thus that GradleProject instance cannot be cached, this means that if the GradleProject model is later asked for
        // from this model repository, it needs to be fetched again (and then gets cached)
        return this.target.fetchGradleBuildAndWait(transientRequestAttributes, fetchStrategy);
    }

    @Override
    public EclipseProject fetchEclipseProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        // natively supported by all Gradle versions >= 1.0
        return this.target.fetchEclipseProjectAndWait(transientRequestAttributes, fetchStrategy);
    }

    @Override
    public GradleProject fetchGradleProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        // natively supported by all Gradle versions >= 1.0
        return this.target.fetchGradleProjectAndWait(transientRequestAttributes, fetchStrategy);
    }

    @Override
    public BuildInvocationsContainer fetchBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        // natively supported by all Gradle versions >= 1.12, if BuildActions supported in the running environment
        if (supportsBuildInvocations(transientRequestAttributes) && supportsBuildActions(transientRequestAttributes)) {
            return this.target.fetchBuildInvocationsAndWait(transientRequestAttributes, fetchStrategy);
        } else {
            GradleProject rootGradleProject = fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
            return BuildInvocationsContainerFactory.createFrom(rootGradleProject);
        }
    }

    @Override
    public Pair<GradleProject, BuildInvocationsContainer> fetchGradleProjectWithBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        // natively supported by all Gradle versions >= 1.12, if BuildActions supported in the running environment
        if (supportsBuildInvocations(transientRequestAttributes) && supportsBuildActions(transientRequestAttributes)) {
            return this.target.fetchGradleProjectWithBuildInvocationsAndWait(transientRequestAttributes, fetchStrategy);
        } else {
            GradleProject rootGradleProject = fetchGradleProjectAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
            BuildInvocationsContainer buildInvocationsContainer = BuildInvocationsContainerFactory.createFrom(rootGradleProject);
            return new Pair<GradleProject, BuildInvocationsContainer>(rootGradleProject, buildInvocationsContainer);
        }
    }

    private boolean supportsBuildInvocations(TransientRequestAttributes transientRequestAttributes) {
        return targetGradleVersionIsEqualOrHigherThan("1.12", transientRequestAttributes);
    }

    private boolean supportsBuildActions(TransientRequestAttributes transientRequestAttributes) {
        if (this.environment == Environment.ECLIPSE) {
            // in an Eclipse/OSGi environment, the Tooling API supports BuildActions only in Gradle versions >= 2.3
            return targetGradleVersionIsEqualOrHigherThan("2.3", transientRequestAttributes);
        } else {
            // in all other environments, BuildActions are supported as of Gradle version >= 1.8
            return targetGradleVersionIsEqualOrHigherThan("1.8", transientRequestAttributes);
        }
    }

    private boolean targetGradleVersionIsEqualOrHigherThan(String minVersion, TransientRequestAttributes transientRequestAttributes) {
        BuildEnvironment buildEnvironment = fetchBuildEnvironmentAndWait(transientRequestAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
        GradleVersion gradleVersion = GradleVersion.version(buildEnvironment.getGradle().getGradleVersion());
        return gradleVersion.getBaseVersion().compareTo(GradleVersion.version(minVersion)) >= 0;
    }

}
