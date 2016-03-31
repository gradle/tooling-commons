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

package com.gradleware.tooling.toolingclient.internal;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.gradleware.tooling.toolingclient.CompositeRequest;
import com.gradleware.tooling.toolingclient.GradleBuildIdentifier;

/**
 * Internal interface that allows reading the request configuration.
 *
 * @param <T> result type
 * @author Stefan Oehme
 */
interface InspectableCompositeRequest<T> extends InspectableRequest<Set<T>>, CompositeRequest<T> {

    GradleBuildIdentifier[] getParticipants();

    File getParticipantJavaHome(GradleBuildIdentifier participant);

    List<String> getParticipantArguments(GradleBuildIdentifier participant);

    List<String> getParticipantJvmArguments(GradleBuildIdentifier participant);
}
