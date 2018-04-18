package com.github.vatbub.awsec2wakelauncher.common.internal;

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
