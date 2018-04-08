package com.github.vatbub.awsec2wakelauncher;

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


import com.github.vatbub.common.core.StringCommon;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

public class Main extends HttpServlet{
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();

        try {
            String jsp = StringCommon.fromFile(new File(Main.class.getResource("/src/main/webapp/index.jsp").toURI()));
            writer.write(jsp);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
