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

package com.gradleware.tooling.toolingclient;

/**
 * A {@code CompositeModelRequest} allows you to fetch a snapshot of some model for a composite build. Instances of {@code CompositeModelRequest} are not thread-safe. <p/> You use
 * a {@code CompositeModelRequest} as follows: <ul> <li>Create an instance by calling {@link ToolingClient#newCompositeModelRequest(Class)}.</li> <li>Configure the request as
 * appropriate.</li> <li>Call either {@link #executeAndWait()} or {@link #execute()} to fetch the model.</li> <li>Optionally, you can reuse the request to fetch the model multiple
 * times.</li> </ul>
 *
 * @param <T> the type of model to fetch
 * @author Stefan Oehme
 */
public interface CompositeBuildModelRequest<T> extends CompositeBuildRequest<T> {

}
