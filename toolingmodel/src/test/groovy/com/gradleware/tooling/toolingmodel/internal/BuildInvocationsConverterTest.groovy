package com.gradleware.tooling.toolingmodel.internal

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSortedSet
import com.google.common.collect.Ordering
import org.gradle.tooling.model.GradleTask
import org.gradle.tooling.model.TaskSelector
import org.gradle.tooling.model.gradle.BuildInvocations
import spock.lang.Specification

class BuildInvocationsConverterTest extends Specification {

  @SuppressWarnings("GroovyAssignabilityCheck")
  def "conversionInBothDirections"() {
    setup:
    def mock = Mock(GradleTask.class)
    mock.getPath() >> ':name'

    def tasks = ImmutableSortedSet.<GradleTask> orderedBy(Ordering.allEqual()).add(SyntheticGradleTask.from(mock)).build()
    def selectors = ImmutableSortedSet.<TaskSelector> orderedBy(Ordering.allEqual()).add(SyntheticTaskSelector.from('name', 'displayName', 'description', true, ImmutableSet.of())).build()
    Map<String, BuildInvocations> mapping = ImmutableMap.of(':', SyntheticBuildInvocations.from(tasks, selectors))

    when:
    def result = BuildInvocationsConverter.INSTANCE.doBackward(BuildInvocationsConverter.INSTANCE.doForward(mapping))

    then:
    result == mapping
  }

}

