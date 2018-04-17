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
import com.github.vatbub.awsec2wakelauncher.unittestcommons.MockAwsInstanceManager;
import com.github.vatbub.awsec2wakelauncher.unittestcommons.TomcatTest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jsunsoft.http.HttpRequest;
import com.jsunsoft.http.HttpRequestBuilder;
import com.jsunsoft.http.ResponseDeserializer;
import org.apache.catalina.LifecycleException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;


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
        System.out.print(json);

        int loopCounter = 0;

        while (true) {
            HttpRequest<String> httpRequest = HttpRequestBuilder.createPost(new URL(new URL("http", "localhost", TOMCAT_PORT, ""), apiSuffix).toURI(), String.class)
                    .responseDeserializer(ResponseDeserializer.ignorableDeserializer()).build();
            String responseBody = httpRequest.executeWithBody(json).get();

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
}
