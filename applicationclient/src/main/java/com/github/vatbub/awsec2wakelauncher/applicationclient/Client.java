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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Use this class to launch AWS EC2 instances remotely.
 * This client connects to the wake launcher server.
 * Your AWS credentials and connection details are stored on the wake launcher server (and not in this client).
 * You therefore don't need to hand your AWS credentials to the users of this client.
 */
public class Client {
    private final Gson gson;
    private URL serverBaseUrl;
    private String apiSuffix;

    /**
     * Creates a new client that connects to the specified server
     *
     * @param serverBaseUrl The url of the wake launcher server to connect to.
     *                      IMPORTANT: This is not the url to the AWS EC2 instance,
     *                      it's the url of the wake launcher server.
     */
    @SuppressWarnings("unused")
    public Client(URL serverBaseUrl) {
        this(serverBaseUrl, "api");
    }

    /**
     * Creates a mnew client that connects to the specified server
     *
     * @param serverBaseUrl The url of the wake launcher server to connect to.
     *                      IMPORTANT: This is not the url to the AWS EC2 instance,
     *                      it's the url of the wake launcher server.
     * @param apiSuffix     The url suffix to reach the api under. This is usually {@code "api"} unless you changed
     *                      the servlet mapping on the wake launcher server.
     */
    public Client(URL serverBaseUrl, String apiSuffix) {
        setServerBaseUrl(serverBaseUrl);
        gson = new GsonBuilder().setPrettyPrinting().create();
        setApiSuffix(apiSuffix);
    }

    /**
     * Launches the AWS EC2 instance with the specified id and waits until it launched.
     *
     * @param instanceId The id of the instance to launch. AWS refers to that as the {@code instance-id}
     * @return Information about the instance's ip address
     * @throws Exception In case anything goes wrong
     * @see #launchInstance(String, OnInstanceReadyRunnable)
     */
    public IpInfo launchAndWaitForInstance(String instanceId) throws Exception {
        final Exception[] e = new Exception[1];
        AtomicReference<IpInfo> ipInfo2 = new AtomicReference<>();

        Thread thread = launchInstance(instanceId, (exception, ipInfo) -> {
            e[0] = exception;
            ipInfo2.set(ipInfo);
        });

        thread.join();

        if (e[0] != null)
            throw e[0];

        return ipInfo2.get();
    }

    /**
     * Launches the AWS EC2 instance with the specified id. This method returns immediately and ddoes not wait for the instance to launch.
     *
     * @param instanceId      The id of the instance to launch. AWS refers to that as the {@code instance-id}
     * @param onInstanceReady Callback that is called when the instance is ready. Keep in mind that this callback is called from a different thread.
     * @return The thread used to poll the wake launcher server. You may call {@code thread.join()} on this thread to wait for the instance to launch.
     * @see #launchAndWaitForInstance(String)
     */
    public Thread launchInstance(String instanceId, OnInstanceReadyRunnable onInstanceReady) {
        Thread thread = new Thread(() -> {
            Exception exception = null;
            IpInfo ipInfo = null;
            try {
                WakeRequest wakeRequest = new WakeRequest(instanceId);
                String json = gson.toJson(wakeRequest, WakeRequest.class);

                WakeResponse wakeResponse = null;
                int retries = 0;
                long lastPollTime = System.currentTimeMillis();

                do {
                    if (System.currentTimeMillis() - lastPollTime >= Math.pow(2, retries) * 100) {
                        retries = retries + 1;
                        lastPollTime = System.currentTimeMillis();
                        HttpRequest<String> httpRequest = HttpRequestBuilder.createPost(new URL(getServerBaseUrl(), getApiSuffix()).toURI(), String.class)
                                .responseDeserializer(ResponseDeserializer.ignorableDeserializer()).build();
                        String responseBody = httpRequest.executeWithBody(json).get();

                        wakeResponse = gson.fromJson(responseBody, WakeResponse.class);
                        ipInfo = new IpInfo(wakeResponse.getInstanceIp(), wakeResponse.getInstanceDns());
                    }
                } while (wakeResponse == null || wakeResponse.getPreviousInstanceState() != 16);

            } catch (Exception e) {
                exception = e;
            } finally {
                onInstanceReady.run(exception, ipInfo);
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
        /**
         * Called when the instance launch is complete
         *
         * @param exception The exception that occurred during the launch or {@code null} if the launch went smoothly
         * @param ipInfo    Information about the instance's ip or {@code null} if the instance failed to launch
         */
        void run(Exception exception, IpInfo ipInfo);
    }

    public static class IpInfo {
        private String instanceIp;
        private String instanceDns;

        public IpInfo() {
            this(null, null);
        }

        public IpInfo(String instanceIp, String instanceDns) {
            setInstanceIp(instanceIp);
            setInstanceDns(instanceDns);
        }

        public String getInstanceIp() {
            return instanceIp;
        }

        public void setInstanceIp(String instanceIp) {
            this.instanceIp = instanceIp;
        }

        public String getInstanceDns() {
            return instanceDns;
        }

        public void setInstanceDns(String instanceDns) {
            this.instanceDns = instanceDns;
        }
    }
}
