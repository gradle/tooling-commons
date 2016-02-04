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

import com.gradleware.tooling.junit.TestDirectoryProvider
import org.gradle.tooling.composite.fixtures.ExternalDependency
import org.junit.Rule
import spock.lang.Specification

abstract class AbstractCompositeBuildConnectorIntegrationTest extends Specification {

    @Rule
    TestDirectoryProvider directoryProvider = new TestDirectoryProvider()

    protected CompositeBuildConnection createComposite(File... rootProjectDirectories) {
        createComposite(rootProjectDirectories as List<File>)
    }

    protected CompositeBuildConnection createComposite(List<File> rootProjectDirectories) {
        CompositeBuildConnector compositeBuildConnector = CompositeBuildConnector.newComposite()

        rootProjectDirectories.each {
            compositeBuildConnector.addParticipant(it)
        }

        compositeBuildConnector.connect()
    }

    protected void withCompositeConnection(List<File> rootProjectDirectories, Closure c) {
        CompositeBuildConnection connection

        try {
            connection = createComposite(rootProjectDirectories)
            c(connection)
        } finally {
            connection?.close()
        }
    }

    protected File createBuildFileWithExternalDependency(File projectDir, ExternalDependency externalDependency) {
        File buildFile = createBuildFile(projectDir)
        buildFile << javaBuildScript()
        buildFile << """
            dependencies {
                compile '${externalDependency.toString()}'
            }
        """
        buildFile
    }

    protected File createBuildFile(File projectDir) {
        createDir(projectDir)
        File buildFile = new File(projectDir, 'build.gradle')
        createFile(buildFile)
        buildFile
    }

    protected String javaBuildScript() {
        """
            apply plugin: 'java'
            repositories {
                mavenCentral()
            }
        """
    }

    protected File createSettingsFile(File projectDir, List<String> projectPaths) {
        File settingsFile = new File(projectDir, 'settings.gradle')
        createFile(settingsFile)
        String includes = projectPaths.collect { "'$it'" }.join(', ')
        settingsFile << "include $includes"
        settingsFile
    }

    private void createDir(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Failed to create directory $dir")
        }
    }

    private void createFile(File file) {
        if (!file.exists() && !file.createNewFile()) {
            throw new IllegalStateException("Failed to create file $file")
        }
    }
}
