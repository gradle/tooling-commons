/*
 * Copyright 2016 the original author or authors.
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

package com.gradleware.tooling.toolingmodel;

/**
 * An access rule defined on a classpath entry.
 *
 * @author Donat Csikos
 */
public interface OmniAccessRule {

    /**
     * Returns the access rule type.
     *
     * @return the access rule type
     */
    int getKind();

    /**
     * Returns the file pattern of this access rule.
     *
     * @return the access rule file pattern
     */
    String getPattern();
}
