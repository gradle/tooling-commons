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

package com.gradleware.tooling.toolingmodel.buildaction;

import com.gradleware.tooling.toolingmodel.util.Triple;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;

/**
 * Composite build action to execute three actions at once.
 */
public final class TripleBuildAction<S, T, U> implements BuildAction<Triple<S, T, U>> {

    private static final long serialVersionUID = 1L;

    private final BuildAction<S> first;
    private final BuildAction<T> second;
    private final BuildAction<U> third;

    TripleBuildAction(BuildAction<S> first, BuildAction<T> second, BuildAction<U> third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Triple<S, T, U> execute(BuildController controller) {
        return new Triple<S, T, U>(this.first.execute(controller), this.second.execute(controller), this.third.execute(controller));
    }

}
