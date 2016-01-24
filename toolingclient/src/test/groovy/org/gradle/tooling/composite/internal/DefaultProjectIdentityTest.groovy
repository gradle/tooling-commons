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
package org.gradle.tooling.composite.internal

import org.gradle.tooling.composite.ProjectIdentity
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class DefaultProjectIdentityTest extends Specification {

    def "is instantiated with non-null constructor parameter values"() {
        given:
        File projectDir = new File('/dev/myProject')
        Set<ProjectIdentity> children = [] as Set<ProjectIdentity>

        when:
        ProjectIdentity projectIdentity = new DefaultProjectIdentity(projectDir, children)

        then:
        projectIdentity.projectDirectory == projectDir
        projectIdentity.children == children
    }

    @Ignore("Assertions will be enabled in Gradle core")
    @Unroll
    def "is instantiated with null '#parameter' constructor parameter value"() {
        when:
        new DefaultProjectIdentity(projectDir, children)

        then:
        Throwable t = thrown(AssertionError)
        t.message == message

        where:
        parameter    | projectDir                 | children                   | message
        'projectDir' | null                       | [] as Set<ProjectIdentity> | 'project directory cannot be null'
        'children'   | new File('/dev/myProject') | null                       | 'children cannot be null'
    }

    @Unroll
    def "can compare with other instance (#projectDir)"() {
        expect:
        ProjectIdentity projectIdentity1 = new DefaultProjectIdentity(new File('/dev/myProject'), [] as Set<ProjectIdentity>)
        ProjectIdentity projectIdentity2 = new DefaultProjectIdentity(projectDir, [] as Set<ProjectIdentity>)
        (projectIdentity1 == projectIdentity2) == equality
        (projectIdentity1.hashCode() == projectIdentity2.hashCode()) == hashCode
        (projectIdentity1.toString() == projectIdentity2.toString()) == stringRepresentation

        where:
        projectDir                 | equality | hashCode | stringRepresentation
        new File('/dev/myProject') | true     | true     | true
        new File('/dev/other')     | false    | false    | false
    }

    def "can build hierarchy"() {
        when:
        ProjectIdentity child1 = new DefaultProjectIdentity(new File('/dev/root/child1'), [] as Set<ProjectIdentity>)
        ProjectIdentity child2 = new DefaultProjectIdentity(new File('/dev/root/child2'), [] as Set<ProjectIdentity>)
        ProjectIdentity root = new DefaultProjectIdentity(new File('/dev/root'), [child1, child2] as Set<ProjectIdentity>)
        child1.parent = root
        child2.parent = root

        then:
        root.children == [child1, child2] as Set<ProjectIdentity>
        child1.parent == root
        child2.parent == root
    }
}
