package com.github.vatbub.awsec2wakelauncher.server;

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
