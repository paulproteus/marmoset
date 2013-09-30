package edu.umd.cs.marmoset.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;

public class Checksums {



	public static MessageDigest getDigest() {
		try {
			return MessageDigest.getInstance("sha-1");
		} catch (NoSuchAlgorithmException e) {
			AssertionError e2 = new AssertionError("Can't get sha-1 digest");
			e2.initCause(e);
			throw e2;
		}
	}
	/**
	 * Converts an array of bytes into a hexadecimal string.
	 * @param bytes the array of bytes
	 * @return the hexadecimal string representation of the byte array
	 */
	public static String byteArrayToHexString(byte[] bytes)
	{
	    StringBuffer sb = new StringBuffer(bytes.length * 2);
	    for (int i = 0; i < bytes.length; i++){
	      int v = bytes[i] & 0xff;
	      if (v < 16) {
	        sb.append('0');
	      }
	      sb.append(Integer.toHexString(v));
	    }
	    return sb.toString().toLowerCase();
	}
	public static String getChecksum(byte[] bytes) {
			MessageDigest digest = getDigest();
			byte [] checksum = digest.digest(bytes);
			return byteArrayToHexString(checksum);
	}
	public static String getChecksum(byte[] bytes1, boolean b, byte[] bytes2) {
        MessageDigest digest = getDigest();
        digest.reset();
        digest.update(bytes1);
        if (b)
            digest.update((byte)1);
        digest.update(bytes2);
        byte [] checksum = digest.digest();
        return byteArrayToHexString(checksum);
}
	/**
	 * Computes the MD5SUM of a list of files.
	 * TODO This method should go into a separate class of static utilities.
	 * @param fileList the list of files
	 * @return the MD5SUM (as a hexadecimal String) of the md5sum of a given list of files.
	 * @throws NoSuchAlgorithmException thrown when the md5sum algorithm is not available
	 * @throws FileNotFoundException if any of the files in the list cannot be found
	 * @throws IOException if any of the files in the list cannot be read
	 */
	public static byte[] checksum(Collection<File> fileList)
	throws IOException
	{
	    MessageDigest digest = getDigest();
	    for (Iterator<File> ii=fileList.iterator(); ii.hasNext();)
	    {
	        File file = ii.next();
	        FileInputStream fis = new FileInputStream(file);
	        byte[] bytes = new byte[2048];
	        int numRead;
	        while ((numRead = fis.read(bytes)) != -1)
	        {
	            digest.update(bytes, 0, numRead);
	        }
	        fis.close();
	    }
	    return digest.digest();
	}
	public static String checksumAsHexString(Collection<File> fileList)
	throws NoSuchAlgorithmException, IOException
	{
	    return byteArrayToHexString(checksum(fileList));
	}

}
