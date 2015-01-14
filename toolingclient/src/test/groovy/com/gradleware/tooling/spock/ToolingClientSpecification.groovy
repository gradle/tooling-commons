package com.gradleware.tooling.spock

import com.gradleware.tooling.toolingclient.ToolingClient
import org.gradle.internal.Factory
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification

class ToolingClientSpecification extends Specification {

  public static final String EMBEDDED_MODE_SYSTEM_PROPERTY_NAME = 'com.gradleware.tooling.integtest.embedded';

  private static final Logger LOG = LoggerFactory.getLogger(ToolingClientSpecification.class);

  @Shared
  ToolingClient toolingClient

  def setupSpec() {
    toolingClient = ToolingClient.newClient(new Factory<GradleConnector>() {

      /**
       * Creates a new {@code GradleConnector} instance and configures it to connect to the target Gradle either in daemon mode or in embedded mode. If
       * the system property <i>com.gradleware.tooling.integtest.embedded</i> is not set, the default value <i>true</i> is used, meaning the connector
       * is configured to connect to the target Gradle in embedded mode.
       */
      @Override
      GradleConnector create() {
        def connector = GradleConnector.newConnector()

        // connect to Gradle either through a separate daemon process or embedded in the same JVM
        def embedded = Boolean.valueOf(System.getProperty(EMBEDDED_MODE_SYSTEM_PROPERTY_NAME, Boolean.TRUE.toString()))
        ((DefaultGradleConnector) connector).embedded(embedded)
        LOG.debug("Connection to target Gradle in '{}' mode ", (embedded ? 'embedded' : 'daemon'))

        connector
      }
    })
  }

  def cleanupSpec() {
    toolingClient.stop(ToolingClient.CleanUpStrategy.GRACEFULLY)
  }

}
