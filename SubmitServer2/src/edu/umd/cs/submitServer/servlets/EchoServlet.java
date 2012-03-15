package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EchoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("URI:" + req.getRequestURI());
        out.println("request scheme " + req.getScheme());
        out.println("request protocol " + req.getProtocol());

        out.println("request name " + req.getServerName());
        out.println("request port " + req.getServerPort());
        out.println("remote host " + req.getRemoteHost());
        
        

        String auth = req.getAuthType();
        if (auth != null) 
            out.println("auth type " + auth);

        for (Cookie c : req.getCookies()) {
            out.println("Cookie name: " + c.getName());
            out.println("      value: " + c.getValue());
        }

        out.println("headers:");

        Enumeration<String> h = req.getHeaderNames();
        while (h.hasMoreElements()) {
            String header = h.nextElement();
            out.println("  " + header + ":" + req.getHeader(header));
        }

        Enumeration<String> i = req.getParameterNames();
        if (i.hasMoreElements()) {
            out.println("parameters:");

            while (i.hasMoreElements()) {
                String n = i.nextElement();
                out.println("  " + n + ": " + req.getParameter(n));
            }
        }

        out.close();

    }
}