package com.github.vatbub.awsec2wakelauncher.common;

import com.github.vatbub.awsec2wakelauncher.common.internal.Response;
import com.github.vatbub.common.updater.Version;

public class ShutdownResponse extends Response {
    public ShutdownResponse(String instanceId) {
        super(instanceId);
    }

    public ShutdownResponse(String instanceId, Version protocolVersion) {
        super(instanceId, protocolVersion);
    }
}
