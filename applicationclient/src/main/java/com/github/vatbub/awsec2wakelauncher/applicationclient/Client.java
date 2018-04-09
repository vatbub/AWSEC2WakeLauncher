package com.github.vatbub.awsec2wakelauncher.applicationclient;

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

    public Client(URL serverBaseUrl) {
        setServerBaseUrl(serverBaseUrl);
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void launchAndWaitForInstance(String instanceId) throws Exception {
        AtomicBoolean ready = new AtomicBoolean(false);
        final Exception[] e = new Exception[1];

        launchInstance(instanceId, (exception) -> {
            e[0] =exception;
            ready.set(true);
        });

        while (!ready.get()) {
            System.out.print("");
        }

        if (e[0] !=null)
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
                    HttpRequest<String> httpRequest = HttpRequestBuilder.createPost(new URL(getServerBaseUrl(), "api").toURI(), String.class)
                            .responseDeserializer(ResponseDeserializer.ignorableDeserializer()).build();
                    String responseBody = httpRequest.executeWithBody(json).get();

                    WakeResponse wakeResponse = gson.fromJson(responseBody, WakeResponse.class);
                    if (wakeResponse.getInstanceState()==WakeResponse.InstanceState.RUNNING)
                        ready=true;
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

    public interface OnInstanceReadyRunnable {
        void run(Exception exception);
    }
}