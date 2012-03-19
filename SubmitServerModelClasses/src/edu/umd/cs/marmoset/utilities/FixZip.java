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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.CheckForNull;

import com.google.common.base.Strings;

public class FixZip {

    private static boolean strictSuffix(String suffix, String full) {
        return full.length() > suffix.length() && suffix.length() >= 0
                && full.endsWith(suffix);
    }

    public static boolean anySlashes(Collection<String> c) {
        for (String s : c)
            if (s.indexOf('/') >= 0)
                return true;
        return false;
    }

    public static String simpleName(String name) {
        int i = name.lastIndexOf('/');
        if (i == -1)
            return name;
        return name.substring(i + 1);
    }

    public static Map<String, String> getFullNames(Collection<String> c) {
        Map<String, String> result = new HashMap<String, String>();
        HashSet<String> dups = new HashSet<String>();

        for (String s : c) {
            String simpleName = simpleName(s);
            if (dups.contains(simpleName))
                continue;
            if (result.containsKey(simpleName)) {
                result.remove(simpleName);
                dups.add(simpleName);
            } else
                result.put(simpleName, s);
        }
        return result;
    }

    public static boolean ignoredName(String name) {
        String simpleName = simpleName(name);
        if (simpleName.isEmpty())
            return true;
        if (name.contains("CVS/") || name.contains(".svn/"))
            return true;
        if (simpleName.endsWith("~"))
            return true;
        if (name.contains("__MACOSX"))
            return true;
        return false;
    }

    public static byte[] discardIgnoredFiles(byte[] zipContents) {
        AdjustName adjuster = new AdjustName() {
            @Override
            public String adjustName(String name) {
                if (ignoredName(name))
                    return null;
                return name;
            }
        };
        return adjustNames(adjuster, zipContents);
    }

    public static String getDirectoryPrefix(String path) {
        if (path == null) return null;
        int i = path.indexOf("/");
        if (i == -1)
            return "";
        return path.substring(0,i+1);
    }
    public static byte[] adjustZipNames(byte[] canonical, byte[] adjustMe) {
        List<String> providedNames = getZipEntryNames(adjustMe);
       
        for (String name : providedNames) {
            if (ignoredName(name)) {
                adjustMe = discardIgnoredFiles(adjustMe);
                providedNames = getZipEntryNames(adjustMe);
                break;
            }
        }

        
        final Map<String, String> providedMap = getFullNames(providedNames);
        String submitFile = providedMap.get(".submit");
        if (submitFile != null && submitFile.length() > 7) {
            String prefix = getDirectoryPrefix(submitFile);
            if (!submitFile.equals(prefix + ".submit"))
                throw new IllegalArgumentException("huh: " + submitFile);
            return adjustNames(prefix, "", adjustMe);
        }

      
        if (canonical == null)
            return adjustMe;
        
        List<String> canonicalNames = getZipEntryNames(canonical);

        final Map<String, String> map = getFullNames(canonicalNames);

        if (!anySlashes(providedNames)) {
            if (!anySlashes(canonicalNames))
                return adjustMe;

            AdjustName adjuster = new AdjustName() {
                @Override
                public String adjustName(String name) {
                    String s = map.get(simpleName(name));
                    if (s != null)
                        return s;
                    else if (name.endsWith(".java"))
                        return "src/" + name;
                    return name;
                }
            };
            return adjustNames(adjuster, adjustMe);
        }

        List<String> matchingProvidedNames = new ArrayList<String>();
        List<String> matchingCanonicalNames = new ArrayList<String>();
        for (String p : providedNames) {
            String c = map.get(simpleName(p));
            if (c == null)
                continue;
            matchingProvidedNames.add(p);
            matchingCanonicalNames.add(c);
        }

        String commonProvided = getCommonPrefix(providedNames);
        String commonCanonical = getCommonPrefix(canonicalNames);
        
        if (!matchingProvidedNames.isEmpty()) {

            String commonMatchingProvided = getCommonPrefix(matchingProvidedNames);
            String commonMatchingCanonical = getCommonPrefix(matchingCanonicalNames);

            if (strictSuffix(commonMatchingCanonical, commonMatchingProvided)) {
                String strip = commonMatchingProvided.substring(0,
                        commonMatchingProvided.length() - commonMatchingCanonical.length());
                if (commonProvided.startsWith(strip))
                    return adjustNames(getDirectoryPrefix(strip), "", adjustMe);
            } else if (strictSuffix(commonMatchingProvided, commonMatchingCanonical)) {
                String add = commonMatchingCanonical.substring(0,
                        commonMatchingCanonical.length() - commonMatchingProvided.length());
                if (commonCanonical.startsWith(add))
                    return adjustNames("", getDirectoryPrefix(add), adjustMe);
            } else {
                String add = getPrefix(matchingProvidedNames, matchingCanonicalNames);
                if (add != null && commonCanonical.startsWith(add)) 
                    return adjustNames("", getDirectoryPrefix(add), adjustMe);
            }
        }



        if (strictSuffix(commonCanonical, commonProvided)) {
            String strip = commonProvided.substring(0, commonProvided.length()
                    - commonCanonical.length());
            return adjustNames(getDirectoryPrefix(strip), "", adjustMe);
        } else if (strictSuffix(commonProvided, commonCanonical)) {
            String add = commonCanonical.substring(0, commonCanonical.length()
                    - commonProvided.length());
            return adjustNames("", getDirectoryPrefix(add), adjustMe);
        } else {
            String add = getPrefix(providedNames, canonicalNames);
            if (add != null)
                return adjustNames("", getDirectoryPrefix(add), adjustMe);
        }
        return adjustMe;
    }

