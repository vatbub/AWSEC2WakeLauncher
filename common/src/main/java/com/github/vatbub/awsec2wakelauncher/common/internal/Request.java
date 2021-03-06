package com.github.vatbub.awsec2wakelauncher.common.internal;

/*-
 * #%L
 * awsec2wakelauncher.common
 * %%
 * Copyright (C) 2016 - 2018 Frederik Kammel
 * %%
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
 * #L%
 */


import com.github.vatbub.common.updater.Version;

public abstract class Request extends ServerInteraction {
    @SuppressWarnings("unused")
    public final RequestType requestType;

    public Request(String instanceId, RequestType requestType) {
        super(instanceId);
        this.requestType = requestType;
    }

    public Request(String instanceId, Version protocolVersion, RequestType requestType) {
        super(instanceId, protocolVersion);
        this.requestType = requestType;
    }

    public enum RequestType {
        WAKE_REQUEST
    }
}
