/*
 * Copyright 2014 Etienne Studer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.spock;

import org.spockframework.runtime.extension.ExtensionAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that iterations of a data-driven feature should be made visible as separate features to the outside world (IDEs, reports, etc.). By default, the name of an iteration
 * is the feature's name followed by the data values of the current iteration. Example of the output for a given iteration:
 * <pre>
 * calculateTimeSpan [14:55, 15:05, 600, SECONDS]
 * calculateTimeSpan [14:55, 15:05, 10, MINUTES]</pre>
 *
 * The feature's name can be replaced by providing a naming pattern after {@code VerboseUnroll}. A naming pattern may refer to data variables by prepending their names with #.
 *
 * The {@code VerboseUnroll} annotation can also be put on a spec class. This has the same effect as putting it on every data-driven feature method that is not already annotated
 * with {@code VerboseUnroll}. By embedding the naming pattern in the method names, each method can still have its own pattern.
 *
 * Note: This extension is inspired and derived from {@link spock.lang.Unroll}.
 *
 * @see spock.lang.Unroll
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(VerboseUnrollExtension.class)
public @interface VerboseUnroll {

    String value() default "";

    Class<? extends DataValueFormatter> formatter() default DataValueFormatter.DefaultDataValueFormatter.class;

}
