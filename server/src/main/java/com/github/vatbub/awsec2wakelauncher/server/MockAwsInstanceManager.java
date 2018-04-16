package com.github.vatbub.awsec2wakelauncher.server;

/*-
 * #%L
 * AWS EC2 WakeLauncher Server
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


import java.util.HashMap;
import java.util.Map;

public class MockAwsInstanceManager extends AwsInstanceManager {
    private final Map<String, Long> instances = new HashMap<>();
    private int secondsToStartInstance;

    public MockAwsInstanceManager(String region, String awsAccessKeyId, String awsSecretKey, int secondsToStartInstance) {
        super(region, awsAccessKeyId, awsSecretKey);
        setSecondsToStartInstance(secondsToStartInstance);
    }

    @Override
    public boolean isInstanceRunning(String instanceId) {
        if (!instances.containsKey(instanceId))
            return false;
        return instances.get(instanceId) + getSecondsToStartInstance() <= getUnixTime();
    }

    @Override
    public void startInstance(String instanceId) {
        instances.put(instanceId, getUnixTime());
    }

    @Override
    public void stopInstance(String instanceId) {
        instances.remove(instanceId);
    }

    public int getSecondsToStartInstance() {
        return secondsToStartInstance;
    }

    public void setSecondsToStartInstance(int secondsToStartInstance) {
        this.secondsToStartInstance = secondsToStartInstance;
    }

    private long getUnixTime() {
        return System.currentTimeMillis() / 1000L;
    }
}
