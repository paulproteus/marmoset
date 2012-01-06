/**
 * Marmoset: an automated snapshot, submission and testing system
 * Copyright (C) 2005, University of Maryland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Email: marmoset-discuss@cs.umd.edu
 *
 * Snail mail:
 * The Marmoset Project
 * Attention: Dr. Bill Pugh
 * Dept. of Computer Science
 * Univ. of Maryland
 * College Park, MD 20742
 */

/*
 * Created on Jan 24, 2005
 *
 */
package edu.umd.cs.submit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * @author pugh
 *
 */
public class CommandLineSubmit {

    public static final String VERSION = "0.1.2";
    public static final int HTTP_TIMEOUT = Integer.getInteger("HTTP_TIMEOUT", 30).intValue()*1000;

    public static void main(String[] args) {
        try {
			Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(),
			        443);
			Protocol.registerProtocol("easyhttps", easyhttps);
			File submitFile = new File(".submit");
			File submitUserFile = new File(".submitUser");
			File submitIgnoreFile = new File(".submitIgnore");
			File cvsIgnoreFile = new File(".cvsignore");

			if (!submitFile.canRead()) {
			    System.out
			            .println("Must perform submit from a directory containing a \".submit\" file");
			    System.exit(1);
			}

			Properties p = new Properties();
            p.load(new FileInputStream(submitFile));
			String submitURL = p.getProperty("submitURL");
			if (submitURL == null) {
			    System.out.println(".submit file does not contain a submitURL");
			    System.exit(1);
			}
			String courseName = p.getProperty("courseName");
			String semester = p.getProperty("semester");
			String projectNumber = p.getProperty("projectNumber");
			System.out.println("Submitting project " + projectNumber + " for " + courseName);

			FilesToIgnore ignorePatterns = new FilesToIgnore();
			addIgnoredPatternsFromFile(cvsIgnoreFile, ignorePatterns);
			addIgnoredPatternsFromFile(submitIgnoreFile, ignorePatterns);

			FindAllFiles find = new FindAllFiles(submitFile, ignorePatterns.getPattern());
			Collection files = find.getAllFiles();

			Properties userProps = new Properties();
			if (submitUserFile.canRead()) {
			    userProps.load(new FileInputStream(submitUserFile));
			}
			if (userProps.getProperty("cvsAccount") == null
			        && userProps.getProperty("classAccount") == null
			        || userProps.getProperty("oneTimePassword") == null) {
			    System.out
			            .println("We need to authenticate you and create a .submitUser file so you can submit from this directory");
			    System.out.println("Please enter your Submit Server User ID and Password");
			    System.out.print("User ID: ");
			    System.out.flush();
			    Console console = System.console();
	            String campusUID = console.readLine();
	            System.out.println("Password: " );
			    String uidPassword = new String(console.readPassword());
			    System.out.println("Thanks!");
			    System.out.println("Preparing for submission. Please wait...");

			    int index = submitURL.indexOf("/eclipse/");
			    String url = submitURL.substring(0, index);
			    url += "/eclipse/NegotiateOneTimePassword";
			    PostMethod post = new PostMethod(url);

			    post.addParameter("campusUID", campusUID);
			    post.addParameter("uidPassword", uidPassword);
			    post.addParameter("courseName", courseName);
			    post.addParameter("semester", semester);
			    post.addParameter("projectNumber", projectNumber);

			    HttpClient client = new HttpClient();
			    client.setConnectionTimeout(HTTP_TIMEOUT);

			    // System.out.println("Preparing to execute method");
			    int status = client.executeMethod(post);
			    // System.out.println("Post finished with status: " +status);

			    if (status != HttpStatus.SC_OK) {
			        throw new HttpException(
			                "Unable to negotiate one-time password with the server: "
			                        + post.getResponseBodyAsString());
			    }

			    InputStream inputStream = post.getResponseBodyAsStream();
			    BufferedReader userStream = new BufferedReader(new InputStreamReader(
			            inputStream));
			    PrintWriter newUserProjectFile = new PrintWriter(new FileWriter(
			            submitUserFile));
			    while (true) {
			        String line = userStream.readLine();
			        if (line == null)
			            break;
			        // System.out.println(line);
			        newUserProjectFile.println(line);
			    }
			    newUserProjectFile.close();
			    if (!submitUserFile.canRead()) {
			        System.out.println("Can't generate or access " + submitUserFile);
			        System.exit(1);
			    }
			    userProps.load(new FileInputStream(submitUserFile));
			}

			// ========================== assemble zip file in byte array
			// ==============================

			System.out.println();
			System.out.println("Submitting the following files");
			ByteArrayOutputStream bytes = new ByteArrayOutputStream(4096);
			byte[] buf = new byte[4096];
			ZipOutputStream zipfile = new ZipOutputStream(bytes);
			zipfile.setComment("zipfile for CommandLineTurnin, version " + VERSION);
			for (Iterator i = files.iterator(); i.hasNext();) {
			    File resource = (File) i.next();
			    if (resource.isDirectory())
			        continue;
			    String relativePath = resource.getCanonicalPath().substring(
			            find.rootPathLength + 1);
			    System.out.println(relativePath);
			    ZipEntry entry = new ZipEntry(relativePath);
			    entry.setTime(resource.lastModified());

			    zipfile.putNextEntry(entry);
			    InputStream in = new FileInputStream(resource);
			    try {
			        while (true) {
			            int n = in.read(buf);
			            if (n < 0)
			                break;
			            zipfile.write(buf, 0, n);
			        }
			    } finally {
			        in.close();
			    }
			    zipfile.closeEntry();

			} // for each file
			zipfile.close();

			MultipartPostMethod filePost = new MultipartPostMethod(p.getProperty("submitURL"));

			p.putAll(userProps);
			// add properties
			for (Iterator submitProperties = p.entrySet().iterator(); submitProperties
			        .hasNext();) {
			    Map.Entry e = (Map.Entry) submitProperties.next();
			    String key = (String) e.getKey();
			    String value = (String) e.getValue();
			    if (!key.equals("submitURL"))
			        filePost.addParameter(key, value);
			}
			filePost.addParameter("submitClientTool", "CommandLineTool");
			filePost.addParameter("submitClientVersion", VERSION);
			byte[] allInput = bytes.toByteArray();
			filePost.addPart(new FilePart("submittedFiles", new ByteArrayPartSource(
			        "submit.zip", allInput)));
			// prepare httpclient
			HttpClient client = new HttpClient();
			client.setConnectionTimeout(HTTP_TIMEOUT);
			int status = client.executeMethod(filePost);
			System.out.println(filePost.getResponseBodyAsString());
			if (status != HttpStatus.SC_OK)
			    System.exit(1);
		} catch (Exception e) {
			System.out.println();
			System.out.println("An Error has occured during submission!");
			System.out.println();
			System.out.println("[DETAILS]");
			System.out.println(e.getMessage());
			System.out.println("For assistance, please contact the administrator at " +
					"submit-help@umiacs.umd.edu with details of the error.");
			System.out.println();
		}
    }

    /**
     * @param submitIgnoreFile
     * @param ignorePatterns
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void addIgnoredPatternsFromFile(File submitIgnoreFile,
            FilesToIgnore ignorePatterns) throws FileNotFoundException, IOException {
        if (submitIgnoreFile.canRead()) {
            BufferedReader ignoreContents = new BufferedReader(new FileReader(
                    submitIgnoreFile));
            while (true) {
                String ignoreMe = ignoreContents.readLine();
                if (ignoreMe == null)
                    break;
                ignorePatterns.addPattern(ignoreMe);
            }
        }
    }
}
