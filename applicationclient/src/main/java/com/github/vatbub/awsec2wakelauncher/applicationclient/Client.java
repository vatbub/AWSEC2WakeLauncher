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


import com.github.vatbub.awsec2wakelauncher.common.WakeRequest;
import com.github.vatbub.awsec2wakelauncher.common.WakeResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jsunsoft.http.HttpRequest;
import com.jsunsoft.http.HttpRequestBuilder;
import com.jsunsoft.http.ResponseDeserializer;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private Gson gson;
    private URL serverBaseUrl;
    private String apiSuffix;

    public Client(URL serverBaseUrl) {
        this(serverBaseUrl, "api");
    }

    public Client(URL serverBaseUrl, String apiSuffix) {
        setServerBaseUrl(serverBaseUrl);
        gson = new GsonBuilder().setPrettyPrinting().create();
        setApiSuffix(apiSuffix);
    }

    public void launchAndWaitForInstance(String instanceId) throws Exception {
        AtomicBoolean ready = new AtomicBoolean(false);
        final Exception[] e = new Exception[1];

        launchInstance(instanceId, (exception) -> {
            e[0] = exception;
            ready.set(true);
        });

        while (!ready.get()) {
            System.out.print("");
        }

        if (e[0] != null)
            throw e[0];
    }

    public Thread launchInstance(String instanceId, OnInstanceReadyRunnable onInstanceReady) {
        Thread thread = new Thread(() -> {
            Exception exception = null;
            try {
                WakeRequest wakeRequest = new WakeRequest(instanceId);
                String json = gson.toJson(wakeRequest, WakeRequest.class);
                System.out.print(json);

                boolean ready = false;

                while (!ready) {
                    HttpRequest<String> httpRequest = HttpRequestBuilder.createPost(new URL(getServerBaseUrl(), getApiSuffix()).toURI(), String.class)
                            .responseDeserializer(ResponseDeserializer.ignorableDeserializer()).build();
                    String responseBody = httpRequest.executeWithBody(json).get();

                    WakeResponse wakeResponse = gson.fromJson(responseBody, WakeResponse.class);
                    if (wakeResponse.getInstanceState() == 16)
                        ready = true;
                }

            } catch (Exception e) {
                exception = e;
            } finally {
                onInstanceReady.run(exception);
            }
        });
        thread.start();
        return thread;
    }

    public URL getServerBaseUrl() {
        return serverBaseUrl;
    }

    public void setServerBaseUrl(URL serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    public String getApiSuffix() {
        return apiSuffix;
    }

    public void setApiSuffix(String apiSuffix) {
        this.apiSuffix = apiSuffix;
    }

    public interface OnInstanceReadyRunnable {
        void run(Exception exception);
    }
}
