package com.gradleware.tooling.domain;

/**
 * Provider of {@code ModelRepository} instances for given sets of {@code FixedRequestAttributes}.
 */
public interface ModelRepositoryProvider {

    /**
     * Returns the {@code ModelRepository} for the given {@code FixedRequestAttributes}. For the same set of request attributes the same model repository instance is returned each
     * time.
     *
     * @param fixedRequestAttributes the request attributes for which to get the model repository
     * @return the model repository
     */
    ModelRepository getModelRepository(FixedRequestAttributes fixedRequestAttributes);

}
