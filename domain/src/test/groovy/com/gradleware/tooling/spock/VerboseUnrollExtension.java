/*
 * Copyright 2014 Etienne Studer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.spock;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.extension.builtin.UnrollNameProvider;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.NameProvider;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.TextUtil;

/**
 * Implementation of the {@link VerboseUnroll} extension. The code of this class is very similar to the code of {@link org.spockframework.runtime.extension.builtin.UnrollExtension}
 * but differs in the {@link #chooseNameProvider} logic.
 */
public final class VerboseUnrollExtension extends AbstractAnnotationDrivenExtension<VerboseUnroll> {

    @Override
    public void visitSpecAnnotation(VerboseUnroll unroll, SpecInfo spec) {
        for (FeatureInfo feature : spec.getFeatures()) {
            if (feature.isParameterized()) {
                visitFeatureAnnotation(unroll, feature);
            }
        }
    }

    @Override
    public void visitFeatureAnnotation(VerboseUnroll unroll, FeatureInfo feature) {
        if (!feature.isParameterized()) {
            return; // could also throw exception
        }

        feature.setReportIterations(true);
        feature.setIterationNameProvider(chooseNameProvider(unroll, feature));
    }

    // custom logic that decorates the default name behaviour by post-fixing the data values of the current iteration
    private NameProvider<IterationInfo> chooseNameProvider(VerboseUnroll unroll, final FeatureInfo feature) {
        final NameProvider<IterationInfo> defaultNameProvider = createDefaultUnrollNameProvider(unroll, feature);
        return new NameProvider<IterationInfo>() {
            @Override
            public String getName(IterationInfo iterationInfo) {
                String defaultName = defaultNameProvider != null ? defaultNameProvider.getName(iterationInfo) : feature.getName();
                String joinedDataValues = TextUtil.join(", ", iterationInfo.getDataValues());
                return String.format("%s [%s]", defaultName, joinedDataValues);
            }
        };
    }

    // same logic as in the UnrollExtension code from Spock
    private NameProvider<IterationInfo> createDefaultUnrollNameProvider(VerboseUnroll unroll, FeatureInfo feature) {
        if (unroll.value().length() > 0) {
            return new UnrollNameProvider(feature, unroll.value());
        } else if (feature.getName().contains("#")) {
            return new UnrollNameProvider(feature, feature.getName());
        } else {
            return null;
        }
    }

}
