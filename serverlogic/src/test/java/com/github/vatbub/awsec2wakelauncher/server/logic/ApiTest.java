package com.github.vatbub.awsec2wakelauncher.server.logic;

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


import com.amazonaws.services.ec2.model.InstanceState;
import com.github.vatbub.awsec2wakelauncher.common.ShutdownRequest;
import com.github.vatbub.awsec2wakelauncher.common.ShutdownResponse;
import com.github.vatbub.awsec2wakelauncher.common.WakeRequest;
import com.github.vatbub.awsec2wakelauncher.common.WakeResponse;
import com.github.vatbub.awsec2wakelauncher.common.internal.ServerInteraction;
import com.github.vatbub.awsec2wakelauncher.unittestcommons.MockAwsInstanceManager;
import com.github.vatbub.awsec2wakelauncher.unittestcommons.TomcatTest;
import com.github.vatbub.common.core.logging.FOKLogger;
import com.github.vatbub.common.updater.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jsunsoft.http.HttpRequest;
import com.jsunsoft.http.HttpRequestBuilder;
import com.jsunsoft.http.NoSuchContentException;
import com.jsunsoft.http.ResponseDeserializer;
import org.apache.catalina.LifecycleException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

public class ApiTest extends TomcatTest {
    private static final int TOMCAT_PORT = 9999;
    private static final String apiSuffix = "api";
    private final static String INSTANCE_ID = "i-765876";
    private static Gson gson;
    private static Api api;
    private static MockAwsInstanceManager mockAwsInstanceManager;

    //     client = new Client(new URL("http", "localhost", TOMCAT_PORT, ""), apiSuffix);

