package com.github.vatbub.awsec2wakelauncher.applicationclient;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class ClientTest {
    @Test
    public void basicClientTest() throws MalformedURLException {
        Client client = new Client(new URL("http://localhost:8080"));
        client.launchAndWaitForInstance("i-45678765");
    }
}
