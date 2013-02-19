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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.CopyUtils;



/**
 * Take a number of smaller zip files and aggregate them
 * into a single large zip file.
 *
 * @author David Hovemeyer
 */
public class ZipFileAggregator {
	/**
	 * This kind of exception is thrown if we try to
	 * add an invalid zip file to the aggregate.
	 */
	public static class BadInputZipFileException extends Exception {
		public BadInputZipFileException(String msg, Throwable e) {
			super(msg, e);
		}
	}

//	/**
//	 * Delegating InputStream that never closes the underlying
//	 * stream.
//	 */
//	private static class NoCloseInputStream extends DataInputStream {
//		public NoCloseInputStream(InputStream in) {
//			super(in);
//		}
//
//		public void close() {
//			// do nothing
//		}
//	}

	// Fields
	private ZipOutputStream zipOutput;

	/**
	 * Constructor.
	 * @param out the OutputStream to write aggregated zip output to
	 */
	public ZipFileAggregator(OutputStream out) {
		this.zipOutput = new ZipOutputStream(out);
	}

	/**
	 * Close the zip output stream.
	 * This needs to be called after all input zip files
	 * have been added.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (zipOutput != null) {
			zipOutput.close();
		}
	}

	public void addPlainFileFromBytes(String name, byte[] bytes)
	throws IOException, BadInputZipFileException
	{
		// Create output entry
		ZipEntry outputEntry = new ZipEntry(name);
		zipOutput.putNextEntry(outputEntry);

		zipOutput.write(bytes);
		zipOutput.closeEntry();
	}

	public void addFileFromBytes(String dirName, byte[] bytes)
	throws IOException, BadInputZipFileException
	{
		addFileFromBytes(dirName, -1, bytes);
	}

	public void addFileFromBytes(String dirName, long time, byte[] bytes)
	throws IOException, BadInputZipFileException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		if (isValidZipfile(bytes)) {
	    	addZipFileFromInputStream(dirName, time, bais);
	    } else {
	    	addFileFromInputStream(dirName, time, bais);
	    }
	}

	private void addFileFromInputStream(String dirName, long time, InputStream inputStream)
	throws IOException
	{
		// Prepend directory name
		String name = dirName + "/nonzipfile";

		// Create output entry
		ZipEntry outputEntry = new ZipEntry(name);
		if (time > 0L) outputEntry.setTime(time); // only add valid times
		zipOutput.putNextEntry(outputEntry);

		// Copy zip input to output
        CopyUtils.copy(inputStream, zipOutput);

		zipOutput.closeEntry();
	}

	/**
	 * Adds a zipfile from an inputStream to the aggregate zipfile.
	 *
	 * @param dirName name of the top-level directory that will be
	 * @param inputStream the inputStream to the zipfile created in the aggregate zip file
	 * @throws IOException
	 * @throws BadInputZipFileException
	 */
	private void addZipFileFromInputStream(String dirName, long time, InputStream inputStream)
	throws IOException, BadInputZipFileException
	{
		// First pass: just scan through the contents of the
		// input file to make sure it's really valid.
	    ZipInputStream zipInput=null;
		try {
			zipInput = new ZipInputStream(
					new BufferedInputStream(inputStream));
			ZipEntry entry;
			while ((entry = zipInput.getNextEntry()) != null) {
				zipInput.closeEntry();
			}
		} catch (IOException e) {
			throw new BadInputZipFileException(
					"Input zip file seems to be invalid", e);
		} finally {
			if (zipInput != null) zipInput.close();
		}

		// FIXME: It is probably wrong to call reset() on any input stream; for my application the inputStream will only ByteArrayInputStream or FileInputStream
		inputStream.reset();

		// Second pass: read each entry from the input zip file,
		// writing it to the output file.
		zipInput = null;
		try {
			// add the root directory with the correct timestamp
			if (time > 0L) {
				// Create output entry
				ZipEntry outputEntry = new ZipEntry(dirName + "/");
				outputEntry.setTime(time);
				zipOutput.closeEntry();
			}

			zipInput = new ZipInputStream(
					new BufferedInputStream(inputStream));
			ZipEntry entry;
			while ((entry = zipInput.getNextEntry()) != null) {
				try {
					String name = entry.getName();
					// Convert absolute paths to relative
					if (name.startsWith("/")) {
						name = name.substring(1);
					}

					// Prepend directory name
					name = dirName + "/" + name;

					// Create output entry
					ZipEntry outputEntry = new ZipEntry(name);
					if (time > 0L) outputEntry.setTime(time);
					zipOutput.putNextEntry(outputEntry);

					// Copy zip input to output
	                CopyUtils.copy(zipInput, zipOutput);
				} catch (Exception zex) {
					// ignore it
				} finally {
					zipInput.closeEntry();
					zipOutput.closeEntry();
				}
			}
		} finally {
			if (zipInput != null) {
				try {
					zipInput.close();
				} catch (IOException ignore) {
					// Ignore
				}
			}
		}
	}

