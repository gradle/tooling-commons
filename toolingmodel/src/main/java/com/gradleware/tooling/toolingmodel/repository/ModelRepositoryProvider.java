package com.gradleware.tooling.toolingmodel.repository;

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
    NewModelRepository getModelRepository(FixedRequestAttributes fixedRequestAttributes);

}
