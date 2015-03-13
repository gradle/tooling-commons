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

package com.gradleware.tooling.toolingmodel.repository.internal

import com.gradleware.tooling.spock.DataValueFormatter
import com.gradleware.tooling.testing.GradleVersionExtractor
import com.gradleware.tooling.toolingclient.GradleDistribution

/**
 * Custom formatting for the {@link GradleDistribution} data value used in Spock test parameterization. The main purpose is to have shorter string representations than what is
 * returned by {@link GradleDistribution#toString()}. The main reason being that TeamCity truncates long test names which leads to tests with identical names and a false test
 * count is displayed as a consequence.
 */
public final class GradleDistributionFormatter implements DataValueFormatter {

    @SuppressWarnings("GroovyAccessibility")
    @Override
    public String format(Object input) {
        if (input instanceof GradleDistribution) {
            if (input.localInstallationDir != null) {
                File dir = input.localInstallationDir
                dir.absolutePath.substring(dir.absolutePath.lastIndexOf(File.separatorChar) + 1) // last path segment
            } else if (input.remoteDistributionUri != null) {
                GradleVersionExtractor.getVersion(input.remoteDistributionUri).get()
            } else if (input.version != null) {
                input.version
            } else {
                String.valueOf(input);
            }
        } else {
            String.valueOf(input);
        }
    }

}
