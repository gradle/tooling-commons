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

import java.util.Set;

/**
 * Provider of {@code ModelRepository} instances for given sets of {@code FixedRequestAttributes}.
 *
 * @author Etienne Studer
 */
public interface ModelRepositoryProvider {

    /**
     * Returns the {@code SimpleModelRepository} for the given {@code FixedRequestAttributes}. For the
     * same set of request attributes the same model repository instance is returned each time.
     *
     * @param fixedRequestAttributes the request attributes for which to get the model repository
     * @return the model repository
     */
    SingleBuildModelRepository getModelRepository(FixedRequestAttributes fixedRequestAttributes);

    /**
     * Returns the {@code CompositeModelReqpository} for the given set
     * {@code FixedRequestAttributes}. For the same set of request attributes the same model
     * repository instance is returned each time.
     *
     * @param fixedRequestAttributes the list of request attributes for which to get the composite model repository
     * @return the composite model repository
     */
    CompositeBuildModelRepository getCompositeModelRepository(Set<FixedRequestAttributes> fixedRequestAttributes);

}
