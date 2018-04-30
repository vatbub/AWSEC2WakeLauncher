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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

@WebServlet("/AppExceptionHandler")
public class ErrorHandlingServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processError(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processError(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        processError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        processError(request, response);
    }

    private void processError(HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        String additionalErrorMessage = (String) request.getAttribute("javax.servlet.error.message");
        // Analyze the servlet exception
        Throwable throwable = (Throwable) request
                .getAttribute("javax.servlet.error.exception");
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        String servletName = (String) request
                .getAttribute("javax.servlet.error.servlet_name");
        String requestUri = (String) request
                .getAttribute("javax.servlet.error.request_uri");

        ErrorResponse errorResponse = new ErrorResponse();

        if (throwable != null) {
            errorResponse.setExceptionClass(throwable.getClass().getCanonicalName());
            errorResponse.setExceptionMessage(throwable.getMessage());
        }
        errorResponse.setRequestUri(requestUri);
        errorResponse.setAdditionalErrorMessage(additionalErrorMessage);
        errorResponse.setServletName(servletName);

        String responseJson = gson.toJson(errorResponse, ErrorResponse.class);
        IOUtils.copy(new StringReader(responseJson), response.getOutputStream(), Charset.forName("UTF-8"));
        response.setContentType("application/json");
        response.setStatus(statusCode);
    }
}
