package com.github.vatbub.awsec2wakelauncher.applicationclient;

import com.github.vatbub.awsec2wakelauncher.common.WakeRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private Gson gson;
    private URL serverBaseUrl;

    public Client(URL serverBaseUrl){
        setServerBaseUrl(serverBaseUrl);
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void launchAndWaitForInstance(String instanceId){
        AtomicBoolean ready = new AtomicBoolean(false);

        launchInstance(instanceId, (exception) -> {
            if (exception!=null)
                throw new RuntimeException(exception);
            ready.set(true);
        });

        while(!ready.get()){
            System.out.print("");
        }
    }

    public void launchInstance(String instanceId, OnInstanceReadyRunnable onInstanceReady){
        new Thread(() -> {
            Exception exception = null;
            try {
            WakeRequest wakeRequest = new WakeRequest(instanceId);
            String json = gson.toJson(wakeRequest, WakeRequest.class);
            System.out.print(json);

            String type = "application/json";
            String encodedData = URLEncoder.encode( json, "UTF-8" );
            HttpURLConnection conn = (HttpURLConnection) new URL(getServerBaseUrl(), "api").openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty( "Content-Type", type );
            conn.setRequestProperty( "Content-Length", String.valueOf(encodedData.length()));
            OutputStream os = conn.getOutputStream();
            os.write(encodedData.getBytes());
            } catch (Exception e) {
                exception = e;
            }finally{
                onInstanceReady.run(exception);
            }
        }).start();
    }

    public URL getServerBaseUrl() {
        return serverBaseUrl;
    }

    public void setServerBaseUrl(URL serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    public interface OnInstanceReadyRunnable{
        void run(Exception exception) ;
    }
}
