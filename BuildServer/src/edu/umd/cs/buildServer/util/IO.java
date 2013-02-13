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
 * Created on Sep 1, 2004
 */
package edu.umd.cs.buildServer.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;

/**
 * IO operations.
 *
 * @author David Hovemeyer
 */
public abstract class IO {
	/**
	 * Copy all data from an input stream to an output stream.
	 *
	 * @param in
	 *            the InputStream
	 * @param out
	 *            the OutputStream
	 * @throws IOException
	 *             if an IO error occurs
	 */
	public static void copyStream(InputStream in, OutputStream out)
			throws IOException {
		copyStream(in, out, Integer.MAX_VALUE);
	}

	/**
	 * Copy all data from an input stream to an output stream.
	 *
	 * @param in
	 *            the InputStream
	 * @param out
	 *            the OutputStream
	 * @param length
	 *            the maximum number of bytes to copy
	 * @throws IOException
	 *             if an IO error occurs
	 */
	public static void copyStream(InputStream in, OutputStream out, int length)
			throws IOException {

		byte[] buf = new byte[4096];

		for (;;) {
			int readlen = Math.min(length, buf.length);
			int n = in.read(buf, 0, readlen);
			if (n < 0)
				break;
			out.write(buf, 0, n);
			length -= n;
		}
	}

	/**
	 * Read file from HTTP response body into a file.
	 *
	 * @param file
	 *            a File in the filesystem where the file should be stored
	 * @param method
	 *            the HttpMethod representing the request and response
	 * @throws IOException
	 *             if the complete file couldn't be downloaded
	 */
	public static void download(File file, HttpMethod method)
			throws IOException {
		InputStream in = null;
		OutputStream out = null;

		try {
			in = new BufferedInputStream(method.getResponseBodyAsStream());
			out = new BufferedOutputStream(new FileOutputStream(file));

			CopyUtils.copy(in, out);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * Monitor an input stream by copying all of its data to an output stream.
	 * Both streams are closed before the thread finishes.
	 *
	 * @param in
	 *            the input stream to monitor
	 * @param out
	 *            the output stream to copy input stream data to
	 * @return the monitor Thread
	 */
	public static Thread monitor(final InputStream in, final OutputStream out) {
		return new Thread() {
			@Override
			public void run() {
				try {
					IO.copyStream(in, out);
				} catch (IOException e) {
					// Ignore
				} finally {
					IO.closeSilently(in,out);
				}
			}
		};
	}

	/**
	 * Copy a file. Destination file is created if it doesn't already exist.
	 *
	 * @param source
	 *            source File
	 * @param dest
	 *            destination File
	 * @throws IOException
	 */
	public static void copyFile(File source, File dest) throws IOException {
		InputStream in = null;
		OutputStream out = null;

		try {
			in = new BufferedInputStream(new FileInputStream(source));
			out = new BufferedOutputStream(new FileOutputStream(dest));

			IO.copyStream(in, out);
		} finally {
			IO.closeSilently(in, out);
		}
	}

	public static void closeSilently(Closeable... args) {
		for(Closeable c : args)
		if (c != null)
		try {
			c.close();
		} catch (IOException e) {
			// ignore
		}
	}
	public static void closeSilently(ZipFile... args) {
        for(ZipFile c : args)
        if (c != null)
        try {
            c.close();
        } catch (IOException e) {
            // ignore
        }
    }
	
}
