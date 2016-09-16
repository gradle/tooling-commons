/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingclient.internal;

import java.io.File;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.GradleDistribution;

/**
 * Base class for all {@link InspectableBuildRequest}s.
 *
 * @param <T> the result type
 * @param <SELF> self reference
 *
 * @author Stefan Oehme
 */
abstract class BaseBuildRequest<T, SELF extends BaseBuildRequest<T, SELF>> extends BaseRequest<T, SELF> implements InspectableBuildRequest<T> {

    private File projectDir;
    private File gradleUserHomeDir;
    private GradleDistribution gradleDistribution;

    BaseBuildRequest(ExecutableToolingClient toolingClient) {
        super(toolingClient);
        this.gradleDistribution = GradleDistribution.fromBuild();
    }

    @Override
    public SELF projectDir(File projectDir) {
        this.projectDir = projectDir;
        return getThis();
    }

    @Override
    public File getProjectDir() {
        return this.projectDir;
    }

    @Override
    public SELF gradleUserHomeDir(File gradleUserHomeDir) {
        this.gradleUserHomeDir = gradleUserHomeDir;
        return getThis();
    }

    @Override
    public File getGradleUserHomeDir() {
        return this.gradleUserHomeDir;
    }

    @Override
    public SELF gradleDistribution(GradleDistribution gradleDistribution) {
        this.gradleDistribution = Preconditions.checkNotNull(gradleDistribution);
        return getThis();
    }

    @Override
    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    @Override
    <S, S_SELF extends BaseRequest<S, S_SELF>> S_SELF copy(BaseRequest<S, S_SELF> request) {
        S_SELF copy = super.copy(request);
        if (copy instanceof BaseBuildRequest) {
            @SuppressWarnings("rawtypes")
            BaseBuildRequest simpleRequest = (BaseBuildRequest) request;
            simpleRequest.projectDir(getProjectDir())
                .gradleUserHomeDir(getGradleUserHomeDir())
                .gradleDistribution(getGradleDistribution());
        }
        return copy;
    }

}
