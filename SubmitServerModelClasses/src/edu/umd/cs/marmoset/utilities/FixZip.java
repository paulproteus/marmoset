package edu.umd.cs.marmoset.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class FixZip {
	
	public static boolean hasProblem(byte[] zipContents)  throws IOException {
		return hasProblem(new ByteArrayInputStream(zipContents));
	}
	
	
	public static byte[] fixProblem(byte[] zipContents, int pk)  throws IOException {
		File tmpFile = writeToTempFile(zipContents, pk);
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		copyZipFile(tmpFile, result);
		tmpFile.delete();
		return result.toByteArray();
	}


	private static File writeToTempFile(byte[] zipContents, int pk)
			throws IOException, FileNotFoundException {
		File tmpFile = File.createTempFile(pk + "fixme", ".zip");
		OutputStream out = new FileOutputStream(tmpFile);
		out.write(zipContents);
		out.close();
		return tmpFile;
	}

	/**
	 * @param tmpFile
	 * @param result
	 * @throws ZipException
	 * @throws IOException
	 */
	private static void copyZipFile(File tmpFile, OutputStream result)
			throws ZipException, IOException {
		ZipOutputStream zOut = new ZipOutputStream(result);
		ZipFile zf = new ZipFile(tmpFile);
		Enumeration<? extends ZipEntry> e = zf.entries();
		byte [] buf = new byte[1024];
		
		while(e.hasMoreElements()) {
			ZipEntry entry = e.nextElement();
			zOut.putNextEntry(entry);
			InputStream in = zf.getInputStream(entry);
			while (true) {
				int bytes = in.read(buf);
				if (bytes < 0) break;
				zOut.write(buf, 0, bytes);
			}
			in.close();
			zOut.closeEntry();
		}
		zOut.close();
	}

	
	static boolean hasProblem(InputStream zipContents)  throws IOException {
		DataInputStream dis = new DataInputStream(zipContents);
		int header = readInt(dis);
		if (header != 0x4034b50) 
			return false;
		int versionNeeded = readShort(dis);
		
		int flag = readShort(dis);
		int method = readShort(dis);
		
		int time = readShort(dis);
		int date = readShort(dis);
		int crc32 = readInt(dis);
		int compressedSize = readInt(dis);
		int uncompressedSize = readInt(dis);
		if (flag != 0 || method != 8) return false;
		int n = readShort(dis);
		int extra = readShort(dis);
		skipFully(dis, n);
		skipFully(dis, extra);
		
		if (method == 0 ) 
			skipFully(dis,uncompressedSize);
		else
			skipFully(dis,compressedSize);
		
		int header2= readInt(dis);
		if (header2 == 0x4034b50 || header2 == 0x02014b50)
			return false;
		return true;
	}
	static int readShort(InputStream dis) throws IOException {
		int first = dis.read();
		int second = dis.read();
		return second << 8 | first;
	}
	static int readInt(InputStream dis) throws IOException {
		int first = dis.read();
		int second = dis.read();
		int third = dis.read();
		int fourth = dis.read() ;
		return fourth << 24 | third << 16 | second << 8 | first;
	}
	
	static void skipFully(DataInputStream is, long bytes) throws IOException {
		while (bytes > 0) {
			long a = is.skip(bytes);
			bytes -= a;
		}
	}
}
