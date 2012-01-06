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

/**
 * @author jspacco
 */
package edu.umd.cs.marmoset.utilities;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.CopyUtils;
import org.apache.log4j.Logger;

import edu.umd.cs.marmoset.modelClasses.Submission;

/**
 * MarmosetUtilities
 * @author jspacco
 */
public final class MarmosetUtilities
{
    private MarmosetUtilities() {}

    public static <T> Map<T, Boolean> setAsMap(final Set<T> set) {
    		return new Map<T, Boolean>() {

				@Override
				public void clear() {
					set.clear();

				}

				@Override
				public boolean containsKey(Object arg0) {
					return set.contains(arg0);
				}

				@Override
				public boolean containsValue(Object arg0) {
					throw new UnsupportedOperationException();
				}

				@Override
				public Set<java.util.Map.Entry<T, Boolean>> entrySet() {
					throw new UnsupportedOperationException();
				}

				@Override
				public Boolean get(Object arg0) {
					return set.contains(arg0);
				}

				@Override
				public boolean isEmpty() {
					return set.isEmpty();
				}

				@Override
				public Set<T> keySet() {
					return set;
				}

				@Override
				public Boolean put(T arg0, Boolean arg1) {
					Boolean result = set.contains(arg0);
					if (arg1)
						set.add(arg0);
					else
						set.remove(arg0);
					return result;
				}

				@Override
				public void putAll(Map<? extends T, ? extends Boolean> arg0) {
					throw new UnsupportedOperationException();

				}

				@Override
				public Boolean remove(Object arg0) {
					Boolean result = set.contains(arg0);
					set.remove(arg0);
					return result;
				}

				@Override
				public int size() {
					return set.size();
				}

				@Override
				public Collection<Boolean> values() {
					throw new UnsupportedOperationException();
				}
    		};
    }

    // [NAT P002]
    // Generage a random password
	private static SecureRandom rng = new SecureRandom();

	private static long nextRandomLong() {
			return rng.nextLong();
	}

	private static long nextRandomNonnegativeLong() {
			return rng.nextLong() & Long.MAX_VALUE;
	}
	/**
	 * @return a random password
	 */
	public static String nextRandomPassword() {
		String s = Long.toHexString(nextRandomLong());
		return s.substring(s.length()-8);
	}
    // [end NAT P002]


	public static String nextLongRandomPassword() {
		String s = Long.toString(nextRandomNonnegativeLong(), 36);
		String t = Long.toString(nextRandomNonnegativeLong(), 36);
		return s+t;
		}


    /**
     * Convert the string-representation of a stackTraceElement back to a StackTraceElement object.
     * @param stackTraceLine The string rep of a stackTraceElement.
     * @return The corresponding StackTraceElement object; null if a StackTraceElement cannot be
     *  reconstructed from the given string.
     */
    public static StackTraceElement parseStackTrace(String stackTraceLine)
    {
        // Try with source info
        String regexp="(.*)\\.([\\w<>]+)\\((\\w+\\.java):(\\d+)\\)";
        Pattern pattern=Pattern.compile(regexp);
        Matcher matcher=pattern.matcher(stackTraceLine);
        if (matcher.matches()) {
            String className=matcher.group(1);
            String methodName=matcher.group(2);
            String fileName=matcher.group(3);
            String s=matcher.group(4);
            int lineNumber=Integer.parseInt(s);
            return new StackTraceElement(className,methodName,fileName,lineNumber);
        }

        // Try without source info
        String regexpUnknown="(.*)\\.([\\w<>]+)\\(Unknown Source\\)";
        pattern=Pattern.compile(regexpUnknown);
        matcher=pattern.matcher(stackTraceLine);
        if (matcher.matches()) {
            String className=matcher.group(1);
            String methodName=matcher.group(2);
            String fileName=null;
            int lineNumber=-1;
            return new StackTraceElement(className,methodName,fileName,lineNumber);
        }

        // Try for native methods
        String regexpNative="(.*)\\.([\\w<>]+)\\(Native Method\\)";
        pattern=Pattern.compile(regexpNative);
        matcher=pattern.matcher(stackTraceLine);
        if (matcher.matches()) {
            String className=matcher.group(1);
            String methodName=matcher.group(2);
            String fileName=null;
            int lineNumber=-2;
            return new StackTraceElement(className,methodName,fileName,lineNumber);
        }

        //throw new IllegalStateException("Unable to parse stack trace: " +stackTraceLine);
        return null;
    }

