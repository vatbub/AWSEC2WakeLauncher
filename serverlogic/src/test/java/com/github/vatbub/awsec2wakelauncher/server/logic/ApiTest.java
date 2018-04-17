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
    private static Gson gson;
    private static Api api;

    @BeforeClass
    public static void startServer() throws LifecycleException, IOException {
        api = new Api();
        TomcatTest.startServer(TOMCAT_PORT, "", "ApiServlet", api, "/" + apiSuffix);
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    //     client = new Client(new URL("http", "localhost", TOMCAT_PORT, ""), apiSuffix);

    private void useMockInstanceManager() {
        if (api.getAwsInstanceManager() instanceof MockAwsInstanceManager)
            return; // already using mock manager

        api.setAwsInstanceManager(new MockAwsInstanceManager(10));
    }

    @Test
    public void wakeRequestTest() throws Exception {
        useMockInstanceManager();

        String instanceId = "i-765876";
        WakeRequest wakeRequest = new WakeRequest(instanceId);
        String json = gson.toJson(wakeRequest, WakeRequest.class);

        int loopCounter = 0;

        while (true) {
            String responseBody = doRequest(json);
            WakeResponse wakeResponse = gson.fromJson(responseBody, WakeResponse.class);

            switch (loopCounter) {
                case 0:
                    Assert.assertEquals(80, wakeResponse.getInstanceState());
                    break;
                default:
                    Assert.assertThat(wakeResponse.getInstanceState(), Matchers.anyOf(Matchers.equalTo(0), Matchers.equalTo(16)));
            }

            loopCounter++;
            if (wakeResponse.getInstanceState() == 16)
                break;
        }
    }

    @Test
    public void requestWithoutRequestType() throws MalformedURLException, URISyntaxException {
        useMockInstanceManager();
        String instanceId = "i-765876";
        ServerInteraction serverInteraction = new ServerInteraction(instanceId) {
        };

        try {
            String responseBody = doRequest(gson.toJson(serverInteraction, ServerInteraction.class));
            Assert.fail("Expected NoSuchContentException, responseBody = \n" + responseBody);
        } catch (NoSuchContentException e) {
            FOKLogger.log(getClass().getName(), Level.INFO, "Expected exception occurred", e);
        }
    }

    @Test
    public void illegalRequestType() {
        useMockInstanceManager();
        String instanceId = "i-765876";
        RequestWithIllegalRequestType requestWithIllegalRequestType = new RequestWithIllegalRequestType(instanceId);

        try {
            String responseBody = doRequest(gson.toJson(requestWithIllegalRequestType, RequestWithIllegalRequestType.class));
            Assert.fail("Expected NoSuchContentException, responseBody = \n" + responseBody);
        } catch (NoSuchContentException e) {
            FOKLogger.log(getClass().getName(), Level.INFO, "Expected exception occurred", e);
        } catch (URISyntaxException | MalformedURLException e) {
            // unexpected
            throw new RuntimeException(e);
        }
    }

    @Test
    public void setInstanceManagerTest() {
        Assert.assertFalse(api.getAwsInstanceManager() instanceof MockAwsInstanceManager);
        useMockInstanceManager();
        Assert.assertTrue(api.getAwsInstanceManager() instanceof MockAwsInstanceManager);
        api.resetInstanceManager();
        Assert.assertFalse(api.getAwsInstanceManager() instanceof MockAwsInstanceManager);
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
