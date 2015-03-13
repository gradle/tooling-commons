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

package com.gradleware.tooling.junit

import static org.junit.Assert.assertTrue

final class TestWorkspaceBuilder {

  def TestFile baseDir

  def TestWorkspaceBuilder(TestFile baseDir) {
    assertTrue(baseDir.isDirectory())
    this.baseDir = baseDir
  }

  def apply(Closure closure) {
    closure.delegate = this
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
  }

  def file(String name) {
    File file = baseDir.file(name)
    file.write('some content')
    file
  }

  @SuppressWarnings(["GrUnresolvedAccess", "GroovyAssignabilityCheck"])
  def methodMissing(String name, Object args) {
    if (args.length == 1 && args[0] instanceof Closure) {
      baseDir.file(name).create(args[0])
    } else {
      throw new MissingMethodException(name, getClass(), args)
    }
  }

}
