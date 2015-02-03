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
