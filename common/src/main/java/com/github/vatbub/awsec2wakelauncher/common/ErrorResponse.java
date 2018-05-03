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


import com.github.vatbub.awsec2wakelauncher.common.internal.Constants;
import com.github.vatbub.awsec2wakelauncher.common.internal.ServerInteraction;
import com.github.vatbub.common.updater.Version;

import java.util.Objects;

public class ErrorResponse extends ServerInteraction {
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

    public ErrorResponse(String servletName, String exceptionClass, String exceptionMessage, String additionalErrorMessage, String requestUri) {
        this(servletName, exceptionClass, exceptionMessage, additionalErrorMessage, requestUri, Constants.DEFAULT_PROTOCOL_VERSION);
    }

    public ErrorResponse(String servletName, String exceptionClass, String exceptionMessage, String additionalErrorMessage, String requestUri, Version protocolVersion) {
        super(null, protocolVersion);
        this.servletName = servletName;
        this.exceptionClass = exceptionClass;
        this.exceptionMessage = exceptionMessage;
        this.additionalErrorMessage = additionalErrorMessage;
        this.requestUri = requestUri;
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ErrorResponse))
            return false;
        ErrorResponse that = (ErrorResponse) obj;
        return Objects.equals(this.getAdditionalErrorMessage(), that.getAdditionalErrorMessage())
                && Objects.equals(this.getExceptionClass(), that.getExceptionClass())
                && Objects.equals(this.getExceptionMessage(), that.getExceptionMessage())
                && Objects.equals(this.getRequestUri(), that.getRequestUri())
                && Objects.equals(this.getServletName(), that.getServletName());
    }
}
