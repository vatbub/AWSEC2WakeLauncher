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


import com.github.vatbub.awsec2wakelauncher.common.ErrorResponse;
import com.github.vatbub.common.core.logging.FOKLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jsunsoft.http.*;
import org.apache.catalina.LifecycleException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ErrorHandlingServletTest extends WebappTest {
    private static final int TOMCAT_PORT = 9999;
    private static final String API_SUFFIX = "api";
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @BeforeClass
    public static void startServer() throws ServletException, IOException, LifecycleException {
        WebappTest.startServer(TOMCAT_PORT, "", "src/main/webapp");
    }

    @Test
    public void test404() throws MalformedURLException, URISyntaxException {
        String four0fourSuffix = "uizfciuzfcu";

        ResponseHandler<String> response = doEmptyPostRequest(four0fourSuffix);
        FOKLogger.info(getClass().getName(), "Response was:\n" + response);

        assertErrorResponse(404, "MainServlet", null, null, "/" + four0fourSuffix, "/" + four0fourSuffix, response);
    }

    @Test
    public void test405() throws MalformedURLException, URISyntaxException {

        ResponseHandler<String> response = doRequest(HttpMethod.GET, "", API_SUFFIX);
        FOKLogger.info(getClass().getName(), "Response was:\n" + response);

        assertErrorResponse(405, "ApiServlet", null, null, "HTTP method GET is not supported by this URL", "/" + API_SUFFIX, response);
    }

    @Test
    public void testMissingRequestType() throws MalformedURLException, URISyntaxException {
        ResponseHandler<String> response = doEmptyPostRequest(API_SUFFIX);
        FOKLogger.info(getClass().getName(), "Response was:\n" + response);

        assertErrorResponse(400, "ApiServlet", null, null, "Request type missing", "/" + API_SUFFIX, response);
    }

    private void assertErrorResponse(int expectedStatusCode, String expectedServletName, String expectedExceptionClass, String expectedExceptionMessage, String expectedAdditionalErrorMessage, String expectedRequestUri, ResponseHandler<String> actual) {
        Assert.assertEquals(expectedStatusCode, actual.getStatusCode());
        ErrorResponse errorResponse = gson.fromJson(actual.getErrorText(), ErrorResponse.class);
        Assert.assertEquals(expectedServletName, errorResponse.getServletName());
        Assert.assertEquals(expectedExceptionClass, errorResponse.getExceptionClass());
        Assert.assertEquals(expectedExceptionMessage, errorResponse.getExceptionMessage());
        Assert.assertEquals(expectedAdditionalErrorMessage, errorResponse.getAdditionalErrorMessage());
        Assert.assertEquals(expectedRequestUri, errorResponse.getRequestUri());
    }

    private ResponseHandler<String> doEmptyPostRequest(String urlSuffix) throws MalformedURLException, URISyntaxException {
        return doRequest(HttpMethod.POST, "", urlSuffix);
    }

    private ResponseHandler<String> doRequest(HttpMethod httpMethod, String json, String urlSuffix) throws MalformedURLException, URISyntaxException {
        FOKLogger.info(getClass().getName(), "Sending the following json:\n" + json);
        HttpRequest<String> httpRequest = HttpRequestBuilder.create(httpMethod, new URL(new URL("http", "localhost", TOMCAT_PORT, ""), urlSuffix).toURI(), String.class)
                .responseDeserializer(ResponseDeserializer.ignorableDeserializer()).build();
        return httpRequest.executeWithBody(json);
    }
}
