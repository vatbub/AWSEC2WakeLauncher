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


import com.amazonaws.services.ec2.model.InstanceState;
import com.github.vatbub.common.core.logging.FOKLogger;

import java.util.HashMap;
import java.util.Map;

public class MockAwsInstanceManager extends AwsInstanceManager {
    private final Map<String, InstanceState> mockInstanceStates = new HashMap<>();
    private final Map<String, InstanceLifecycleThread> instanceTransitions = new HashMap<>();
    private int durationOfInstanceTransitionsInSeconds;

    public MockAwsInstanceManager(int durationOfInstanceTransitionsInSeconds) {
        super("eu-central-1", null, null);
        setDurationOfInstanceTransitionsInSeconds(durationOfInstanceTransitionsInSeconds);
    }

    @Override
    public InstanceState getInstanceState(String instanceId) {
        if (getMockInstanceStates().containsKey(instanceId))
            return getMockInstanceStates().get(instanceId);

        return new InstanceState().withCode(80);
    }

    @Override
    public void startInstance(String instanceId) {
        if (instanceTransitions.containsKey(instanceId))
            instanceTransitions.get(instanceId).finishTransition();

        StartInstanceThread startInstanceThread = new StartInstanceThread(instanceId);
        instanceTransitions.put(instanceId, startInstanceThread);
        startInstanceThread.start();
    }

    @Override
    public void stopInstance(String instanceId) {
        if (instanceTransitions.containsKey(instanceId))
            instanceTransitions.get(instanceId).finishTransition();

        StopInstanceThread stopInstanceThread = new StopInstanceThread(instanceId);
        instanceTransitions.put(instanceId, stopInstanceThread);
        stopInstanceThread.start();
    }

    public int getDurationOfInstanceTransitionsInSeconds() {
        return durationOfInstanceTransitionsInSeconds;
    }

    public void setDurationOfInstanceTransitionsInSeconds(int durationOfInstanceTransitionsInSeconds) {
        this.durationOfInstanceTransitionsInSeconds = durationOfInstanceTransitionsInSeconds;
    }

    private long getUnixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public Map<String, InstanceState> getMockInstanceStates() {
        return mockInstanceStates;
    }

    /*
    0 : pending
16 : running
32 : shutting-down
48 : terminated
64 : stopping
80 : stopped
     */
    private abstract class InstanceLifecycleThread extends Thread {
        private boolean finish;
        private String instanceId;
        private long startTime;

        public InstanceLifecycleThread(String instanceId) {
            setInstanceId(instanceId);
            startTime = getUnixTime();
        }

        @Override
        public void run() {
            instanceTransitions.remove(getInstanceId());
        }

        public void updateInstanceState(int statusCode) {
            getMockInstanceStates().put(getInstanceId(), new InstanceState().withCode(statusCode));
        }

        public void finishTransition() {
            finish = true;
        }

        public String getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }

        public boolean isFinish() {
            return finish;
        }

        public long getStartTime() {
            return startTime;
        }
    }

    private class StartInstanceThread extends InstanceLifecycleThread {

        public StartInstanceThread(String instanceId) {
            super(instanceId);
        }

        @Override
        public void run() {
            updateInstanceState(0);

            while (!isFinish() && getStartTime() + getDurationOfInstanceTransitionsInSeconds() >= getUnixTime()) {
                FOKLogger.fine(getClass().getName(), "Waiting for mock instance to boot...");
            }

            updateInstanceState(16);

            super.run();
        }
    }

    private class StopInstanceThread extends InstanceLifecycleThread {

        public StopInstanceThread(String instanceId) {
            super(instanceId);
        }

        @Override
        public void run() {
            updateInstanceState(64);

            while (!isFinish() && getStartTime() + getDurationOfInstanceTransitionsInSeconds() >= getUnixTime()) {
                FOKLogger.fine(getClass().getName(), "Waiting for mock instance to stop...");
            }

            updateInstanceState(80);

            super.run();
        }
    }
}
