package com.github.vatbub.awsec2wakelauncher.server;

/*-
 * #%L
 * AWS EC2 WakeLauncher Server
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


import com.github.vatbub.common.core.logging.FOKLogger;
import com.jsunsoft.http.HttpRequest;
import com.jsunsoft.http.HttpRequestBuilder;
import com.jsunsoft.http.ResponseDeserializer;
import com.jsunsoft.http.ResponseHandler;
import org.apache.catalina.LifecycleException;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ErrorHandlingServletTest extends WebappTest {
    private static final int TOMCAT_PORT = 9999;

    @BeforeClass
    public static void startServer() throws ServletException, IOException, LifecycleException {
        WebappTest.startServer(TOMCAT_PORT, "", "src/main/webapp/WEB-INF");
    }

    @Test
    public void test404() throws MalformedURLException, URISyntaxException {
        ResponseHandler<String> response = doRequest("", "api");
        System.out.println(response);
    }

    private ResponseHandler<String> doRequest(String json, String urlSuffix) throws MalformedURLException, URISyntaxException {
        FOKLogger.info(getClass().getName(), "Sending the following json:\n" + json);
        HttpRequest<String> httpRequest = HttpRequestBuilder.createPost(new URL(new URL("http", "localhost", TOMCAT_PORT, ""), urlSuffix).toURI(), String.class)
                .responseDeserializer(ResponseDeserializer.ignorableDeserializer()).build();
        return httpRequest.executeWithBody(json);
    }
}
