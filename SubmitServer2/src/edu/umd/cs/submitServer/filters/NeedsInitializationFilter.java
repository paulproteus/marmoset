package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Student;

public class NeedsInitializationFilter extends SubmitServerFilter {

    static volatile boolean initialized = false;
    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) arg0;
       if (!initialized) {
            Connection conn = null;
            try  {
                conn = getConnection();
                if (Student.existAny(conn)) 
                    initialized = true;
                else 
                    request.setAttribute(INITIALIZATION_NEEDED,true);
            } catch (SQLException e) {
                getSubmitServerFilterLog().warn(e);
            } finally {
                Queries.close(conn);
            }
        }
        chain.doFilter(request, arg1);
        
    }

}
