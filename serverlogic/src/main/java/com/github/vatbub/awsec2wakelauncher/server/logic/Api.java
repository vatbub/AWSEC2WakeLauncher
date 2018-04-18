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
import com.github.vatbub.awsec2wakelauncher.common.internal.AwsInstanceManager;
import com.github.vatbub.awsec2wakelauncher.common.internal.Constants;
import com.github.vatbub.awsec2wakelauncher.common.internal.Request.RequestType;
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

        RequestType requestType;

        try {
            requestType = RequestType.valueOf(requestTypeTemp);
        } catch (IllegalArgumentException e) {
            FOKLogger.info(getClass().getName(), "Request had illegal request type, sending error...");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal request type");
            return;
        }

        FOKLogger.fine(getClass().getName(), "Request type is: " + requestType);

        String responseJson;

        switch (requestType) {
            case WAKE_REQUEST:
                WakeRequest wakeRequest = gson.fromJson(requestBody, WakeRequest.class);

                if (wakeRequest.getProtocolVersion() == null) {
                    FOKLogger.info(getClass().getName(), "No protocol version specified, assuming version " + Constants.DEFAULT_PROTOCOL_VERSION);
                    wakeRequest.setProtocolVersion(Constants.DEFAULT_PROTOCOL_VERSION);
                }

                if (wakeRequest.getInstanceId() == null) {
                    FOKLogger.info(getClass().getName(), "Wake request contained no instanceId");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No instance id specified");
                    return;
                }

                WakeResponse wakeResponse = new WakeResponse(wakeRequest.getInstanceId());
                InstanceState instanceState = getAwsInstanceManager().getInstanceState(wakeRequest.getInstanceId());

                wakeResponse.setPreviousInstanceState(instanceState.getCode());

                if (instanceState.getCode() == 80)
                    // only start the instance when it's stopped (cannot start pending, running, stopping or terminated instances.
                    getAwsInstanceManager().startInstance(wakeRequest.getInstanceId());

                wakeResponse.setNewInstanceState(getAwsInstanceManager().getInstanceState(wakeRequest.getInstanceId()).getCode());

                responseJson = gson.toJson(wakeResponse, WakeResponse.class);
                break;
            case SHUTDOWN_REQUEST:
                ShutdownRequest shutdownRequest = gson.fromJson(requestBody, ShutdownRequest.class);

                if (shutdownRequest.getProtocolVersion() == null) {
                    FOKLogger.info(getClass().getName(), "No protocol version specified, assuming version " + Constants.DEFAULT_PROTOCOL_VERSION);
                    shutdownRequest.setProtocolVersion(Constants.DEFAULT_PROTOCOL_VERSION);
                }
                if (shutdownRequest.getInstanceId() == null) {
                    FOKLogger.info(getClass().getName(), "Wake request contained no instanceId");
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No instance id specified");
                    return;
                }

                ShutdownResponse shutdownResponse = new ShutdownResponse(shutdownRequest.getInstanceId());
                instanceState = getAwsInstanceManager().getInstanceState(shutdownRequest.getInstanceId());

                shutdownResponse.setPreviousInstanceState(instanceState.getCode());

                if (instanceState.getCode() == 16)
                    // only stop the instance when it's running (cannot stop pending, stopping or terminated instances.
                    getAwsInstanceManager().stopInstance(shutdownRequest.getInstanceId());

                shutdownResponse.setNewInstanceState(getAwsInstanceManager().getInstanceState(shutdownRequest.getInstanceId()).getCode());

                responseJson = gson.toJson(shutdownResponse, ShutdownResponse.class);
                break;
            default:
                FOKLogger.severe(getClass().getName(), "Internal server error: Illegal enum value");
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Illegal enum value");
                return;
        }

        IOUtils.copy(new StringReader(responseJson), resp.getOutputStream(), Charset.forName("UTF-8"));
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    public AwsInstanceManager getAwsInstanceManager() {
        return awsInstanceManager;
    }

    public void setAwsInstanceManager(AwsInstanceManager awsInstanceManager) {
        this.awsInstanceManager = awsInstanceManager;
    }
}
