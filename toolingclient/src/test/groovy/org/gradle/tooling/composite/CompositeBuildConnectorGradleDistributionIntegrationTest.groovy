/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.tooling.composite

import org.gradle.api.Action
import org.gradle.tooling.composite.fixtures.ExternalDependencies
import org.gradle.tooling.composite.fixtures.ExternalDependency
import org.gradle.tooling.model.eclipse.EclipseProject
import org.gradle.util.DistributionLocator
import org.gradle.util.GradleVersion
import org.gradle.wrapper.GradleUserHomeLookup
import spock.lang.Shared

class CompositeBuildConnectorGradleDistributionIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    public static final String VERSION = '2.10'

    @Shared
    DistributionLocator locator = new DistributionLocator()

    def "can create composite with participating project using different distribution types"(String version, Action<CompositeParticipant> configurer) {
        given:
        File projectDir = directoryProvider.createDir('project')
        ExternalDependency commonsLangDep = ExternalDependencies.COMMONS_LANG
        createBuildFileWithExternalDependency(projectDir, commonsLangDep)

        when:
        CompositeBuildConnection connection
        Set<ModelResult<EclipseProject>> compositeModel

        try {
            CompositeBuildConnector compositeBuildConnector = CompositeBuildConnector.newComposite()
            CompositeParticipant compositeParticipant = compositeBuildConnector.addParticipant(projectDir)
            configurer.execute(compositeParticipant)
            CompositeBuildConnection compositeBuildConnection = compositeBuildConnector.connect()
            compositeModel = compositeBuildConnection.getModels(EclipseProject)
        } finally {
            connection?.close()
        }

        then:
        compositeModel.size() == 1

        where:
        version | configurer
        VERSION | { it.useDistribution(locator.getDistributionFor(GradleVersion.version(VERSION))) }
        VERSION | { it.useGradleVersion(VERSION) }
        VERSION | { it.useInstallation(findGradleDistribution(VERSION)) }
    }

    private File findGradleDistribution(String version) {
        File downloadDir = new File(GradleUserHomeLookup.gradleUserHome(), "wrapper/dists/gradle-$version-bin")
        File[] hashDirs = downloadDir.listFiles()

        if (hashDirs.length == 0) {
            throw new IllegalStateException("Expected Gradle distribution in $downloadDir")
        }

        new File(hashDirs[0], "gradle-$version")
    }
}