    interface AdjustName {
        public @CheckForNull
        String adjustName(String name);
    }

    private static byte[] adjustNames(final String strip, final String add,
            byte[] zipContents) {
        if (strip.length() == 0 && add.length() == 0)
            return zipContents;
        if (strip.length() > 0 && !strip.endsWith("/"))
            throw new IllegalArgumentException("Strip: " + strip);
        if (add.length() > 0 && !add.endsWith("/"))
            throw new IllegalArgumentException("Add: " + add);
        
        AdjustName adjuster = new AdjustName() {

            @Override
            public String adjustName(String name) {
                if (name.startsWith(strip))
                    return add + name.substring(strip.length());
                return name;
            }
        };
        return adjustNames(adjuster, zipContents);
    }

    private static byte[] adjustNames(AdjustName adjuster, byte[] zipContents) {

        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(
                zipContents));
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ZipOutputStream zOut = new ZipOutputStream(bOut);
        byte[] buf = new byte[1024];
        try {
            while (true) {
                ZipEntry ze = zin.getNextEntry();
                if (ze == null)
                    break;
                String name = ze.getName();
                name = adjuster.adjustName(name);

                if (name != null) {
                    ZipEntry ze2 = makeZipEntry(name, ze);
                    zOut.putNextEntry(ze2);
                    while (true) {
                        int sz = zin.read(buf);
                        if (sz == -1)
                            break;
                        zOut.write(buf, 0, sz);
                    }
                    zOut.closeEntry();
                }
                zin.closeEntry();

            }
            zOut.close();
        } catch (IOException e) {
            return zipContents;
        }

        return bOut.toByteArray();

    }

    /**
     * @param name
     * @param ze
     * @return
     */
    public static ZipEntry makeZipEntry(String name, ZipEntry ze) {
        ZipEntry ze2 = new ZipEntry(name);
        if (ze.getComment() != null)
            ze2.setComment(ze.getComment());
        if (ze.getCompressedSize() != -1)
            ze2.setCompressedSize(ze.getCompressedSize());
        if (ze.getCrc() != -1)
            ze2.setCrc(ze.getCrc());
        if (ze.getExtra() != null)
            ze2.setExtra(ze.getExtra());
        if (ze.getSize() != -1)
            ze2.setSize(ze.getSize());
        if (ze.getTime() != -1)
            ze2.setTime(ze.getTime());
        return ze2;
    }

    public static String getCommonPrefix(List<String> names) {
        String result = null;
        for (String s : names)
            if (result == null)
                result = s;
            else
                result = Strings.commonPrefix(result, s);
        return result;
    }

    public static @CheckForNull
    String getPrefix(List<String> provided, List<String> canonicalNames) {
        String result = null;

        for (String s : provided) {
            String r = getPrefix(s, canonicalNames);
            if (r != null) {
                if (result == null)
                    result = r;
                else if (!result.equals(r))
                    return null;
            }
        }
        return result;
    }

    public static @CheckForNull
    String getPrefix(String name, List<String> canonicalNames) {
        String result = null;
        for (String s : canonicalNames)
            if (s.endsWith(name)) {
                String p = s.substring(0, s.length() - name.length());
                if (result == null)
                    result = p;
                else if (!result.equals(p))
                    return null;

            }
        return result;
    }

    public static List<String> getZipEntryNames(byte[] zipContents) {
        ArrayList<String> result = new ArrayList<String>();
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(
                zipContents));
        try {
            while (true) {
                ZipEntry ze = zin.getNextEntry();
                if (ze == null)
                    break;
                result.add(ze.getName());
                zin.closeEntry();
            }
        } catch (IOException e) {
            assert true;
        } finally {
            try {
                zin.close();
            } catch (IOException e) {
                assert true;
            }
        }
        return result;
    }

    public static boolean hasProblem(byte[] zipContents) throws IOException {
        return hasProblem(new ByteArrayInputStream(zipContents));
    }

    public static byte[] fixProblem(byte[] zipContents, int pk)
            throws IOException {
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
        byte[] buf = new byte[1024];

        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            zOut.putNextEntry(entry);
            InputStream in = zf.getInputStream(entry);
            while (true) {
                int bytes = in.read(buf);
                if (bytes < 0)
                    break;
                zOut.write(buf, 0, bytes);
            }
            in.close();
            zOut.closeEntry();
        }
        zOut.close();
        zf.close();
    }

    static boolean hasProblem(InputStream zipContents) throws IOException {
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
        if (flag != 0 || method != 8)
            return false;
        int n = readShort(dis);
        int extra = readShort(dis);
        skipFully(dis, n);
        skipFully(dis, extra);

        if (method == 0)
            skipFully(dis, uncompressedSize);
        else
            skipFully(dis, compressedSize);

        int header2 = readInt(dis);
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
        int fourth = dis.read();
        return fourth << 24 | third << 16 | second << 8 | first;
    }

    static void skipFully(DataInputStream is, long bytes) throws IOException {
        while (bytes > 0) {
            long a = is.skip(bytes);
            bytes -= a;
        }
    }
}