    /**
     * @param conn
     * @param submissionPK
     * @param filename
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void fixSubmissionZipfile(Connection conn,
    		@Submission.PK int submissionPK, String filename) throws SQLException, FileNotFoundException, IOException
    {
        Submission submission=Submission.lookupBySubmissionPK(submissionPK,conn);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        FileInputStream fis=new FileInputStream(filename);
        CopyUtils.copy(fis,baos);
        byte[] bytes=baos.toByteArray();

        submission.setArchiveForUpload(bytes);
        submission.updateCachedArchive(conn);
    }

    /**
     * @param param
     * @return
     */
    public static boolean isTrue(String param)
    {
        param = param.toUpperCase();
        if (param.equals("TRUE") || param.equals("YES"))
            return true;
        return false;
    }

    /**
     * Uses the kill command to kill this process as a group leader with: <br>
     * kill -9 -&lt;pid&gt;
     * <p>
     * If kill -9 -&lt;pid&gt; fails, then this method will call
     * @param process
     */
    public static void destroyProcessGroup(Process process, Logger log)
    {
        int pid=0;
        try {
            pid = getPid(process);

            log.debug("PID to be killed = " +pid);

            //String command = "kill -9 -" +pid;
            String command = "kill -9 " +pid;

            String[] cmd = command.split("\\s+");

            Process kill = Runtime.getRuntime().exec(cmd);
            log.warn("Trying to kill the process group leader: " +command);
            kill.waitFor();
        } catch (IOException e) {
            // if we can't execute the kill command, then try to destroy the process
            log.warn("Unable to execute kill -9 -" +pid+ "; now calling process.destroy()");
       	} catch (InterruptedException e) {
            log.error("kill -9 -" +pid+ " process was interrupted!  Now calling process.destroy()");
        } catch (IllegalAccessException e) {
            log.error("Illegal field access to PID field; calling process.destroy()", e);
        } catch (NoSuchFieldException e) {
            log.error("Cannot find PID field; calling process.destroy()", e);
        } finally {
            // call process.destroy() whether or not "kill -9 -<pid>" worked
            // in order to maintain proper internal state
            process.destroy();
        }
    }

    /**
     * Uses reflection to extract the pid, a private field of the private class UNIXProcess.
     * This will fail on any non-Unix platform that doesn't use UNIXProcess.  It may
     * fail if the UNIXProcess class changes at all.  It may fail anyway for unpredictable
     * reasons.
     * @param process The process
     * @return the pid of this process
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static int getPid(Process process)
    throws NoSuchFieldException, IllegalAccessException
    {
        Class<? extends Process> processClass = process.getClass();
        Field pidField = processClass.getDeclaredField("pid");
        pidField.setAccessible(true);
        return pidField.getInt(process);
    }

    public static String commandToString(List<String> args) {
    	StringBuffer buf = new StringBuffer();
    	for (Iterator<String> i = args.iterator(); i.hasNext(); ) {
    		String arg = i.next();
    		if (buf.length() > 0)
    			buf.append(' ');
    		buf.append(arg);
    	}
    	return buf.toString();
    }

    public static String commandToString(String[] command)
    {
        StringBuffer buf=new StringBuffer();
        for (String s : command) {
            buf.append(s + " ");
        }
        return buf.toString();
    }

    public static boolean stringEquals(String s1, String s2) {
    	if (s1 != null) {
    		if (s2 != null)
    			return s1.equals(s2);
    		else
    			return false;
    	} else {
    		return s2 == null;
    	}
    }

    public static int hashString(String s) {
    	return s == null ? 0 : s.hashCode();
    }

    public static Integer toIntegerOrNull(String s)
    throws NumberFormatException {
        return s == null ? null : Integer.valueOf(s);
    }
}

