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

package com.gradleware.tooling.junit;

import static org.junit.Assert.assertTrue

final class TestFile extends File {

  @SuppressWarnings("GroovyUnusedDeclaration")
  public TestFile(String path) {
    this(new File(path));
  }

  public TestFile(File file) {
    super(file.absolutePath);
  }

  public TestFile(File file, Object... path) {
    super(join(file, path).absolutePath);
  }

  private static File join(File file, Object[] path) {
    File current = file.absoluteFile;
    for (Object p : path) {
      current = new File(current, p.toString());
    }
    try {
      return current.canonicalFile;
    } catch (IOException e) {
      throw new RuntimeException(String.format("Cannot canonicalize file '%s'.", current), e);
    }
  }

  public TestFile file(Object... path) {
    try {
      return new TestFile(this, path);
    } catch (RuntimeException e) {
      throw new RuntimeException(String.format("Cannot locate file '%s' relative to '%s'.", Arrays.toString(path), this), e);
    }
  }

  public TestFile create(Closure structure) {
    assertTrue(isDirectory() || mkdirs());
    new TestWorkspaceBuilder(this).apply(structure);
    return this;
  }

}
