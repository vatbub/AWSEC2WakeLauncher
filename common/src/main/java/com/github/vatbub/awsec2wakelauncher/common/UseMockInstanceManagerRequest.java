package com.github.vatbub.awsec2wakelauncher.common;

import com.github.vatbub.awsec2wakelauncher.common.internal.Constants;
import com.github.vatbub.awsec2wakelauncher.common.internal.Request;
import com.github.vatbub.common.updater.Version;

public class UseMockInstanceManagerRequest extends Request {
    private boolean useMockManager;
    private int secondsToStartInstance;

    public UseMockInstanceManagerRequest(boolean useMockManager) {
        this(useMockManager, 30);
    }

    public UseMockInstanceManagerRequest(boolean useMockManager, int secondsToStartInstance) {
        this(useMockManager, secondsToStartInstance, Constants.DEFAULT_PROTOCOL_VERSION);
    }

    public UseMockInstanceManagerRequest(boolean useMockManager, int secondsToStartInstance, Version protocolVersion) {
        super(null, protocolVersion);
        setUseMockManager(useMockManager);
        setSecondsToStartInstance(secondsToStartInstance);
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.UseMockManagerRequest;
    }

    public boolean isUseMockManager() {
        return useMockManager;
    }

    public void setUseMockManager(boolean useMockManager) {
        this.useMockManager = useMockManager;
    }

    public int getSecondsToStartInstance() {
        return secondsToStartInstance;
    }

    public void setSecondsToStartInstance(int secondsToStartInstance) {
        this.secondsToStartInstance = secondsToStartInstance;
    }
}