    @BeforeClass
    public static void startServer() throws LifecycleException, IOException {
        api = new Api();
        TomcatTest.startServer(TOMCAT_PORT, "", "ApiServlet", api, "/" + apiSuffix);
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private static void useMockInstanceManager() {
        if (api.getAwsInstanceManager() instanceof MockAwsInstanceManager)
            return; // already using mock manager

        mockAwsInstanceManager = new MockAwsInstanceManager(10);
        api.setAwsInstanceManager(mockAwsInstanceManager);
    }

    @Before
    public void resetMockInstanceManager() {
        api.resetInstanceManager();
    }

    @Test
    public void wakeRequestTest() throws Exception {
        useMockInstanceManager();

        WakeRequest wakeRequest = new WakeRequest(INSTANCE_ID);
        String json = gson.toJson(wakeRequest, WakeRequest.class);

        int loopCounter = 0;

        while (true) {
            String responseBody = doRequest(json);
            WakeResponse wakeResponse = gson.fromJson(responseBody, WakeResponse.class);

            switch (loopCounter) {
                case 0:
                    Assert.assertEquals(80, wakeResponse.getPreviousInstanceState());
                    Assert.assertEquals(0, wakeResponse.getNewInstanceState());
                    break;
                default:
                    Assert.assertThat(wakeResponse.getPreviousInstanceState(), Matchers.anyOf(Matchers.equalTo(0), Matchers.equalTo(16)));
                    Assert.assertThat(wakeResponse.getNewInstanceState(), Matchers.anyOf(Matchers.equalTo(0), Matchers.equalTo(16)));
            }

            loopCounter++;
            if (wakeResponse.getNewInstanceState() == 16)
                break;
        }
    }

    @Test
    public void shutdownRequestTest() throws Exception {
        useMockInstanceManager();

        // pretend the instance was already started
        mockAwsInstanceManager.getMockInstanceStates().put(INSTANCE_ID, new InstanceState().withCode(16));

        ShutdownRequest shutdownRequest = new ShutdownRequest(INSTANCE_ID);
        String json = gson.toJson(shutdownRequest, ShutdownRequest.class);

        int loopCounter = 0;

        while (true) {
            String responseBody = doRequest(json);
            ShutdownResponse wakeResponse = gson.fromJson(responseBody, ShutdownResponse.class);

            switch (loopCounter) {
                case 0:
                    Assert.assertEquals(16, wakeResponse.getPreviousInstanceState());
                    Assert.assertEquals(64, wakeResponse.getNewInstanceState());
                    break;
                default:
                    Assert.assertThat(wakeResponse.getPreviousInstanceState(), Matchers.anyOf(Matchers.equalTo(64), Matchers.equalTo(80)));
                    Assert.assertThat(wakeResponse.getNewInstanceState(), Matchers.anyOf(Matchers.equalTo(64), Matchers.equalTo(80)));
            }

            loopCounter++;
            if (wakeResponse.getNewInstanceState() == 80)
                break;
        }
    }

    @Test
    public void requestWithoutRequestType() throws MalformedURLException, URISyntaxException {
        useMockInstanceManager();
        ServerInteraction serverInteraction = new ServerInteraction(INSTANCE_ID) {
        };

        try {
            String responseBody = doRequest(gson.toJson(serverInteraction, ServerInteraction.class));
            Assert.fail("Expected NoSuchContentException, responseBody = \n" + responseBody);
        } catch (NoSuchContentException e) {
            FOKLogger.log(getClass().getName(), Level.INFO, "Expected exception occurred", e);
        }
    }

    @Test(expected = NoSuchContentException.class)
    public void illegalRequestType() throws MalformedURLException, URISyntaxException {
        useMockInstanceManager();
        RequestWithIllegalRequestType requestWithIllegalRequestType = new RequestWithIllegalRequestType(INSTANCE_ID);

        String responseBody = doRequest(gson.toJson(requestWithIllegalRequestType, RequestWithIllegalRequestType.class));
        Assert.fail("Expected NoSuchContentException, responseBody = \n" + responseBody);
    }

    @Test
    public void setInstanceManagerTest() {
        Assert.assertFalse(api.getAwsInstanceManager() instanceof MockAwsInstanceManager);
        useMockInstanceManager();
        Assert.assertTrue(api.getAwsInstanceManager() instanceof MockAwsInstanceManager);
        api.resetInstanceManager();
        Assert.assertFalse(api.getAwsInstanceManager() instanceof MockAwsInstanceManager);
    }

    @Test(expected = NoSuchContentException.class)
    public void wakeRequestWithoutInstanceId() throws Exception {
        useMockInstanceManager();

        WakeRequest wakeRequest = new WakeRequest(null);
        String json = gson.toJson(wakeRequest, WakeRequest.class);

        doRequest(json);
    }

    @Test(expected = NoSuchContentException.class)
    public void shutdownRequestWithoutInstanceId() throws Exception {
        useMockInstanceManager();

        ShutdownRequest shutdownRequest = new ShutdownRequest(null);
        String json = gson.toJson(shutdownRequest, WakeRequest.class);

        doRequest(json);
    }

    @Test
    public void wakeRequestWithoutProtocolVersion() throws Exception {
        useMockInstanceManager();

        WakeRequest wakeRequest = new WakeRequest(INSTANCE_ID, null);
        String json = gson.toJson(wakeRequest, WakeRequest.class);

        System.out.println(doRequest(json));
    }

    @Test
    public void shutdownRequestWithoutProtocolVersion() throws Exception {
        useMockInstanceManager();

        ShutdownRequest shutdownRequest = new ShutdownRequest(INSTANCE_ID, null);
        String json = gson.toJson(shutdownRequest, WakeRequest.class);

        System.out.println(doRequest(json));
    }

    private String doRequest(String json) throws MalformedURLException, URISyntaxException {
        FOKLogger.info(getClass().getName(), "Sending the following json:\n" + json);
        HttpRequest<String> httpRequest = HttpRequestBuilder.createPost(new URL(new URL("http", "localhost", TOMCAT_PORT, ""), apiSuffix).toURI(), String.class)
                .responseDeserializer(ResponseDeserializer.ignorableDeserializer()).build();
        return httpRequest.executeWithBody(json).get();
    }

    private class RequestWithIllegalRequestType extends ServerInteraction {
        public String requestType = "ILLEGAL_VALUE";

        public RequestWithIllegalRequestType(String instanceId) {
            super(instanceId);
        }

        public RequestWithIllegalRequestType(String instanceId, Version protocolVersion) {
            super(instanceId, protocolVersion);
        }
    }
}
