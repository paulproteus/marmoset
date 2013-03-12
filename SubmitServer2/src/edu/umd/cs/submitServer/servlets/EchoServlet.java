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

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
      doGet(req, resp);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        String delayString = req.getParameter("delay");
        PrintWriter out = resp.getWriter();

        int delay = 60;
        if (delayString != null)
          delay = Integer.parseInt(delayString.trim());
        try {
        Thread.sleep(delay * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace(out);
      }
       
        out.println("      URI:" + req.getRequestURI());
        out.println("      URL:" + req.getRequestURL().toString());
        out.println("   scheme: " + req.getScheme());
        out.println(" protocol: " + req.getProtocol());
        out.println("   method: " + req.getMethod());
        out.println("     name: " + req.getServerName());
        out.println("     port: " + req.getServerPort());
        out.println("     host: " + req.getRemoteHost());
        out.println("  charset: " + req.getCharacterEncoding());
        out.println("     type: " + req.getContentType());
        out.println("    delay: " + delay);
        
        
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
                String value = req.getParameter(n);
                out.println("  " + n + ": " + value);
                if (n.equals("title")) {
                  byte b[] = value.getBytes();
                  for(int j = 0; j < b.length; j++) {
                    int c = b[j] & 0xff;
                    if (c <= 127)
                      out.print(" " + (char)c);
                    else 
                      out.printf("%2x", c);
                    
                  }
                  out.println();
                  
                }
            }
        }

        out.close();

    }
}