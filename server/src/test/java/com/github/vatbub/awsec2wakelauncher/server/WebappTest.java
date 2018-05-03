package com.github.vatbub.awsec2wakelauncher.server;

/*-
 * #%L
 * awsec2wakelauncher.unittestcommons
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


import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class WebappTest {
    private static Tomcat tomcat;
    private static Path destinationPath;
    private static Path webappsPath;
    private static Path baseDir;

    public static void startServer(int tomcatPort, String contextPath, String webXmlPath) throws LifecycleException, IOException, ServletException {
        List<String> relativeFolders = new ArrayList<>();
        relativeFolders.add("src");
        relativeFolders.add("main");
        relativeFolders.add("webapp");

        tomcat = new Tomcat();
        tomcat.setBaseDir(".");
        tomcat.setPort(tomcatPort);

        // copy src/main/webapp to webapps/src/main/webapp
        baseDir = tomcat.getServer().getCatalinaHome().toPath();
        Path sourcePath = baseDir.getParent().resolve("server");
        webappsPath = baseDir.resolve("webapps");
        destinationPath = webappsPath;
        for (String folder : relativeFolders) {
            sourcePath = sourcePath.resolve(folder);
            destinationPath = destinationPath.resolve(folder);
        }

        FileUtils.copyDirectory(sourcePath.toFile(), destinationPath.toFile());

        /* There needs to be a symlink to the current dir named 'webapps' */
        tomcat.addWebapp(contextPath, webXmlPath);
        tomcat.init();
        tomcat.start();
    }

    @AfterClass
    public static void shutDownTomcat() throws LifecycleException, IOException {
        tomcat.stop();
        FileUtils.deleteDirectory(destinationPath.toFile());
        FileUtils.deleteDirectory(webappsPath.toFile());
        FileUtils.deleteDirectory(baseDir.resolve("work").toFile());
    }

    @SuppressWarnings("unused")
    public static Tomcat getTomcat() {
        return tomcat;
    }
}
