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

package com.gradleware.tooling.testing;

import com.google.common.base.Preconditions;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.gradle.util.GradleVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * A specification that matches against Gradle version patterns.
 */
public final class GradleVersionSpec {

    private static final String CURRENT = "current";
    private static final String NOT_CURRENT = "!current";
    private static final String EQUALS = "=";
    private static final String NOT_EQUALS = "!=";
    private static final String GREATER_THAN_OR_EQUALS = ">=";
    private static final String GREATER_THAN = ">";
    private static final String SMALLER_THAN_OR_EQUALS = "<=";
    private static final String SMALLER_THAN = "<";

    private GradleVersionSpec() {
    }

    /**
     * Creates a spec from the given version constraint.
     *
     * @param constraint the version constraint, must not be null
     * @return the spec representing the version constraint, never null
     */
    public static Spec<GradleVersion> toSpec(String constraint) {
        Preconditions.checkNotNull(constraint);

        String trimmed = constraint.trim();

        // exclusive patterns
        if (trimmed.equals(CURRENT)) {
            final GradleVersion current = GradleVersion.current();
            return new Spec<GradleVersion>() {
                @Override
                public boolean isSatisfiedBy(GradleVersion element) {
                    return element.equals(current);
                }
            };
        }
        if (trimmed.equals(NOT_CURRENT)) {
            final GradleVersion current = GradleVersion.current();
            return new Spec<GradleVersion>() {
                @Override
                public boolean isSatisfiedBy(GradleVersion element) {
                    return !element.equals(current);
                }
            };
        }
        if (trimmed.startsWith(EQUALS)) {
            final GradleVersion target = GradleVersion.version(trimmed.substring(1)).getBaseVersion();
            return new Spec<GradleVersion>() {
                @Override
                public boolean isSatisfiedBy(GradleVersion element) {
                    return element.getBaseVersion().equals(target);
                }
            };
        }
        if (trimmed.startsWith(NOT_EQUALS)) {
            final GradleVersion target = GradleVersion.version(trimmed.substring(2)).getBaseVersion();
            return new Spec<GradleVersion>() {
                @Override
                public boolean isSatisfiedBy(GradleVersion element) {
                    return !element.getBaseVersion().equals(target);
                }
            };
        }

        // AND-combined patterns
        List<Spec<GradleVersion>> specs = new ArrayList<Spec<GradleVersion>>();
        String[] patterns = trimmed.split("\\s+");
        for (String value : patterns) {
            if (value.startsWith(NOT_EQUALS)) {
                final GradleVersion version = GradleVersion.version(value.substring(2));
                specs.add(new Spec<GradleVersion>() {
                    @Override
                    public boolean isSatisfiedBy(GradleVersion element) {
                        return !element.getBaseVersion().equals(version);
                    }
                });
            } else if (value.startsWith(GREATER_THAN_OR_EQUALS)) {
                final GradleVersion minVersion = GradleVersion.version(value.substring(2));
                specs.add(new Spec<GradleVersion>() {
                    @Override
                    public boolean isSatisfiedBy(GradleVersion element) {
                        return element.getBaseVersion().compareTo(minVersion) >= 0;
                    }
                });
            } else if (value.startsWith(GREATER_THAN)) {
                final GradleVersion minVersion = GradleVersion.version(value.substring(1));
                specs.add(new Spec<GradleVersion>() {
                    @Override
                    public boolean isSatisfiedBy(GradleVersion element) {
                        return element.getBaseVersion().compareTo(minVersion) > 0;
                    }
                });
            } else if (value.startsWith(SMALLER_THAN_OR_EQUALS)) {
                final GradleVersion maxVersion = GradleVersion.version(value.substring(2));
                specs.add(new Spec<GradleVersion>() {
                    @Override
                    public boolean isSatisfiedBy(GradleVersion element) {
                        return element.getBaseVersion().compareTo(maxVersion) <= 0;
                    }
                });
            } else if (value.startsWith(SMALLER_THAN)) {
                final GradleVersion maxVersion = GradleVersion.version(value.substring(1));
                specs.add(new Spec<GradleVersion>() {
                    @Override
                    public boolean isSatisfiedBy(GradleVersion element) {
                        return element.getBaseVersion().compareTo(maxVersion) < 0;
                    }
                });
            } else {
                throw new RuntimeException(String.format("Unsupported version range '%s' specified in constraint '%s'. Supported formats: '>=nnn' or '<=nnn' or space-separate patterns", value, constraint));
            }
        }

        return Specs.and(specs);
    }

}
