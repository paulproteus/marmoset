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
 * Created on April 8, 2005
 */
package edu.umd.cs.submitServer.content;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestProperties;
import edu.umd.cs.marmoset.utilities.FileNames;
import edu.umd.cs.marmoset.utilities.TextUtilities;

/**
 * Render a sourcefile from a Submission as HTML.
 *
 * @author David Hovemeyer
 */
public class DisplaySubmissionSourceCode {

    private static final Set<String> sourceExtensionSet = new HashSet<String>();
    private static final Set<String> knownSourceFileNames = new HashSet<String>();

    private static final FileNameMap mimeMap = URLConnection.getFileNameMap();
    static {
        sourceExtensionSet.add(".java");
        sourceExtensionSet.add(".c");
        sourceExtensionSet.add(".cc");
        sourceExtensionSet.add(".h");
        sourceExtensionSet.add(".ocaml");
        sourceExtensionSet.add(".ml");
        sourceExtensionSet.add(".mli");
        sourceExtensionSet.add(".ruby");
        sourceExtensionSet.add(".rb");
        sourceExtensionSet.add(".xml");
        sourceExtensionSet.add(".txt");
        sourceExtensionSet.add(".jsp");
        sourceExtensionSet.add(".php");
        sourceExtensionSet.add(".sql");
        sourceExtensionSet.add(".py");

        knownSourceFileNames.add("makefile");
    }

