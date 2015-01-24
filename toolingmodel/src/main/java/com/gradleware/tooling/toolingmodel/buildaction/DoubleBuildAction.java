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
