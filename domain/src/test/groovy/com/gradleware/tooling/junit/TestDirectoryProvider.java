/*
 * Copyright 2010 the original author or authors.
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
package com.gradleware.tooling.junit;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A JUnit rule which provides a unique temporary folder for the test.
 */
@SuppressWarnings("deprecation")
public final class TestDirectoryProvider implements TestRule, org.junit.rules.MethodRule {

    private static File root;
    private static AtomicInteger testCounter;

    private static final Logger LOG = LoggerFactory.getLogger(TestDirectoryProvider.class);

    private File dir;
    private String prefix;

    static {
        // the space in the root directory name is intentional to ensure our code works with directories that contain spaces
        root = new File("build/tmp/test files");
        testCounter = new AtomicInteger(1);
    }

    @Deprecated
    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        // already calculate the test directory prefix since the method name is known to this method
        String methodName = method.getName() != null ? method.getName() : "unknown-test";
        String className = target != null ? target.getClass().getSimpleName() : String.format("UnknownTestClass-%d", testCounter.getAndIncrement());
        init(methodName, className);

        // create a statement that will execute the given statement and cleans up the created test directory afterwards
        return createStatementWithDirectoryCleanup(base);
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        // already calculate the test directory prefix since the method name is known to this method
        String methodName = description.getMethodName() != null ? description.getMethodName() : "unknown-test";
        String className = description.getTestClass() != null ? description.getTestClass().getSimpleName() : String.format("UnknownTestClass-%d", testCounter.getAndIncrement());
        init(methodName, className);

        // create a statement that will execute the given statement and cleans up the created test directory afterwards
        return createStatementWithDirectoryCleanup(base);
    }

    private void init(String methodName, String className) {
        if (prefix == null) {
            String safeMethodName = methodName.replaceAll("\\s", "_").replace(":", "_").replace('"', '_').replace('\'', '_').replace(File.pathSeparator, "_");
            if (safeMethodName.length() > 64) {
                safeMethodName = safeMethodName.substring(0, 32) + "..." + safeMethodName.substring(safeMethodName.length() - 32);
            }
            prefix = String.format("%s/%s", className, safeMethodName);
            LOG.debug("Using prefix '{}'", prefix);
        }
    }

    private Statement createStatementWithDirectoryCleanup(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
                boolean success = TestDirectoryProvider.FileUtils.deleteRecursive(getTestDirectory());
                if (!success) {
                    throw new RuntimeException(String.format("Cannot delete directory '%s'.", dir));
                }
                // don't delete the directory on failure in order to allow to investigate what went wrong
            }
        };
    }

    public File getTestDirectory() {
        if (dir == null) {
            if (prefix == null) {
                // happens if this method is invoked in a constructor or a @Before method
                // it also happens when using @RunWith(SomeRunner) when the runner does not support rules
                prefix = determinePrefix();
            }
            for (int counter = 1; true; counter++) {
                dir = new File(root, counter == 1 ? prefix : String.format("%s-%d", prefix, counter));
                if (dir.mkdirs()) {
                    break;
                }
            }
            LOG.debug("Using test directory '{}'", dir);
        }
        return dir;
    }

    private String determinePrefix() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().endsWith("Test") || element.getClassName().endsWith("Spec")) {
                return Iterables.getLast(Splitter.on('.').split(element.getClassName())) + "/unknown-test";
            }
        }
        return String.format("UnknownTestClass-%d/unknown-test", testCounter.getAndIncrement());
    }

    public File file(Object... path) {
        File current = getTestDirectory().getAbsoluteFile();
        for (Object p : path) {
            current = new File(current, p.toString());
        }
        try {
            return current.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot canonicalize file '%s'.", current), e);
        }
    }

    public File createFile(Object... path) {
        File file = file(path);
        try {
            boolean success = file.createNewFile();
            if (!success) {
                throw new RuntimeException(String.format("File '%s' already exists'.", dir));
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot create new file '%s'.", file), e);
        }
        return file;
    }

    public File createDir(Object... path) {
        File dir = file(path);
        boolean success = dir.mkdirs();
        if (!success) {
            throw new RuntimeException(String.format("Directory '%s' already exists'.", dir));
        }
        return dir;
    }

    private static final class FileUtils {

        @SuppressWarnings("ConstantConditions")
        private static boolean deleteRecursive(File path) throws FileNotFoundException {
            boolean success = true;
            if (path.isDirectory()) {
                for (File file : path.listFiles()) {
                    success = success && FileUtils.deleteRecursive(file);
                }
            }
            return success && path.delete();
        }

    }

}
