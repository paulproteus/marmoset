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
 * Created on Feb 1, 2005
 */
package edu.umd.cs.buildServer.util;

import static edu.umd.cs.buildServer.ConfigurationKeys.HOSTNAME;
import static edu.umd.cs.buildServer.ConfigurationKeys.SUBMIT_SERVER_HANDLEBUILDSERVERLOGMESSAGE_PATH;
import static edu.umd.cs.buildServer.ConfigurationKeys.SUPPORTED_COURSE_LIST;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import edu.umd.cs.buildServer.BuildServerConfiguration;
import edu.umd.cs.buildServer.Configuration;
import edu.umd.cs.buildServer.MissingConfigurationPropertyException;

/**
 * This class uses contacts a servlet to log messages back to the submitServer.
 * It serializes and sends the entire LoggingEvent object. The location of the
 * submitServer and the path to the servlet are all contained in the config
 * file; thus a Configuration object must be passed to this appender so that it
 * can find the server.
 * 
 * @author jspacco
 * 
 */
public class ServletAppender extends AppenderSkeleton implements Appender {
    private static final int HTTP_TIMEOUT = 10 * 1000;
    private BuildServerConfiguration config;
    private boolean APPEND_TO_SUBMIT_SERVER = false;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent
     * )
     */
    @Override
    protected void append(LoggingEvent event) {
        if (!APPEND_TO_SUBMIT_SERVER)
            return;
        try {
            Throwable throwable = null;
            if (event.getThrowableInformation() != null) {
                String[] throwableStringRep = event.getThrowableStrRep();
                StringBuffer stackTrace = new StringBuffer();
                for (String stackTraceString : throwableStringRep) {
                    stackTrace.append(stackTraceString);
                }
                throwable = new Throwable(stackTrace.toString());
            }

            LoggingEvent newLoggingEvent = new LoggingEvent(event.getFQNOfLoggerClass(), event.getLogger(), event.getLevel(),
                    getConfig().getHostname() + ": " + event.getMessage(), throwable);

            HttpClient client = new HttpClient();
            client.setConnectionTimeout(HTTP_TIMEOUT);

            String logURL = config.getServletURL(SUBMIT_SERVER_HANDLEBUILDSERVERLOGMESSAGE_PATH);

            MultipartPostMethod post = new MultipartPostMethod(logURL);
            // add password

            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(sink);

            out.writeObject(newLoggingEvent);
            out.flush();
            // add serialized logging event object
            post.addPart(new FilePart("event", new ByteArrayPartSource("event.out", sink.toByteArray())));

            int status = client.executeMethod(post);
            if (status != HttpStatus.SC_OK) {
                throw new IOException("Couldn't contact server: " + status);
            }
        } catch (IOException e) {
            // TODO any way to log these without an infinite loop?
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    public boolean requiresLayout() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.Appender#close()
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    /**
     * @return Returns the config.
     */
    public BuildServerConfiguration getConfig() {
        return config;
    }

    /**
     * @param config
     *            The config to set.
     */
    public void setConfig(BuildServerConfiguration config) throws MissingConfigurationPropertyException {
        this.config = config;

    }
}