	/**
	 * Add a zip file to be added to the aggregate.
	 *
	 * @param dirName   name of the top-level directory that will be
	 *                  created in the aggregate zip file
	 * @param inputFile the zip file to be added
	 *
	 * @throws BadInputZipFileException if the input file seems
	 *                     to be invalid; in this case,
	 *                     we do <em>not</em> write any
	 *                     output to the output file, and
	 *                     writing of other zip files to
	 *                     the aggregate may continue
	 * @throws IOException if an error occurs while copying the
	 *                     input entries to the aggregate output;
	 *                     in this case, the aggregate output is
	 *                     probably corrupted or incomplete
	 */
	public void addFile(String dirName, File inputFile)
	throws IOException, BadInputZipFileException
	{
		long time = -1L;

		// safely check the time in the file
		try {
			time = inputFile.lastModified();
		} catch (Exception e) {}

		if (time <= 0L) time = -1L;

	    FileInputStream fis = null;
	    try {
	    	fis=new FileInputStream(inputFile);
	    	addZipFileFromInputStream(dirName, time, fis);
	    } finally {
	    	if (fis != null) fis.close();
	    }
	}


	// Just for testing.
	public static void main(String[] args) throws Exception {
		FileInputStream fis = new FileInputStream("inputfile1.zip");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CopyUtils.copy(fis, baos);

		byte[] byteArr = baos.toByteArray();
		fis.close();

		fis = new FileInputStream("inputfile2.zip");
		baos = new ByteArrayOutputStream();
		CopyUtils.copy(fis,baos);

		byteArr = baos.toByteArray();
		fis.close();

//		if (args.length < 2) {
//			System.err.println("Usage: " +
//					ZipFileAggregator.class.getName() +
//					" <output file> <file1, file2...>");
//			System.exit(1);
//		}
//
//		ZipFileAggregator agg=null;
//		try {
//			agg=new ZipFileAggregator(new FileOutputStream(args[0]));
//			for (int i = 1; i < args.length; ++i) {
//				File inputFile = new File(args[i]);
//				System.out.println("Adding " + args[i]);
//				try {
//					agg.addFile(args[i], inputFile);
//				} catch (ZipFileAggregator.BadInputZipFileException e) {
//					System.out.println("Recoverable exception: " + e);
//					System.out.println("Continuing...");
//				}
//			}
//		} finally {
//			if (agg != null) agg.close();
//		}
//		System.out.println("Done!");
	}

	public static boolean isValidZipfile(byte[] byteArr)
	{
		ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(byteArr));
		boolean areBytesAZipfile=false;
		try {
			while (true) {
				ZipEntry entry = zipIn.getNextEntry();
				if (entry == null)
					break;
				// We need to read at least one valid entry for this to be a valid zipfile
				areBytesAZipfile=true;
			}
		} catch (Throwable e) {
			return false;
		}
		return areBytesAZipfile;
	}

}