    /**
     * Fetch the Collection of source files for a given Submission.
     *
     * @param conn
     *            the database connection
     * @param submission
     *            the Submission
     * @param testProperties
     *            TODO
     * @return List containing source file names (Strings) for the Submission
     * @throws IOException
     * @throws SQLException
     */
    public static List<String> getSourceFilesForSubmission(Connection conn,
            Submission submission, TestProperties testProperties)
            throws IOException, SQLException {
        TreeSet<String> result = new TreeSet<String>();

        byte[] archive = submission.downloadArchive(conn);

        ZipInputStream zipInput = null;
        try {
            zipInput = new ZipInputStream(new ByteArrayInputStream(archive));
            ZipEntry zipEntry;

            while ((zipEntry = zipInput.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                String simpleName = FileNames.trimSourceFileName(entryName);
                int lastDot = simpleName.lastIndexOf('.');
                if (lastDot >= 0) {
                    String ext = entryName.substring(lastDot);
                    if (sourceExtensionSet.contains(ext)) {
                        result.add(entryName);
                        continue;
                    }
                }

                if (knownSourceFileNames.contains(simpleName)) {
                    result.add(entryName);
                    continue;
                }
                String mimeType = mimeMap.getContentTypeFor(simpleName);
                if (mimeType != null && mimeType.startsWith("text")) {
                    result.add(entryName);
                    continue;
                }
                if (testProperties != null
                        && testProperties.isSourceFile(simpleName)) {
                    result.add(entryName);
                    continue;
                }

            }

            List<String> list = new LinkedList<String>();
            list.addAll(result);

            return list;
        } finally {
            if (zipInput != null)
                zipInput.close();
        }
    }

	/**
     * Render the given sourcefile from the given Submission as HTML.
     * 
     * Available as jsp tag: 
     *   Name: ${ss:displayAllSourceFiles(connection, submission, project, tabWidth, codeCoverageResults)}
     *
     *
     * @param conn
     *            the database connection
     * @param submission
     *            the Submission
     * @param project TODO
     * @param codeCoverageResults
     *            the code coverage information for all of the files. If null,
     *            then we assume no code coverage information is available and
     *            none will be displayed.
     * @param sourceFile
     *            the filename of the sourcefile within the Submission
     * @param highlightStartLine
     *            first line to highlight
     * @param numHighlightLines
     *            number of lines to highlight
     * @param numContextLines
     *            number of lines of context before and after highlight to
     *            display
     * @return formatted HTML table displaying the source code
     * @throws IOException
     * @throws SQLException
     */
    public static String displayAllSourceFiles(Connection conn,
            Submission submission,  Project project,
            @CheckForNull Integer tabWidth, @CheckForNull  CodeCoverageResults codeCoverageResults) throws IOException,
            SQLException {

        byte[] archive = submission.downloadArchive(conn);
        Map<String, List<String>> text = TextUtilities.scanTextFilesInZip(archive);

        Map<String, BitSet> changed = project.computeDiff(conn, submission, text, null);

        return displayArchive(text, tabWidth, codeCoverageResults, changed);
    }

    public static int getNumberChangedLines(Connection conn,
            Submission submission,  Project project) throws IOException,
            SQLException {
        
        int numChangedLines = submission.getNumChangedLines();
        if (numChangedLines >= 0)
            return numChangedLines;
    	 byte[] archive = submission.downloadArchive(conn);
         Map<String, List<String>> text = TextUtilities.scanTextFilesInZip(archive);

         Map<String, BitSet> changed = project.computeDiff(conn, submission, text, null);
         int count = 0;
         for(Map.Entry<String, List<String>> e : text.entrySet()) {
        	 	BitSet b = changed.get(e.getKey());
        	 	if (b != null)
        	 		count += b.cardinality();
        	 	else
        	 		count += e.getValue().size();
         }

         submission.setNumChangedLines(count);
         submission.update(conn);
         return count;
    }


	/**
     * Render the given sourcefile from the given Submission as HTML.
     *
     * @param conn
     *            the database connection
     * @param info.submission
     *            the Submission
     * @param project TODO
     * @param codeCoverageResults
     *            the code coverage information for all of the files. If null,
     *            then we assume no code coverage information is available and
     *            none will be displayed.
     * @param sourceFile
     *            the filename of the sourcefile within the Submission
     * @param highlightStartLine
     *            first line to highlight
     * @param numHighlightLines
     *            number of lines to highlight
     * @param numContextLines
     *            number of lines of context before and after highlight to
     *            display
     * @return formatted HTML table displaying the source code
     * @throws IOException
     * @throws SQLException
     */
    public static String displayBaselineSource(Connection conn,
              Project project,
            @CheckForNull Integer tabWidth) throws IOException,
            SQLException {

    		Integer baselinePK = project.getArchivePK();
        if (baselinePK == null || baselinePK.intValue() == 0) {
        	return "[No baseline source]";
        }
        Map<String, List<String>> text = TextUtilities.scanTextFilesInZip(project.downloadArchive(conn));

        return displayArchive(text, tabWidth, null, null);

    }

	private static String displayArchive(Map<String, List<String>> text,
			@CheckForNull Integer tabWidth, @CheckForNull CodeCoverageResults codeCoverageResults,
			@CheckForNull Map<String, BitSet> changed) throws UnsupportedEncodingException,
			IOException {

		StringWriter o = new StringWriter();
        PrintWriter out = new PrintWriter(o);

        out.println("<ul>");
        String oldDir = "";

        for(Map.Entry<String, List<String>> e : text.entrySet()) {
            String sourceFile = e.getKey();
            List<String> contents = e.getValue();
            int lastSlash = sourceFile.lastIndexOf('/');
            String dir =  sourceFile.substring(0, lastSlash+1);
            String simpleName = sourceFile.substring(lastSlash+1);
            if (!oldDir.equals(dir)) {
                if (oldDir.length() > 0)
                    out.println("</ul>");
                if (dir.length() > 0)
                    out.printf("<li>%s<ul>%n", dir.substring(0, dir.length()-1));
                oldDir = dir;
            }
            String description = diffDescription(changed, sourceFile, contents);

            if (description == null)
                continue;
            out.printf("<li><a href=\"javascript:openElement('%s')\">%s</a> (%s) %n", encode(sourceFile), simpleName, description);


        }
        if (oldDir.length() > 0)
            out.println("</ul>");

        out.println("</ul>");

        generateOpenCloseAll(out);

        out.printf("<div id=\"allSourceCode\">%n");
        for(Map.Entry<String, List<String>> e : text.entrySet()) {
            String fileName = e.getKey();

            boolean fileIsChanged = !isUnchanged(fileName, changed);
            
            if (!fileIsChanged)
                continue;

            List<String> txt = e.getValue();
            String description = diffDescription(changed, fileName, txt);
            
            displaySourceCode(out, fileName, description,  fileIsChanged, txt, tabWidth, codeCoverageResults, changed == null ? null : changed.get(fileName));
            out.printf("</div>%n");

        }
        out.printf("</div>%n");
        out.close();
        return o.toString();
	}

    private static void generateOpenCloseAll(PrintWriter out) {
        out.println("<p>");
        out.println("<a href=\"javascript:openAll()\">expand all</a>");
        out.println(" | ");
        out.println("<a href=\"javascript:closeAll()\">close all</a>");
        out.println("</p>");
    }




    private static boolean isUnchanged(String file, Map<String,BitSet> changed) {
        if (changed == null)
            return false;
        BitSet bitSet = changed.get(file);
        if (bitSet == null)
            return false;
        if (bitSet.cardinality() == 0)
            return true;
        return false;
    }

  
	private static String diffDescription(Map<String, BitSet> changed,
			String sourceFile, List<String> contents) {
		if (isUnchanged(sourceFile, changed))
		   return null; // String.format("%d lines, unchanged", contents.size());
		else if  (changed != null) {
		    BitSet bitSet = changed.get(sourceFile);
		    if (bitSet == null)
		    	   return String.format("%d lines", contents.size());
		    assert bitSet.cardinality() > 0;
		    if (bitSet.cardinality() == contents.size())
		        return String.format("%d lines, all new", contents.size());
		    else
		        return String.format("%d/%d lines changed", bitSet.cardinality(), contents.size());

		} else {
		    return String.format("%d lines", contents.size());
		}

	}

    /**
     * Render the given sourcefile from the given Submission as HTML.
     *
     * @param conn
     *            the database connection
     * @param submission
     *            the Submission
     * @param sourceFile
     *            the filename of the sourcefile within the Submission
     * @param highlightStartLine
     *            first line to highlight
     * @param numHighlightLines
     *            number of lines to highlight
     * @param numContextLines
     *            number of lines of context before and after highlight to
     *            display
     * @param codeCoverageResults
     *            the code coverage information for all of the files. If null,
     *            then we assume no code coverage information is available and
     *            none will be displayed.
     * @return formatted HTML table displaying the source code
     * @throws IOException
     * @throws SQLException
     */
    public static String displaySourceCode(Connection conn,
            Submission submission, String sourceFile,
            Integer highlightStartLine, Integer numHighlightLines,
            Integer numContextLines, Integer tabWidth,
            CodeCoverageResults codeCoverageResults) throws IOException,
            SQLException {
        sourceFile = sourceFile.replace('\\', '/');

        byte[] archive = submission.downloadArchive(conn);
        // Project project = Project.getByProjectPK(submission.getProjectPK(),
        // conn);

        ZipInputStream zipInput = null;
        try {
            zipInput = new ZipInputStream(new ByteArrayInputStream(archive));

            ZipEntry zipEntry;

            while ((zipEntry = zipInput.getNextEntry()) != null) {
                String entryName = zipEntry.getName().replace('\\', '/');

                if (sourceFile.equals(entryName)
                        || sourceFile.equals(FileNames
                                .trimSourceFileName(entryName))) {

                    return displaySourceCode(zipInput, entryName,
                            highlightStartLine, numHighlightLines,
                            numContextLines, tabWidth, codeCoverageResults);

                }
            }

            return null;
        } finally {
            if (zipInput != null)
                zipInput.close();
        }
    }

    public static String headJavascript() {
        return "<script langauge=\"JavaScript\" type=\"text/javascript\"> \n"
                + "function sourceToggle(item) { \n"
                +    " obj=document.getElementById(item);  if (obj.style.display==\"none\") {\n"
                +    " openElement(item); } else { obj.style.display=\"none\"; } } \n"
                + "function openElement(item) { \n"
                +    " document.getElementById(item).style.display=\"block\"; \n"
                +    " document.getElementById('__'+item).scrollIntoView();  } \n"
                + "function openAll() { \n"
                +    " asc = document.getElementById(\"allSourceCode\").getElementsByTagName(\"div\"); \n"
                +    " for (var i = 0; i < asc.length; i++) {\n"
                +    "   asc[i].style.display=\"block\"; \n"
                +    " }}\n"
                + "function closeAll() { \n"
                +    " asc = document.getElementById(\"allSourceCode\").getElementsByTagName(\"div\"); \n"
                +    " for (var i = 0; i < asc.length; i++) {\n"
                +    "   asc[i].style.display=\"none\";\n "
                +    " }}\n"
                +  "</script>";

            }


    private static String encode(String file) throws UnsupportedEncodingException {
         return URLEncoder.encode(file, "UTF-8").replaceAll("/","%2F").replace('%','_');
    }


	private static void displaySourceCode(
            PrintWriter out,
            String sourceFile, String description,
            boolean initiallyOpen,
            List<String> txt,
            @CheckForNull Integer tabWidth, @CheckForNull CodeCoverageResults codeCoverageResults, @CheckForNull BitSet isChanged)
            throws IOException {

		String encoding = encode(sourceFile);
        if (txt.isEmpty()) {
			out.printf("<h4><a id=\"%s\">%s</a> %s</h4>%n", "__" + encoding, sourceFile, description);
			return;

		}
        DisplaySourceCodeAsHTML src2html =  DisplaySourceCodeAsHTML.build(sourceFile, txt.iterator());

        // NAT Account for tab width change, if any
        if (tabWidth != null && tabWidth > 0)
            src2html.setTabWidth(tabWidth);

        src2html.setIsChanged(isChanged, txt.size());
        out.printf("<h4><a id=\"%s\" href=\"javascript:sourceToggle('%s')\">%s</a> %s</h4>%n", "__" + encoding, encoding, sourceFile, description);

        out.printf("  <div id=\"%s\" class=\"sourceCode\" style=\"display:%s\">%n", encoding, initiallyOpen ? "block" : "none");
        src2html.setOutputStream(out);

        if (codeCoverageResults != null) {
            File f = new File(sourceFile);
            FileWithCoverage fileWithCoverage = codeCoverageResults
                    .getFileWithCoverage(f.getName());
            src2html.setFileWithCoverage(fileWithCoverage);
            try {
                src2html.setExcludingCodeCoverage(codeCoverageResults
                        .isExcludingResult());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        src2html.convert();


    }

    private static String displaySourceCode(InputStream zipInput,
            String sourceFile, Integer highlightStartLine,
            Integer numHighlightLines, Integer numContextLines,
            Integer tabWidth, CodeCoverageResults codeCoverageResults)
            throws IOException {

        StringWriter o = new StringWriter();
        PrintWriter out = new PrintWriter(o);

        // TODO Instantiate subclasses of DisplaySourceCodeAsHTML
        // based on the source code extension

        DisplaySourceCodeAsHTML src2html =  DisplaySourceCodeAsHTML.build(sourceFile, zipInput);

        // NAT Account for tab width change, if any
        if (tabWidth != null && tabWidth > 0)
            src2html.setTabWidth(tabWidth);

        src2html.setOutputStream(out);


        // Highlight and context support
        if (highlightStartLine != null && numHighlightLines != null) {
            if (highlightStartLine.intValue() > 0
                    && numHighlightLines.intValue() >= 0) {
                src2html.addHighlightRange(highlightStartLine.intValue(),
                        numHighlightLines.intValue(), "codehighlight");

                // Number of context lines around the code highlight to display
                if (numContextLines != null && numContextLines.intValue() > 0) {
                    int startLine = Math.max(1, highlightStartLine.intValue()
                            - numContextLines.intValue());
                    int endLine = highlightStartLine.intValue()
                            + numHighlightLines.intValue()
                            + numContextLines.intValue();
                    src2html.setDisplayRange(startLine, endLine);
                }
            }
        }

        if (codeCoverageResults != null) {
            // XXX I'm using the fact that the File class will properly
            // strip off leading paths (without me using lastIndexOf() or
            // somesuch).
            // Sort of a hack because the sourceFile is not a path to a real
            // file and
            // the file created should never be used as a file.
            File f = new File(sourceFile);
            FileWithCoverage fileWithCoverage = codeCoverageResults
                    .getFileWithCoverage(f.getName());
            src2html.setFileWithCoverage(fileWithCoverage);
            try {
                src2html.setExcludingCodeCoverage(codeCoverageResults
                        .isExcludingResult());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        src2html.convert();

        out.close();
        return o.toString();
    }
}
