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

import com.gradleware.tooling.toolingclient.CompositeBuildRequest;
import com.gradleware.tooling.toolingclient.GradleDistribution;

import java.io.File;

/**
 * Internal base class for all composite requests.
 *
 * @param <T> the result type
 * @param <SELF> self reference
 * @author Stefan Oehme
 */
abstract class BaseCompositeBuildRequest<T, SELF extends BaseCompositeBuildRequest<T, SELF>> extends BaseRequest<T, SELF>implements InspectableCompositeBuildRequest<T> {

    private File gradleUserHomeDir;
    private File projectDir;
    private GradleDistribution gradleDistribution;

    BaseCompositeBuildRequest(ExecutableToolingClient toolingClient) {
        super(toolingClient);
    }

    @Override
    public CompositeBuildRequest<T> gradleUserHomeDir(File gradleUserHomeDir) {
        this.gradleUserHomeDir = gradleUserHomeDir;
        return this;
    }

    @Override
    public CompositeBuildRequest<T> projectDir(File projectDir) {
        this.projectDir = projectDir;
        return this;
    }

    @Override
    public File getProjectDir() {
        return this.projectDir;
    }

    @Override
    public CompositeBuildRequest<T> gradleDistribution(GradleDistribution gradleDistribution) {
        this.gradleDistribution = gradleDistribution;
        return this;
    }

    @Override
    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    @Override
    public File getGradleUserHomeDir() {
        return this.gradleUserHomeDir;
    }

    @Override
    <S, S_SELF extends BaseRequest<S, S_SELF>> S_SELF copy(BaseRequest<S, S_SELF> request) {
        S_SELF copy = super.copy(request);
        if (copy instanceof BaseSingleBuildRequest) {
            @SuppressWarnings("rawtypes")
            BaseCompositeBuildRequest compositeRequest = (BaseCompositeBuildRequest) request;
            compositeRequest.projectDir(this.projectDir);
            compositeRequest.gradleDistribution(this.gradleDistribution);
        }
        return copy;
    }

}
