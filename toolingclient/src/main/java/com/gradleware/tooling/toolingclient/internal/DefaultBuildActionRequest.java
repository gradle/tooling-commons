/*
 * Copyright 2015 the original author or authors.
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

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;
import org.gradle.tooling.BuildAction;

/**
 * Internal implementation of the {@link com.gradleware.tooling.toolingclient.BuildActionRequest} API.
 *
 * @param <T> the result type
 * @author Etienne Studer
 */
final class DefaultBuildActionRequest<T> extends BaseRequest<T, DefaultBuildActionRequest<T>> implements InspectableBuildActionRequest<T> {

    private final BuildAction<T> buildAction;

    DefaultBuildActionRequest(ExecutableToolingClient toolingClient, BuildAction<T> buildAction) {
        super(toolingClient);
        this.buildAction = Preconditions.checkNotNull(buildAction);
    }

    @Override
    public BuildAction<T> getBuildAction() {
        return this.buildAction;
    }

    @Override
    public <S> DefaultBuildActionRequest<S> deriveForBuildAction(BuildAction<S> buildAction) {
        return copy(new DefaultBuildActionRequest<S>(getToolingClient(), buildAction));
    }

    @Override
    public T executeAndWait() {
        return getToolingClient().executeAndWait(this);
    }

    @Override
    public LongRunningOperationPromise<T> execute() {
        return getToolingClient().execute(this);
    }

    @Override
    DefaultBuildActionRequest<T> getThis() {
        return this;
    }

}
