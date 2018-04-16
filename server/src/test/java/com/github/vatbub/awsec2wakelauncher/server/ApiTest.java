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


import com.github.vatbub.awsec2wakelauncher.applicationclient.Client;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class ApiTest {
    private static final int TOMCAT_PORT = 9999;
    private static Tomcat tomcat;
    private static Path destinationPath;
    private static Path baseDir;
    private static final String apiSuffix = "api";
    private static Context context;
    private static Api api;
    private Client client;

    @BeforeClass
    public static void startServer() throws ServletException, LifecycleException, IOException {
        List<String> relativeFolders = new ArrayList<>();
        relativeFolders.add("src");
        relativeFolders.add("main");
        relativeFolders.add("webapp");

        tomcat = new Tomcat();
        tomcat.setBaseDir(".");
        tomcat.setPort(TOMCAT_PORT);
        System.out.println(tomcat.getServer().getCatalinaHome());

        // copy src/main/webapp to webapps/src/main/webapp
        baseDir = tomcat.getServer().getCatalinaHome().toPath();
        Path sourcePath = baseDir;
        Path webappsPath = sourcePath.resolve("webapps");
        destinationPath = webappsPath;
        for (String folder : relativeFolders) {
            sourcePath = sourcePath.resolve(folder);
            destinationPath = destinationPath.resolve(folder);
        }

        FileUtils.copyDirectory(sourcePath.toFile(), destinationPath.toFile());

        Path relativePath = webappsPath.relativize(destinationPath);
        System.out.println(relativePath);

        /* There needs to be a symlink to the current dir named 'webapps' */
        context = tomcat.addContext("", relativePath.toString());
        api = new Api();
        api.init();
        // context.getServletContext().addServlet("ApiServlet", api);
        tomcat.addServlet("", "ApiServlet", api);
        context.addServletMappingDecoded("/" + apiSuffix, "ApiServlet");
        tomcat.init();
        tomcat.start();
    }

    @AfterClass
    public static void shutDownTomcat() throws LifecycleException, IOException {
        tomcat.stop();
        FileUtils.deleteDirectory(destinationPath.toFile());
        FileUtils.deleteDirectory(baseDir.resolve("work").toFile());
    }

    @Before
    public void setClientUp() throws MalformedURLException {
        client = new Client(new URL("http", "localhost", TOMCAT_PORT, ""), apiSuffix);
    }

    @Test
    public void wakeRequestTest() throws Exception {
        client.launchAndWaitForInstance("i-765876");
    }
}
