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


import com.github.vatbub.awsec2wakelauncher.common.ShutdownRequest;
import com.github.vatbub.awsec2wakelauncher.common.WakeRequest;
import com.github.vatbub.awsec2wakelauncher.common.WakeResponse;
import com.github.vatbub.awsec2wakelauncher.common.internal.Constants;
import com.github.vatbub.common.core.Common;
import com.github.vatbub.common.core.logging.FOKLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class Api extends HttpServlet {
    static {
        if (Common.getInstance().getAppName() == null)
            Common.getInstance().setAppName(Constants.SERVER_APP_NAME);
    }

    private WakeRequest sampleWakeRequest = new WakeRequest("");
    private ShutdownRequest sampleShutdownRequest = new ShutdownRequest("");
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        FOKLogger.info(getClass().getName(), "Received POST request, parsing...");
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(req.getInputStream(), stringWriter, Charset.forName("UTF-8"));
        String requestBody = stringWriter.toString();

        FOKLogger.fine(getClass().getName(), "Request body is: \n" + requestBody);

        if (requestBody.contains(sampleWakeRequest.getRequestType())) {
            FOKLogger.fine(getClass().getName(), "Request type is: " + sampleWakeRequest.getRequestType());
            WakeRequest wakeRequest = gson.fromJson(requestBody, WakeRequest.class);

            // handle the wake request

            WakeResponse wakeResponse = new WakeResponse(wakeRequest.getInstanceId());
            StringReader stringReader = new StringReader(gson.toJson(wakeResponse, WakeResponse.class));
            IOUtils.copy(stringReader, resp.getOutputStream(), Charset.forName("UTF-8"));
            resp.setStatus(HttpServletResponse.SC_OK);
        } else if (requestBody.contains(sampleShutdownRequest.getRequestType())) {
            FOKLogger.fine(getClass().getName(), "Request type is: " + sampleShutdownRequest.getRequestType());
        } else {
            FOKLogger.info(getClass().getName(), "Request had illegal request type, sending error...");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal request type");
        }
    }
}
