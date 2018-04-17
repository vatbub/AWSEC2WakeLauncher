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


import com.amazonaws.services.ec2.model.InstanceState;
import com.github.vatbub.awsec2wakelauncher.common.ShutdownRequest;
import com.github.vatbub.awsec2wakelauncher.common.WakeRequest;
import com.github.vatbub.awsec2wakelauncher.common.WakeResponse;
import com.github.vatbub.awsec2wakelauncher.common.internal.AwsInstanceManager;
import com.github.vatbub.awsec2wakelauncher.common.internal.Constants;
import com.github.vatbub.awsec2wakelauncher.common.internal.Request;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Api extends HttpServlet {
    private AwsInstanceManager awsInstanceManager;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private WakeRequest sampleWakeRequest = new WakeRequest("");
    private ShutdownRequest sampleShutdownRequest = new ShutdownRequest("");

    @Override
    public void init() throws ServletException {
        super.init();

        if (Common.getInstance().getAppName() == null)
            Common.getInstance().setAppName(Constants.SERVER_APP_NAME);

        if (getAwsInstanceManager() == null)
            resetInstanceManager();
    }

    public void resetInstanceManager() {
        setAwsInstanceManager(new AwsInstanceManager(System.getenv(Constants.AWS_REGION_ENV_NAME), System.getenv(Constants.AWS_KEY_ID_ENV_NAME), System.getenv(Constants.AWS_SECRET_ENV_NAME)));
    }

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

        FOKLogger.info(getClass().getName(), "Request body is: \n" + requestBody);

        Pattern requestTypePattern = Pattern.compile("\"requestType\": \".*\"");
        Matcher matcher = requestTypePattern.matcher(requestBody);
        if (!matcher.find()) {
            FOKLogger.warning(getClass().getName(), "Request did not specify a request type");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request type missing");
            return;
        }

        // request contains request type
        String requestTypeTemp = matcher.group(0);
        requestTypeTemp = requestTypeTemp.replace("\"", "");
        requestTypeTemp = requestTypeTemp.replace("requestType: ", "");

        Request.RequestType requestType = Request.RequestType.valueOf(requestTypeTemp);

        FOKLogger.fine(getClass().getName(), "Request type is: " + requestType);

        switch (requestType) {
            case WakeRequest:
                WakeRequest wakeRequest = gson.fromJson(requestBody, WakeRequest.class);
                WakeResponse wakeResponse = new WakeResponse(wakeRequest.getInstanceId());
                InstanceState instanceState = getAwsInstanceManager().getInstanceState(wakeRequest.getInstanceId());

                wakeResponse.setInstanceState(instanceState.getCode());

                switch (instanceState.getCode()) {
                    case 0: // pending
                    case 16: // running
                    case 32: // shutting-down
                    case 48: // terminated
                    case 64: // stopping
                        // in any of the above cases, we
                        break;
                    case 80: // stopped
                }

                if (instanceState.getCode() == 80)
                    // only start the instance when it's stopped (cannot start pending, running, stopping or terminated instances.
                    getAwsInstanceManager().startInstance(wakeRequest.getInstanceId());

                StringReader stringReader = new StringReader(gson.toJson(wakeResponse, WakeResponse.class));
                IOUtils.copy(stringReader, resp.getOutputStream(), Charset.forName("UTF-8"));
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case ShutdownRequest:
                break;
            default:
                FOKLogger.info(getClass().getName(), "Request had illegal request type, sending error...");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal request type");
        }
    }

    public AwsInstanceManager getAwsInstanceManager() {
        return awsInstanceManager;
    }

    public void setAwsInstanceManager(AwsInstanceManager awsInstanceManager) {
        this.awsInstanceManager = awsInstanceManager;
    }
}
