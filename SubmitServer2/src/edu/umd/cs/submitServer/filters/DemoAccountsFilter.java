package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.WebConfigProperties;

public class DemoAccountsFilter extends SubmitServerFilter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String demoAccounts = WebConfigProperties.get().getProperty(
                "authentication.demoAccounts");

        if (!Strings.isNullOrEmpty(demoAccounts)) {
            Connection conn = null;
            try {
                conn = getConnection();

                String[] accts = demoAccounts.trim().split(",");
                List<Student> demoStudents = new ArrayList<Student>(
                        accts.length);
                for (String loginName : accts) {
                    Student s = Student.lookupByLoginName(loginName, conn);
                    if (s != null)
                        demoStudents.add(s);

                }
                request.setAttribute("demoAccounts", demoStudents);

            } catch (SQLException e) {
                throw new ServletException(e);
            } finally {
                releaseConnection(conn);
            }
        }

        chain.doFilter(request, response);
    }

}
