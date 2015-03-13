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

import com.gradleware.tooling.toolingmodel.util.Pair;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;

/**
 * Composite build action to execute two actions at once.
 */
public final class DoubleBuildAction<S, T> implements BuildAction<Pair<S, T>> {

    private static final long serialVersionUID = 1L;

    private final BuildAction<S> first;
    private final BuildAction<T> second;

    DoubleBuildAction(BuildAction<S> first, BuildAction<T> second) {
        this.first = first;
        this.second = second;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<S, T> execute(BuildController controller) {
        return new Pair<S, T>(this.first.execute(controller), this.second.execute(controller));
    }

}
