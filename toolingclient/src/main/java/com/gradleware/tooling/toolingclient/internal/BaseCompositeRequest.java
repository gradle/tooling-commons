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

import com.gradleware.tooling.toolingclient.CompositeRequest;
import com.gradleware.tooling.toolingclient.GradleBuildIdentifier;

/**
 * @author Stefan Oehme
 *
 * @param <T> the result type
 * @param <SELF> self reference
 */
abstract class BaseCompositeRequest<T, SELF extends BaseCompositeRequest<T, SELF>> extends BaseRequest<T, SELF>implements InspectableCompositeRequest<T> {

    BaseCompositeRequest(ExecutableToolingClient toolingClient) {
        super(toolingClient);
    }

    @Override
    public CompositeRequest<T> participants(GradleBuildIdentifier... buildIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompositeRequest<T> addParticipants(GradleBuildIdentifier... buildIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GradleBuildIdentifier[] getParticipants() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    <S, S_SELF extends BaseRequest<S, S_SELF>> S_SELF copy(BaseRequest<S, S_SELF> request) {
        S_SELF copy = super.copy(request);
        if (copy instanceof BaseSimpleRequest) {
            @SuppressWarnings("rawtypes")
            BaseCompositeRequest compositeRequest = (BaseCompositeRequest) request;
            compositeRequest.participants(getParticipants());
        }
        return copy;
    }

}
