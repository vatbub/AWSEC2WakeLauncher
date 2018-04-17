package com.github.vatbub.awsec2wakelauncher.applicationclient;

/*-
 * #%L
 * awsec2wakelauncher.applicationclient
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


import com.github.vatbub.awsec2wakelauncher.server.Api;
import com.github.vatbub.awsec2wakelauncher.unittestcommons.MockAwsInstanceManager;
import com.github.vatbub.awsec2wakelauncher.unittestcommons.TomcatTest;
import org.apache.catalina.LifecycleException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ClientTest extends TomcatTest {
    private static final int TOMCAT_PORT = 9999;
    private static final String apiSuffix = "api";
    private static Api api;
    private Client client;

    @BeforeClass
    public static void startServer() throws LifecycleException, IOException {
        api = new Api();
        TomcatTest.startServer(TOMCAT_PORT, "", "ApiServlet", api, "/" + apiSuffix);
    }

    @Before
    public void setClientUp() throws MalformedURLException {
        client = new Client(new URL("http", "localhost", TOMCAT_PORT, ""), apiSuffix);
    }

    private void useMockInstanceManager() {
        if (api.getAwsInstanceManager() instanceof MockAwsInstanceManager)
            return; // already using mock manager

        api.setAwsInstanceManager(new MockAwsInstanceManager(10));
    }
    @Test
    public void basicClientTest() throws Exception {
        client.launchAndWaitForInstance("i-45678765");
    }
}
