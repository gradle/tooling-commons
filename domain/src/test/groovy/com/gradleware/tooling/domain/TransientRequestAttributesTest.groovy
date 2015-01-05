package com.gradleware.tooling.domain

import com.google.common.collect.ImmutableList
import com.gradleware.tooling.toolingapi.BuildActionRequest
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import spock.lang.Specification

class TransientRequestAttributesTest extends Specification {

  def "apply"() {
    setup:
    BuildActionRequest<?> buildActionRequest = Mock(BuildActionRequest.class)
    def colorOutput = true
    def output = Mock(OutputStream)
    def error = Mock(OutputStream)
    def input = Mock(InputStream)
    def progressListeners = ImmutableList.of(Mock(ProgressListener))
    def cancellationToken = GradleConnector.newCancellationTokenSource().token()
    def requestAttributes = new TransientRequestAttributes(colorOutput, output, error, input, progressListeners, cancellationToken)

    when:
    requestAttributes.apply(buildActionRequest)

    then:
    1 * buildActionRequest.colorOutput(colorOutput)
    1 * buildActionRequest.standardOutput(output)
    1 * buildActionRequest.standardError(error)
    1 * buildActionRequest.standardInput(input)
    1 * buildActionRequest.progressListeners(progressListeners.toArray(new ProgressListener[0]))
    1 * buildActionRequest.cancellationToken(cancellationToken)
  }

  def "equalsAndHashCode"() {
    setup:
    def requestAttributes = new TransientRequestAttributes(true, Mock(OutputStream), Mock(OutputStream), Mock(InputStream), ImmutableList.of(Mock(ProgressListener)), GradleConnector.newCancellationTokenSource().token())
    assert requestAttributes == requestAttributes
    assert requestAttributes.hashCode() == requestAttributes.hashCode()
  }

}
