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

package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;

/**
 * Event that is broadcast when {@code OmniGradleBuild} has been updated.
 */
public final class GradleBuildUpdateEvent {

    private final OmniGradleBuild gradleBuild;

    public GradleBuildUpdateEvent(OmniGradleBuild gradleBuild) {
        this.gradleBuild = Preconditions.checkNotNull(gradleBuild);
    }

    public OmniGradleBuild getGradleBuild() {
        return this.gradleBuild;
    }

}
