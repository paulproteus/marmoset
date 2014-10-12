package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.Submission.BuildStatus;
import edu.umd.cs.marmoset.utilities.SystemInfo;

public class ServerStatusFilter extends SubmitServerFilter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        Connection conn = null;
        try {

            conn = getConnection();

            int numNewSubmissions = Submission.numSubmissions(conn, BuildStatus.NEW);
            request.setAttribute("newSubmissions", numNewSubmissions);
            request.setAttribute("systemLoad", SystemInfo.getSystemLoad());
            
  

        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }

        chain.doFilter(request, response);
    }

}
