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
