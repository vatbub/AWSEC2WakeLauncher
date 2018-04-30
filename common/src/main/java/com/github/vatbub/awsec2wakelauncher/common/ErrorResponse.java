package com.github.vatbub.awsec2wakelauncher.common;

/*-
 * #%L
 * awsec2wakelauncher.common
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


import com.github.vatbub.awsec2wakelauncher.common.internal.Response;
import com.github.vatbub.common.updater.Version;

public class ErrorResponse extends Response {
    private String servletName;
    private String exceptionClass;
    private String exceptionMessage;
    private String additionalErrorMessage;
    private String requestUri;

    public ErrorResponse() {
        super(null);
    }

    public ErrorResponse(Version protocolVersion) {
        super(null, protocolVersion);
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getAdditionalErrorMessage() {
        return additionalErrorMessage;
    }

    public void setAdditionalErrorMessage(String additionalErrorMessage) {
        this.additionalErrorMessage = additionalErrorMessage;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
}
