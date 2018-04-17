package com.github.vatbub.awsec2wakelauncher.common;

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


import com.github.vatbub.awsec2wakelauncher.common.internal.Request;
import com.github.vatbub.common.updater.Version;

public class ShutdownRequest extends Request {
    public ShutdownRequest(String instanceId) {
        super(instanceId, RequestType.SHUTDOWN_REQUEST);
    }

    public ShutdownRequest(String instanceId, Version protocolVersion) {
        super(instanceId, protocolVersion, RequestType.SHUTDOWN_REQUEST);
    }
}
