package com.gradleware.tooling.toolingmodel.repository;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;

/**
 * Event that is broadcast when {@code OmniGradleBuildStructure} has been updated.
 */
public final class NewGradleBuildStructureUpdateEvent {

    private final OmniGradleBuildStructure gradleBuildStructure;

    public NewGradleBuildStructureUpdateEvent(OmniGradleBuildStructure gradleBuildStructure) {
        this.gradleBuildStructure = Preconditions.checkNotNull(gradleBuildStructure);
    }

    public OmniGradleBuildStructure getGradleBuildStructure() {
        return this.gradleBuildStructure;
    }

}
