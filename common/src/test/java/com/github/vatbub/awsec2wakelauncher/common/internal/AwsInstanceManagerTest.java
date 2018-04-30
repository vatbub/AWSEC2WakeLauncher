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
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AwsInstanceManagerTest {
    private final static int AWS_PORT = 8000;
    private final static String AWS_REGION = "eu-central-1";
    private final static String AWS_KEY = "zuhuiuztgvfcdetzu";
    private final static String AWS_SECRET = "65rt676tghgftzujhgtr56z";
    private static String INSTANCE_ID;
    private static Process serverProcess;
    private static IOException streamCopyException;
    private AwsInstanceManager awsInstanceManager;

    @BeforeClass
    public static void startServer() throws IOException {
        serverProcess = Runtime.getRuntime().exec("moto_server ec2 -p" + AWS_PORT);
        new Thread(() -> {
            try {
                IOUtils.copy(serverProcess.getInputStream(), System.out);
            } catch (IOException e) {
                streamCopyException = e;
            }
        }).start();
    }

    @AfterClass
    public static void stopServer() throws IOException {
        if (streamCopyException != null)
            throw streamCopyException;
        if (serverProcess != null && serverProcess.isAlive())
            serverProcess.destroy();
    }

    @Before
    public void setAwsInstanceManagerUp() {
        awsInstanceManager = new AwsInstanceManager(AWS_KEY, AWS_SECRET, new AwsClientBuilder.EndpointConfiguration("http://localhost:" + AWS_PORT, AWS_REGION));

        // create the instance
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        runInstancesRequest.withImageId("ami-ac442ac3")
                .withInstanceType("m1.small")
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName("my-key-pair")
                .withSecurityGroups("my-security-group");
        RunInstancesResult runInstancesResult = awsInstanceManager.getEc2Client().runInstances(runInstancesRequest);
        INSTANCE_ID = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();

        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(INSTANCE_ID);

        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(instanceIds);
        awsInstanceManager.getEc2Client().stopInstances(stopInstancesRequest);
    }

    @After
    public void terminateInstance() {
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(INSTANCE_ID);
        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(instanceIds);
        awsInstanceManager.getEc2Client().terminateInstances(terminateInstancesRequest);
    }

    @Test
    public void startInstanceTest() {
        awsInstanceManager.startInstance(INSTANCE_ID);
        Assert.assertThat(awsInstanceManager.getInstanceState(INSTANCE_ID).getCode(), Matchers.anyOf(Matchers.equalTo(0), Matchers.equalTo(16)));
    }
}
