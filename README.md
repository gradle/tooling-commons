Tooling Commons
===============

# Overview

Tooling Commons is a set of libraries to facilitate building tools on top of [Gradle](http://www.gradle.org). Its development is driven by the requirements that come
out of the _Buildship_ project.


# Building

Naturally, the Tooling Commons libraries are built with Gradle. Gradle provides an innovative [wrapper](http://gradle.org/docs/current/userguide/gradle_wrapper.html) that allows
you to work with a Gradle build without having to manually install Gradle. The wrapper is a batch script on Windows and a shell script on other operating systems.

You should use the wrapper to build the Tooling Commons libraries. Generally, you should use the wrapper for any wrapper-enabled project because it guarantees building with the
Gradle version that the build was intended to use.

To build the entire project, you should run the following in the root of the checkout.

    ./gradlew build

This will compile all the code and run all the tests.


# Compatibility

The Tooling Commons libraries are in an early stage of maturity in terms of their public API. Currently, we do not make
any commitments to providing backward-compatibility. We do strive to keep the APIs as stable as possible, though.


# Support

Ath this point in time, we do not provide official support for the Tooling Commons libraries.


# Feedback and Contributions

Both feedback and contributions are very welcome.


# License

This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

Copyright 2015 the original author or authors.
