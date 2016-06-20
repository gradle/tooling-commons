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

package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniAccessRule;
import org.gradle.tooling.model.eclipse.AccessRule;

/**
 * Default implementation of {@link OmniAccessRule}.
 *
 * @author Donat Csikos
 */
final class DefaultOmniAccessRule implements OmniAccessRule {

    private int kind;
    private String pattern;

    DefaultOmniAccessRule(int kind, String pattern) {
        this.kind = Preconditions.checkNotNull(kind);
        this.pattern = Preconditions.checkNotNull(pattern);
    }

    @Override
    public int getKind() {
        return this.kind;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    static DefaultOmniAccessRule from(AccessRule rule) {
        return new DefaultOmniAccessRule(rule.getKind(), rule.getPattern());
    }
}
