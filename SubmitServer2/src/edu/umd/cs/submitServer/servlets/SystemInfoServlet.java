/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

/*
 * Created on Jan 15, 2005
 *
 * @author jspacco
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.ServerError;
import edu.umd.cs.marmoset.utilities.SystemInfo;


public class SystemInfoServlet extends SubmitServerServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    
    String load = SystemInfo.getSystemLoad();
    double loadAverage = SystemInfo.getLoadAverage();
    if (!SystemInfo.isGood(load)) {
      Logger log = getSubmitServerServletLog();
      log.warn(load);
      Connection conn = null;
      if (loadAverage > 6.0)
        try {
          conn = getConnection();
          ServerError.insert(conn, ServerError.Kind.OVERLOADED, load, this.getClass().getSimpleName(),
              request.getRequestURI(), null);
        } catch (SQLException e) {
          handleSQLException(e);
          throw new ServletException(e);
        } finally {
          releaseConnection(conn);
        }

    }
    response.getWriter().println(load);

  }

}
