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

package edu.umd.cs.marmoset.modelClasses;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.annotation.WillClose;
import javax.annotation.WillNotClose;

/**
 * @author jspacco
 * TODO replace with jakarta-commons-IO
 */
public class IO
{

	public static byte[] getBytes(InputStream is)
			throws IOException {
		return getBytes(is, 1024);
	}

	
	public static byte[] getBytes(InputStream is, int sizeInBytesEstimate)
			throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(
				sizeInBytesEstimate <= 0 ? 32 : sizeInBytesEstimate);
		copyStream(is, bytes);

		byte[] bytesForUpload = bytes.toByteArray();
		return bytesForUpload;
	}
	
    /**
     * Copy all data from an input stream to an output stream.
     * @param in the InputStream
     * @param out the OutputStream
     * @throws IOException if an IO error occurs
     */
    public static void copyStream(@WillNotClose InputStream in, @WillNotClose OutputStream out) throws IOException {
    	copyStream(in, out, Integer.MAX_VALUE);
    }

    /**
     * Copy all data from an input stream to an output stream.
     * @param in the InputStream
     * @param out the OutputStream
     * @param length the maximum number of bytes to copy
     * @throws IOException if an IO error occurs
     */
    public static void copyStream(@WillNotClose InputStream in, @WillNotClose  OutputStream out, int length)
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

    public static Process execAndDumpToStringBuffer(String cmd[], final StringBuffer out, final StringBuffer err) throws IOException
    {
        final Process proc = Runtime.getRuntime().exec(cmd);
        
        Thread t = new Thread()
        { 
            @Override
			public void run()
            {
            	BufferedReader reader=null;
            	try {
            		reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    String line;
                    while ( (line = reader.readLine()) != null )
                    {
                        out.append(line +"\n");
                    }
                }
                catch (IOException e) {
                    System.err.println("Exception getting output stream from proc process:" + e);
                } finally {
                	try {
                		if (reader != null) reader.close();
                	} catch (IOException ignore) {
                		// ignore
                	}
                }
            }
        };
        t.start();
        
        Thread t2 = new Thread()
        { 
            @Override
			public void run()
            {
            	BufferedReader reader=null;
            	try { 
                    reader= new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                    String line;
                    while ( (line = reader.readLine()) != null )
                    {
                        err.append(line +"\n");
                    }
                }
                catch (IOException e) {
                    System.err.println("Exception getting output stream from proc process:" + e);
                } finally {
                	try {
                		if (reader!=null) reader.close();
                	} catch (IOException ignore) {
                		// ignore
                	}
                }
                
                
            }
        };
        t2.start();
        
        return proc;
    }
    
    public static void closeInputStreamAndIgnoreIOException(@WillClose InputStream in)
    {
    	try {
    		if (in != null) in.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
    
    public static void closeOutputStreamAndIgnoreIOException(@WillClose OutputStream out)
    {
    	try {
    		if (out != null) out.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
    
    public static void closeReaderAndIgnoreIOException(@WillClose Reader reader)
    {
    	try {
    		if (reader != null) reader.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
    
    public static void closeWriterAndIgnoreIOException(@WillClose Writer writer)
    {
    	try {
    		if (writer != null) writer.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
    public static void closeAndIgnoreIOException(@WillClose Closeable closeable)
    {
    	try {
    		if (closeable != null) closeable.close();
    	} catch (IOException ignore) {
    		// ignore
    	}
    }
}
