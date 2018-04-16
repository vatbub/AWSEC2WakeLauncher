package com.github.vatbub.awsec2wakelauncher.server;

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
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;

public class AwsInstanceManager {
    private AmazonEC2 ec2Client;

    public AwsInstanceManager(String region, String awsAccessKeyId, String awsSecretKey) {
        setEc2Client(AmazonEC2Client.builder().withRegion(region).withCredentials(new AWSCredentialsProvider() {
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
        }).build());
    }

    public AmazonEC2 getEc2Client() {
        return ec2Client;
    }

    public void setEc2Client(AmazonEC2 ec2Client) {
        this.ec2Client = ec2Client;
    }

    public InstanceState getInstanceState(String instanceId) {
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        List<String> instanceIds = new ArrayList<>(1);
        instanceIds.add(instanceId);
        describeInstancesRequest.setInstanceIds(instanceIds);
        DescribeInstancesResult describeInstancesResult = getEc2Client().describeInstances(describeInstancesRequest);
        Instance instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);
        return instance.getState();
    }

    public void startInstance(String instanceId) {
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(instanceId);
        StartInstancesRequest startInstancesRequest = new StartInstancesRequest(instanceIds);
        getEc2Client().startInstances(startInstancesRequest);
    }

    public void stopInstance(String instanceId) {
        List<String> instanceIds = new ArrayList<>();
        instanceIds.add(instanceId);
        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(instanceIds);
        getEc2Client().stopInstances(stopInstancesRequest);
    }
}
