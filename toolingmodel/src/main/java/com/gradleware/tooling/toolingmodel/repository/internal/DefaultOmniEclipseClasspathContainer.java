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

import com.gradleware.tooling.toolingmodel.OmniAccessRule;
import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute;
import com.gradleware.tooling.toolingmodel.OmniEclipseClasspathContainer;
import org.gradle.tooling.model.eclipse.EclipseClasspathContainer;

import java.util.List;

/**
 * Default implementation of the {@link OmniEclipseClasspathContainer} interface.
 *
 * @author Donat Csikos
 */
public class DefaultOmniEclipseClasspathContainer extends AbstractOmniClasspathEntry implements OmniEclipseClasspathContainer {

    private final String path;

    private DefaultOmniEclipseClasspathContainer(String path, List<OmniClasspathAttribute> attributes, List<OmniAccessRule> accessRules) {
        super(attributes, accessRules);
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public static DefaultOmniEclipseClasspathContainer from(EclipseClasspathContainer container) {
        return new DefaultOmniEclipseClasspathContainer(
                container.getPath(),
                getClasspathAttributes(container),
                getAccessRules(container));
    }

}
