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
import groovy.transform.TupleConstructor
import org.gradle.tooling.model.eclipse.EclipseProject
import org.junit.Rule
import spock.lang.Specification

abstract class AbstractCompositeBuildConnectorIntegrationTest extends Specification {

    @Rule
    TestDirectoryProvider directoryProvider = new TestDirectoryProvider()

    protected CompositeBuildConnection createComposite(File... rootProjectDirectories) {
        CompositeBuildConnector compositeBuildConnector = CompositeBuildConnector.newComposite()

        rootProjectDirectories.each {
            compositeBuildConnector.addParticipant(it)
        }

        compositeBuildConnector.connect()
    }

    protected File createBuildFileWithDependency(File projectDir, ExternalDependency externalDependency) {
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

    protected EclipseProject assertProjectInCompositeModel(Set<ModelResult<EclipseProject>> compositeModel, String projectName) {
        ModelResult<EclipseProject> modelResult = compositeModel.find { it.model.name == projectName }
        assert modelResult
        modelResult.getModel()
    }

    protected void assertExternalDependencies(EclipseProject eclipseProject, ExternalDependency... externalDependencies) {
        assert eclipseProject.classpath.size() == externalDependencies.size()

        externalDependencies.each { externalDependency ->
            assert eclipseProject.classpath.collect { it.gradleModuleVersion }.find {
                it.group == externalDependency.group && it.name == externalDependency.name && it.version == externalDependency.version
            }
        }
    }

    protected void assertNoProjectDependencies(EclipseProject eclipseProject) {
        assert eclipseProject.projectDependencies.size() == 0
    }

    @TupleConstructor
    public static class ExternalDependency {
        final String group
        final String name
        final String version

        String toString() {
            "$group:$name:$version"
        }
    }

    public static class ExternalDependencies {
        public static final ExternalDependency COMMONS_LANG = new ExternalDependency('commons-lang', 'commons-lang', '2.6')
        public static final ExternalDependency LOG4J = new ExternalDependency('log4j', 'log4j', '1.2.17')
        public static final ExternalDependency COMMONS_MATH = new ExternalDependency('commons-math', 'commons-math', '1.2')
        public static final ExternalDependency COMMONS_CODEC = new ExternalDependency('commons-codec', 'commons-codec', '1.10')
    }
}
