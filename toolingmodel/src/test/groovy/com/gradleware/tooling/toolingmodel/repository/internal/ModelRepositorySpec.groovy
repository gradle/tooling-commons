/*
 * Copyright 2016 the original author or authors.
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

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingModelToolingClientSpecification
import com.gradleware.tooling.testing.GradleVersionExtractor
import com.gradleware.tooling.testing.GradleVersionParameterization
import com.gradleware.tooling.toolingclient.GradleDistribution

import org.gradle.util.GradleVersion
import org.junit.Rule

abstract class ModelRepositorySpec extends ToolingModelToolingClientSpecification {

    @Rule
    TestDirectoryProvider directoryProvider = new TestDirectoryProvider("single-project");

    @Rule
    TestDirectoryProvider directoryProviderMultiProjectBuild = new TestDirectoryProvider("multi-project");

    @Rule
    TestDirectoryProvider directoryProviderCompositeBuild = new TestDirectoryProvider("composite-project");

    @Rule
    TestDirectoryProvider directoryProviderErroneousBuildStructure = new TestDirectoryProvider("erroneous-build-structure");

    @Rule
    TestDirectoryProvider directoryProviderErroneousBuildFile = new TestDirectoryProvider("erroneous-build-file");

    def setup() {
        // prepare a Gradle build that has a root project and two child projects, and one gradle child project
        directoryProvider.createFile('settings.gradle') << '''
           rootProject.name = 'my root project'
           include 'sub1'
           include 'sub2'
           include 'sub2:subSub1'
        '''
        directoryProvider.createFile('build.gradle') << '''
            description = 'a sample root project'
            task myTask {}
        '''

        directoryProvider.createDir('sub1')
        directoryProvider.createFile('sub1', 'build.gradle') << '''
            description = 'sub project 1'
            task myFirstTaskOfSub1 {
                description = '1st task of sub1'
                group = 'build'
            }
           task mySecondTaskOfSub1 {
               description = '2nd task of sub1'
           }
        '''

        directoryProvider.createDir('sub2')
        directoryProvider.createFile('sub2', 'build.gradle') << '''
            description = 'sub project 2'
            task myFirstTaskOfSub2 {
               description = '1st task of sub2'
            }
            task mySecondTaskOfSub2 {
               description = '2nd task of sub2'
            }
            task myTask {
               description = 'another task of sub2'
               group = 'build'
            }
        '''

        directoryProvider.createDir('sub2', 'subSub1')
        directoryProvider.createFile('sub2', 'subSub1', 'build.gradle') << '''
            description = 'subSub project 1 of sub project 2'
            task myFirstTaskOfSub2subSub1{
                description = '1st task of sub2:subSub1'
            }
            task mySecondTaskOfSub2subSub1{
                description = '2nd task of sub2:subSub1'
            }
            task myTask {}
        '''

        // prepare a Gradle build with a multi-project structure
        directoryProviderMultiProjectBuild.createFile('settings.gradle') << '''
            rootProject.name = 'root project of multi-project build'
            include 'api'
            include 'impl'
        '''

        directoryProviderMultiProjectBuild.createFile('build.gradle') << '''
            subprojects {
                apply plugin: 'java'
                repositories {
                    mavenCentral()
                }
            }
        '''

        directoryProviderMultiProjectBuild.createDir('api')
        directoryProviderMultiProjectBuild.createFile('api', 'build.gradle') << '''
            dependencies {
                compile 'com.google.guava:guava:18.0'
            }
        '''

        directoryProviderMultiProjectBuild.createDir('impl')
        directoryProviderMultiProjectBuild.createFile('impl', 'build.gradle') << '''
            dependencies {
                compile project(':api')
                compile 'log4j:log4j:1.2.17'
            }
        '''

        directoryProviderCompositeBuild.createFile('settings.gradle') << '''
            rootProject.name='root'
            includeBuild 'included1'
            includeBuild 'included2'
        '''
        directoryProviderCompositeBuild.createDir('included1', 'sub1')
        directoryProviderCompositeBuild.createDir('included1', 'sub2')
        directoryProviderCompositeBuild.createDir('included2', 'sub1')
        directoryProviderCompositeBuild.createDir('included2', 'sub2')
        directoryProviderCompositeBuild.createFile('included1', 'settings.gradle') << '''
            rootProject.name = 'included1'
            include 'sub1', 'sub2'
        '''
        directoryProviderCompositeBuild.createFile('included2', 'settings.gradle') << '''
            rootProject.name = 'included2'
            include 'sub1', 'sub2'
        '''

        // prepare a Gradle build that has an erroneous structure
        directoryProviderErroneousBuildStructure.createFile('settings.gradle') << 'include foo'

        // prepare a Gradle build that has a valid structure but an erroneous build script
        directoryProviderErroneousBuildFile.createFile('settings.gradle')
        directoryProviderErroneousBuildFile.createFile('build.gradle') << 'task myTask {'
    }

    protected static boolean higherOrEqual(String minVersion, GradleDistribution distribution) {
        def gradleVersion = GradleVersion.version(extractVersion(distribution))
        gradleVersion.baseVersion.compareTo(GradleVersion.version(minVersion)) >= 0
    }

    @SuppressWarnings(["GroovyAssignabilityCheck", "GroovyAccessibility"])
    private static String extractVersion(GradleDistribution distribution) {
        if (distribution.version) {
            distribution.version
        } else if (distribution.remoteDistributionUri) {
            GradleVersionExtractor.getVersion(distribution.remoteDistributionUri).get()
        } else {
            throw new IllegalStateException("Cannot extract version from distribution: " + distribution)
        }
    }

    protected static List<GradleDistribution> gradleDistributionRange(String versionPattern) {
        GradleVersionParameterization.Default.INSTANCE.getGradleDistributions(versionPattern)
    }

}
