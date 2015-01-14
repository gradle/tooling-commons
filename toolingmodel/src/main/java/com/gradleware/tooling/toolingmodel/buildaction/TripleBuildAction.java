package com.gradleware.tooling.toolingmodel.buildaction;

import com.gradleware.tooling.toolingmodel.util.Triple;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;

/**
 * Composite build action to execute three actions at once.
 *
 * @since 2.3
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
