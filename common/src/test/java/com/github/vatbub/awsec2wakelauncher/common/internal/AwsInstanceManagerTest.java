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


import com.amazonaws.client.builder.AwsClientBuilder;
import com.github.vatbub.awsec2wakelauncher.common.internal.testing.TomcatTest;
import com.tlswe.awsmock.cloudwatch.servlet.MockCloudWatchEndpointServlet;
import org.apache.catalina.LifecycleException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class AwsInstanceManagerTest extends TomcatTest {
    private final static int AWS_PORT = 8000;
    private final static String AWS_REGION = "eu-central-1";
    private final static String AWS_KEY = "zuhuiuztgvfcdetzu";
    private final static String AWS_SECRET = "65rt676tghgftzujhgtr56z";
    private final static String INSTANCE_ID = "i-456787";
    private AwsInstanceManager awsInstanceManager;

    @BeforeClass
    public static void startServer() throws IOException, LifecycleException {
        TomcatTest.startServer(AWS_PORT, "", "MockEc2EndpointServlet", new MockCloudWatchEndpointServlet(), "/ec2-endpoint/*");
    }

    @Before
    public void setAwsInstanceManagerUp() {
        awsInstanceManager = new AwsInstanceManager(AWS_REGION, AWS_KEY, AWS_SECRET, new AwsClientBuilder.EndpointConfiguration("http://localhost:" + AWS_PORT + "/ec2-endpoint/", AWS_REGION));
    }

    @Test
    public void startInstanceTest() {
        awsInstanceManager.startInstance(INSTANCE_ID);
        Assert.assertEquals(0, awsInstanceManager.getInstanceState(INSTANCE_ID).getCode().intValue());
    }

    @Test
    public void stopInstanceTest() {
        awsInstanceManager.startInstance(INSTANCE_ID);
        while (awsInstanceManager.getInstanceState(INSTANCE_ID).getCode() != 16)
            System.out.println("Waiting for instance to boot...");

        awsInstanceManager.stopInstance(INSTANCE_ID);
        Assert.assertEquals(64, awsInstanceManager.getInstanceState(INSTANCE_ID).getCode().intValue());
    }
}
