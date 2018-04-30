package com.github.vatbub.awsec2wakelauncher.common.internal;

/*-
 * #%L
 * webappRunnerSample Maven Webapp
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


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the connection to AWS EC2 and performs the actual AWS api calls.
 */
public class AwsInstanceManager {
    private AmazonEC2 ec2Client;

    /**
     * Instantiates a new instance manager with the specified credentials
     *
     * @param region         The region to connect to. Use the region ids mentioned in the {@code Region} column of <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#ec2_region">this</a> table.
     * @param awsAccessKeyId The id of the access key to use to connect to EC2. Create this in the IAM.
     * @param awsSecretKey   The secret of the access key to use to connect to EC2. Create this in the IAM.
     */
    public AwsInstanceManager(String region, String awsAccessKeyId, String awsSecretKey) {
        this(region, awsAccessKeyId, awsSecretKey, null);
    }

    /**
     * Instantiates a new instance manager with the specified credentials
     *
     * @param endpointConfiguration The endpoint configuration to use.
     * @param awsAccessKeyId        The id of the access key to use to connect to EC2. Create this in the IAM.
     * @param awsSecretKey          The secret of the access key to use to connect to EC2. Create this in the IAM.
     */
    public AwsInstanceManager(String awsAccessKeyId, String awsSecretKey, @NotNull AwsClientBuilder.EndpointConfiguration endpointConfiguration) {
        this(null, awsAccessKeyId, awsSecretKey, endpointConfiguration);
    }

    private AwsInstanceManager(String region, String awsAccessKeyId, String awsSecretKey, AwsClientBuilder.EndpointConfiguration endpointConfiguration) {
        AmazonEC2ClientBuilder clientBuilder = AmazonEC2Client.builder().withCredentials(new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return awsAccessKeyId;
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return awsSecretKey;
                    }
                };
            }

            @Override
            public void refresh() {
                // No-op
            }
        });

        if (endpointConfiguration == null)
            clientBuilder.withRegion(region);
        else
            clientBuilder.withEndpointConfiguration(endpointConfiguration);

        setEc2Client(clientBuilder.build());
    }

    /**
     * Returns the underlying AWS EC2 client used to perform api calls.
     *
     * @return The underlying AWS EC2 client used to perform api calls.
     */
    public AmazonEC2 getEc2Client() {
        return ec2Client;
    }

    /**
     * Sets the underlying AWS EC2 client used to perform api calls. You should not need to use this.
     *
     * @param ec2Client The underlying AWS EC2 client to be used to perform api calls.
     */
    public void setEc2Client(AmazonEC2 ec2Client) {
        this.ec2Client = ec2Client;
    }

    /**
     * Returns the state of the specified instance
     *
     * @param instanceId The id of the instance to get the state for
     * @return The state of the specified instance
     */
    public InstanceState getInstanceState(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        List<String> instanceIds = new ArrayList<>(1);
        instanceIds.add(instanceId);
        describeInstancesRequest.setInstanceIds(instanceIds);
        DescribeInstancesResult describeInstancesResult = getEc2Client().describeInstances(describeInstancesRequest);
        Instance instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);
        return instance.getState();
    }

    /**
     * Starts the specified instance
     *
     * @param instanceId The instance to start
     */
    public void startInstance(String instanceId) {
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(instanceId);
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest(instanceIds);
        getEc2Client().startInstances(startInstancesRequest);
    }
}
