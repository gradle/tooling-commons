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

package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniJavaRuntime;
import com.gradleware.tooling.toolingmodel.OmniJavaSourceSettings;
import com.gradleware.tooling.toolingmodel.OmniJavaVersion;

/**
 * Default implementation of the {@link OmniJavaSourceSettings} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniJavaSourceSettings implements OmniJavaSourceSettings {

    private final OmniJavaVersion sourceLanguageLevel;
    private final OmniJavaVersion targetBytecodeLevel;
    private final OmniJavaRuntime targetRuntime;

    private DefaultOmniJavaSourceSettings(OmniJavaVersion sourceLanguageLevel, OmniJavaVersion targetBytecodeLevel, OmniJavaRuntime targeRuntime) {
        this.sourceLanguageLevel = sourceLanguageLevel;
        this.targetBytecodeLevel = targetBytecodeLevel;
        this.targetRuntime = targeRuntime;
    }

    @Override
    public OmniJavaVersion getSourceLanguageLevel() {
        return this.sourceLanguageLevel;
    }

    @Override
    public OmniJavaVersion getTargetBytecodeLevel() {
        return this.targetBytecodeLevel;
    }

    @Override
    public OmniJavaRuntime getTargetRuntime() {
        return this.targetRuntime;
    }

    public static DefaultOmniJavaSourceSettings from(OmniJavaVersion sourceLanguageLevel, OmniJavaVersion targetBytecodeLevel, OmniJavaRuntime targeRuntime) {
        return new DefaultOmniJavaSourceSettings(sourceLanguageLevel, targetBytecodeLevel, targeRuntime);
    }

}
