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


import com.github.vatbub.awsec2wakelauncher.common.internal.testing.MockAwsInstanceManager;
import com.github.vatbub.awsec2wakelauncher.common.internal.testing.TomcatTest;
import com.github.vatbub.awsec2wakelauncher.server.logic.Api;
import com.jsunsoft.http.NoSuchContentException;
import org.apache.catalina.LifecycleException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientTest extends TomcatTest {
    private static final int TOMCAT_PORT = 9999;
    private static final String apiSuffix = "api";
    private static Api api;

    @BeforeClass
    public static void startServer() throws LifecycleException, IOException {
        api = new Api();
        TomcatTest.startServer(TOMCAT_PORT, "", "ApiServlet", api, "/" + apiSuffix);
    }

    @Before
    public void resetMockInstanceManager() {
        api.setAwsInstanceManager(new MockAwsInstanceManager(10));
    }

    @Test
    public void basicSyncTest() throws Exception {
        Client client = new Client(new URL("http", "localhost", TOMCAT_PORT, ""), apiSuffix);
        client.launchAndWaitForInstance("i-45678765");
    }

    @Test
    public void basicAsyncTest() throws Exception {
        Client client = new Client(new URL("http", "localhost", TOMCAT_PORT, ""), apiSuffix);

        final Exception[] e = new Exception[1];

        Thread thread = client.launchInstance("i-45678765", (exception) -> e[0] = exception);

        thread.join();

        Assert.assertNull(e[0]);
    }

    @Test(expected = NoSuchContentException.class)
    public void illegalServerURLSyncTest() throws Exception {
        Client client = new Client(new URL("http", "someIllegalHost", TOMCAT_PORT, ""), apiSuffix);
        client.launchAndWaitForInstance("i-45678765");
    }

    @Test
    public void illegalServerURLAsyncTest() throws Exception {
        Client client = new Client(new URL("http", "someIllegalHost", TOMCAT_PORT, ""), apiSuffix);

        AtomicBoolean ready = new AtomicBoolean(false);
        final Exception[] e = new Exception[1];

        client.launchInstance("i-45678765", (exception) -> {
            e[0] = exception;
            ready.set(true);
        });

        while (!ready.get()) {
            System.out.print("");
        }

        Assert.assertNotNull(e[0]);
    }
}
