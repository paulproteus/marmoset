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
 * Created on April 6, 2005
 */
package edu.umd.cs.submitServer.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;

import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.codeCoverage.Cond;
import edu.umd.cs.marmoset.codeCoverage.CoverageStats;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;
import edu.umd.cs.marmoset.codeCoverage.Line;
import edu.umd.cs.marmoset.modelClasses.TestOutcome;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.parser.JavaTokenScanner;
import edu.umd.cs.marmoset.parser.PlainTextTokenScanner;
import edu.umd.cs.marmoset.parser.ReaderIterator;
import edu.umd.cs.marmoset.parser.Token;
import edu.umd.cs.marmoset.parser.TokenScanner;
import edu.umd.cs.marmoset.parser.TokenType;
import edu.umd.cs.marmoset.utilities.EditDistance;

/**
 * Display source code as HTML.
 *
 * @author David Hovemeyer
 */
public class DisplaySourceCodeAsHTML {
	private static final Entity ENTITY_NBSP = new Entity("&nbsp;");
	private static final Entity ENTITY_LT = new Entity("&lt;");
	private static final Entity ENTITY_GT = new Entity("&gt;");
	private static final Entity ENTITY_AMP = new Entity("&amp;");

	private static final int DEFAULT_TAB_WIDTH = 8;

	private static class HighlightRange implements Comparable<HighlightRange> {
		int startLine;
		String style;
		int id;

		HighlightRange(int startLine, String style, int id) {
			this.startLine = startLine;
			this.style = style;
			this.id = id;
		}

		@Override
		public int compareTo(HighlightRange other) {
			return this.startLine - other.startLine;
		}

		@Override
		public int hashCode() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof HighlightRange))
				return false;
			HighlightRange other = (HighlightRange) o;
			return startLine == other.startLine && id == other.id
					&& style.equals(other.style);

		}
	}

	private static class Entity {
		String value;

		Entity(String value) {
			this.value = value;
		}
	}
	@CheckForNull BitSet isChanged;
	


	private final TokenScanner scanner;
	protected PrintWriter out;
	private int tabWidth = DEFAULT_TAB_WIDTH;
	private Map<Integer, HighlightRange> highlightMap = new HashMap<Integer, HighlightRange>();
	private Set<Integer> anchorLines = new HashSet<Integer>();
	private Map<TokenType, String> tokenStyleMap = new HashMap<TokenType, String>();
	private int displayStart, displayEnd;

	private int lineNo;
	private int col;
	private HighlightRange currentHighlightRange;
	private boolean anchoredLine;
	private int highlightCount;

	protected FileWithCoverage fileWithCoverage;

	/**
	 * Sets the FileWithCoverage for this source file. The coverage results will
	 * be used to markup the source with additional information.
	 *
	 * @param fileWithCoverage
	 *            The code coverage results associated with this source file to
	 *            be used when displaying the source file.
	 */
	public void setFileWithCoverage(FileWithCoverage fileWithCoverage) {
		this.fileWithCoverage = fileWithCoverage;
	}

	public static DisplaySourceCodeAsHTML build(File f) throws FileNotFoundException {
		return build(f.getName(), new FileInputStream(f));
	}

	public static DisplaySourceCodeAsHTML build(String fileName, InputStream in) {
		return build(fileName, new ReaderIterator(in));
	}
	public static DisplaySourceCodeAsHTML build(String fileName, Iterator<String> txt) {
		TokenScanner scanner;
		if (fileName.endsWith(".java") || fileName.endsWith(".c")
				|| fileName.endsWith(".rb") || fileName.endsWith(".ruby")
				|| fileName.endsWith(".h"))
			scanner = new JavaTokenScanner(txt);
		else
			scanner = new PlainTextTokenScanner(txt);

		return new DisplaySourceCodeAsHTML(scanner);
	}

	/**
	 * Constructor.
	 */
	public DisplaySourceCodeAsHTML(TokenScanner scanner) {
		this.scanner = scanner;
		setDefaultTokenStyles();
	}

	/**
	 * Set the PrintStream to write HTML output.
	 *
	 * @param outputStream
	 *            the PrintStream
	 */
	public void setOutputStream(PrintWriter outputStream) {
		this.out = outputStream;
	}

	/**
	 * Set the tab width. Defaults to 4 if not set explicitly.
	 *
	 * @param tabWidth
	 *            the tab width
	 */
	public void setTabWidth(int tabWidth) {
		this.tabWidth = tabWidth;
	}

	/**
	 * Add a range of lines to be highlighted. Lines are numbered starting at 1
	 * (which is the first line in the file).
	 *
	 * @param startLine
	 *            first line to highlight
	 * @param numLines
	 *            number of lines to highlight
	 * @param style
	 *            CSS style to use for the highlight
	 */
	public void addHighlightRange(int startLine, int numLines, String style) {
		HighlightRange highlight = new HighlightRange(startLine, style,
				highlightCount++);
		anchorLines.add(startLine);
		for(int i = startLine; i < startLine + numLines; i++)
		    highlightMap.put(i, highlight);
	}

	/**
	 * Set a range of lines of the file to display.
	 *
	 * @param displayStart
	 *            first line to display (inclusive)
	 * @param displayEnd
	 *            last line to display (exclusive)
	 */
	public void setDisplayRange(int displayStart, int displayEnd) {
		this.displayStart = displayStart;
		this.displayEnd = displayEnd;
	}

	/**
	 * Set a CSS class to use for tokens of the given type. This is useful for
	 * implementing token-based code highlighting.
	 *
	 * @param tokenType
	 *            the type of token (e.g., keyword, string literal, etc.)
	 * @param style
	 *            the CSS class to use for the token type
	 */
	public void setTokenStyle(TokenType tokenType, String style) {
		tokenStyleMap.put(tokenType, style);
	}

	/**
	 * Set the default set of token styles.
	 */
	public void setDefaultTokenStyles() {
		setTokenStyle(TokenType.KEYWORD, "codekeyword");
		setTokenStyle(TokenType.SINGLE_LINE_COMMENT, "codecomment");
		setTokenStyle(TokenType.MULTI_LINE_COMMENT, "codecomment");
		setTokenStyle(TokenType.STRING_LITERAL, "codestring");
		setTokenStyle(TokenType.LITERAL, "codeliteral");
	}

	@CheckForNull BitSet showLine;
	/**
	 * Set the default set of token styles.
	 */
	public void clearTokenStyles() {
		tokenStyleMap.remove(TokenType.KEYWORD);
		tokenStyleMap.remove(TokenType.SINGLE_LINE_COMMENT);
		tokenStyleMap.remove(TokenType.MULTI_LINE_COMMENT);
		tokenStyleMap.remove(TokenType.STRING_LITERAL);
		tokenStyleMap.remove(TokenType.LITERAL);
	}

	/**
	 * Convert input Java source to HTML.
	 *
	 * @throws IOException
	 */
	public void convert() throws IOException {

		lineNo = 1;

		beginCode();
		beginLine();

		for (Iterator<Token> i = scanner; i.hasNext();) {
			Token token = i.next();

			boolean displayThisLine = inDisplayRange();

			if (token.getType() == TokenType.NEWLINE) {
				newLine(displayThisLine, !i.hasNext());
				continue;
			}

			if (!displayThisLine || isElided)
				continue;

			String style = tokenStyleMap.get(token.getType());

			if (style != null) {
				out.print("<span class=\"");
				out.print(style);
				out.print("\">");
			}

			displayToken(token.getLexeme());

			if (style != null) {
				out.print("</span>");
			}
		}
		endLine();
		endCode();
	}



	private void newLine(boolean displayThisLine, boolean lastLine) throws IOException {
		col = 0;
		++lineNo;
		if (displayThisLine) {
			endLine();
			col = 0;
			if (!lastLine)
				beginLine();
		}
	}

	private boolean inDisplayRange() {
		if (displayStart == 0)
			return true;
		else
			return lineNo >= displayStart && lineNo < displayEnd;
	}

	/**
	 * Begin the HTML code to display the source code.
	 */
	protected void beginCode() {

	    isElided = false;
	    elidedCount = 0;
		// Display code coverage stats if we have code coverage information
		// available.
		if (fileWithCoverage != null) {
			CoverageStats coverageStats = fileWithCoverage.getCoverageStats();
			coverageStats.setExcludingResult(isExcludingCodeCoverage);
			out.println("<table>");

			out.println("<th>Source file</th>");
			out.println("<th>statements</th>");
			out.println("<th>conditionals</th>");
			out.println("<th>methods</th>");


			out.println("<tr>");
			out.println("<td>" + fileWithCoverage.getShortFileName() + "</td>");
			out.println(coverageStats.getHTMLTableRow());
			out.println("</tr>");

			out.println("</table>");
			out.println("<p>");
		}

		out.println("<table class=\"codetable\" width=\"100%\">");

		// Tarantula: Coverage information split up by test case
		if (testOutcomeCollection != null) {
			out.print("<th>#<th>C");
			out.print(TestOutcomeCollection
					.formattedTestHeader(testOutcomeCollection, true));
			out.println("<th>score<th>line");
		}
	}

	/**
	 * End the HTML code to display the source code.
	 */
	protected void endCode() {
	    if (isElided)
	        printElidedMessage();
		out.println("</table>");
	}

	boolean inBeginningWhiteSpace;
	boolean isElided;
	int elidedCount = 0;
	/**
	 * Begin a line of source code.
	 */
	protected void beginLine() throws IOException {
		inBeginningWhiteSpace = true;

		// New range begins?
		currentHighlightRange = highlightMap.get(lineNo);
		anchoredLine = anchorLines.contains(lineNo);

		String style = null;
		if (currentHighlightRange != null) {
			style = currentHighlightRange.style;
		}

		// Callback for adding coverage information to the generated page
		CoverageInfo coverageInfo = coverageCallback(style);
		boolean wasElided = isElided;

		style = coverageInfo.style;
		
		if (style != null) {
		    isElided = false;
		} else if (isChanged != null && !isChanged.get(lineNo - 1)) {
            if (showLine != null && !showLine.get(lineNo - 1)) {
                if (!isElided) {
                    isElided = true;
                    elidedCount = 1;
                } else
                    elidedCount++;
            } else {
                isElided = false;
                style = "codeunchanged";
            } 
        } else
            isElided = false;
		if (isElided)
		    return;
		if (wasElided) {
		    printElidedMessage();
		}

        // right align the line number count
        // also drop an anchor at each line
        out.print("<tr><td class=\"linenumber\"><a title=\"" + lineNo + "\">" + lineNo + "</a></td>");
        out.print(coverageInfo.coverageCol);
        out.print("<td");
		if (style != null) {
			// XXX HACK ALERT: Trying to set either a style and a class.
			if (style.contains("style="))
				out.print(" " + style);
			else
				out.print(" class=\"" + style + "\"");
		}
		out.print(">");
		if (anchoredLine) {
			out.print("<a name=\"");
			out.print("codehighlight");
			out.print(currentHighlightRange.id);
			out.print("\">");
		}
	}

    /**
     * 
     */
    public void printElidedMessage() {
        out.print("<tr><td class=\"linenumber\"/>");
        if (fileWithCoverage != null)
        	  out.print("<td/>");
        out.print("<td class=\"codeelided\">");
        out.printf(" ... %d lines elided ... </td>%n", elidedCount);
    }

	static class CoverageInfo {	

		 String style;
		 String coverageCol;
	}
	/**
	 * @param style
	 * @return
	 * @throws IOException
	 */
	private CoverageInfo coverageCallback(String style) throws IOException {
		CoverageInfo result = new CoverageInfo();
		result.style = style;
		result.coverageCol = "";
		if (fileWithCoverage != null) {
			Line<?> line = fileWithCoverage.getCoverage(lineNo);
			if (line == null)
				result.coverageCol = "<td/>";
			else {
				String codeClass;

				if (line.isCovered() ^ isExcludingCodeCoverage)
					codeClass = "codecoveredcount";
				else {
					codeClass = "codeuncoveredcount";
					if (!anchoredLine)
						result.style = "codeuncovered";
				}

				String body = "";
				if (line instanceof Cond) {
					Cond c = (Cond) line;
					body = c.getTrueCount() + "/" + c.getFalseCount();
				} else
					body = Integer.toString(line.getCount());

				result.coverageCol = String.format("<td class=\"%s\">%s</td>", codeClass, body);
				

				// Display coverage information split up by testOutcome.
//				style = tarantulaCallback(style);

			}
		}
		return result;
	}

	/**
	 * @throws IOException
	 */
	private String tarantulaCallback(String style) throws IOException {
		if (testOutcomeCollection != null) {

			// FIXME: Should handle any selected test type, not only all
			// cardinal test types
			Iterable<TestOutcome> iterable = testOutcomeCollection
					.getIterableForCardinalTestTypes();

			boolean isLineExecutable = false;

			int totalTests = 0;
			int passed = 0;
			int totalPassed = 0;
			int failed = 0;
			int totalFailed = 0;
			int countByOutcome = 0;
			for (TestOutcome outcome : iterable) {
				totalTests++;
				FileWithCoverage coverageForGivenOutcome = outcome
						.getCodeCoverageResults().getFileWithCoverage(
								fileWithCoverage.getShortFileName());

				countByOutcome = coverageForGivenOutcome
						.getStmtCoverageCount(lineNo);
				if (outcome.getOutcome().equals(TestOutcome.PASSED))
					totalPassed++;
				else
					totalFailed++;

				if (countByOutcome > 0) {
					isLineExecutable = true;
					if (outcome.getOutcome().equals(TestOutcome.PASSED)) {
						// passed outcome
						out.print("<td class=\"passed\">" + countByOutcome
								+ "</td>");
						passed++;
					} else {
						out.print("<td class=\"failed\">" + countByOutcome
								+ "</td>");
						failed++;
					}
				} else if (countByOutcome < 0) {
					out.print("<td></td>");
				} else {
					out.print("<td></td>");
				}
			}
			if (isLineExecutable) {
				double passedPct = totalPassed > 0 ? (double) passed
						/ (double) totalPassed : 0.0;
				double failedPct = totalFailed > 0 ? (double) failed
						/ (double) totalFailed : 0.0;
				double intensity = Math.max(passedPct, failedPct);

				double score = passedPct / (passedPct + failedPct);
				 if (false) System.out.println(fileWithCoverage.getShortFileName() +
				 ", lineNumber = "+lineNo+
				 ", executable? "+isLineExecutable+
				 ", countByOutcome = "+countByOutcome+
				 ", totalPassed = "+totalPassed+
				 ", totalFailed = "+totalFailed+
				 ", passed = " +passed+
				 ", failed = " +failed+
				 ", passedPct = "+passedPct+
				 ", failedPct = "+failedPct+
				 ", score = "+score);
				style = " style=\"background-color:#"
						+ scaleColor(score, intensity) + "\"";
				out.print("<td bgcolor=\"#" + scaleColor(score, intensity)
						+ "\">" + format.format(score) + "</td>");
			} else {
				out.print("<td></td>");
			}
		}
		return style;
	}

	/**
	 * Given a score from 0.0 to 1.0, computes an RGB color where 0.0 is
	 * completely red and 1.0 is completely green, and 0.5 is completley yellow.
	 * TODO Handle brightness intensities.
	 *
	 * @param score
	 * @return
	 */
	public static String scaleColor(double score, double intensity) {
		int range = 511;
		int halfRange = 256;

		int hue = (int) (range * score);

		int red = 0;
		int green = 0;
		if (hue < halfRange) {
			// hue <= 255
			red = 255;
			green = hue;

		} else if (hue == halfRange) {
			green = 255;
			red = 255;
		} else {
			// hue > 256
			hue = hue - halfRange;
			green = 255;
			red = halfRange - hue;
		}

		green = (int) (green * intensity);
		if (green > 255)
			green = 255;
		red = (int) (red * intensity);
		if (red > 255)
			red = 255;

		String greenStr = Long.toHexString(green);
		if (greenStr.length() == 1)
			greenStr = "0" + greenStr;

		String redStr = Long.toHexString(red);
		if (redStr.length() == 1)
			redStr = "0" + redStr;
		return redStr + greenStr + "00";
	}

	private static final NumberFormat format = new DecimalFormat("0.00");

	/**
	 * End a line of source code.
	 */
	protected void endLine() {
		if (col == 0) {
		    out.print(" ");
		}
		if (anchoredLine) {
			out.print("</a>");
			anchoredLine = false;
		}
		out.println("</td></tr>");
	}

	private void displayToken(String lexeme) throws IOException {
		for (int i = 0; i < lexeme.length(); ++i) {
			char c = lexeme.charAt(i);

			switch (c) {
			case ' ':
				if (false && inBeginningWhiteSpace)
					displayEntity(ENTITY_NBSP);
				else {
					out.print(c);
					col++;
				}
				break;
			case '\t':
				displayTab();
				break;
			case '<':
				inBeginningWhiteSpace = false;
				displayEntity(ENTITY_LT);
				break;
			case '>':
				inBeginningWhiteSpace = false;
				displayEntity(ENTITY_GT);
				break;
			case '&':
				inBeginningWhiteSpace = false;
				displayEntity(ENTITY_AMP);
				break;
			default:
				inBeginningWhiteSpace = false;
				out.print(c);
				++col;
				break;
			}
		}
	}

	private void displayEntity(Entity entity) {
		out.print(entity.value);
		++col;
	}

	private void displayTab() {
		int nSpace = tabWidth - (col % tabWidth);
		while (nSpace-- > 0) {
			out.print(" ");
			++col;
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 1 || args.length > 4) {
			System.err
					.println("Usage: "
							+ DisplaySourceCodeAsHTML.class.getName()
							+ " <java source file> [<coverage.xml>] [<highlight start>-<num highlight lines>] [<n context lines>]");
			System.exit(1);
		}
		File f = new File(args[0]);

		System.out.printf("<html><head><title>%s</title>%n", f.getName());
		if (new File("styles.css").exists()) {
			System.out
					.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">");
		}
		System.out.println("</head><body>");
		DisplaySourceCodeAsHTML src2html =  DisplaySourceCodeAsHTML.build(f);
		src2html.setOutputStream(new PrintWriter(System.out, true));
		if (args.length >= 2) {
			// XML file containing code coverage results
			CodeCoverageResults codeCoverageResults = CodeCoverageResults
					.parseFile(args[1]);
			// trim out the full name of the source file
			String trimmedSourceFileName = args[0];
			int lastSlash = trimmedSourceFileName.lastIndexOf('/');
			trimmedSourceFileName = trimmedSourceFileName
					.substring(lastSlash + 1);

			src2html.setFileWithCoverage(codeCoverageResults
					.getFileWithCoverage(trimmedSourceFileName));

			if (args.length >= 4) {
				int startLine = Integer.parseInt(args[2]);
				int numLines = Integer.parseInt(args[3]);
				src2html.addHighlightRange(startLine, numLines, "codehighlight");
				if (args.length >= 5) {
					int numContextLines = Integer.parseInt(args[4]);
					src2html.setDisplayRange(
							Math.max(1, startLine - numContextLines), startLine
									+ numLines + numContextLines);
				}
			}
		}
		src2html.convert();
		System.out.println("</body></html>");

	}

	private TestOutcomeCollection testOutcomeCollection;

	public void setTestOutcomeCollection(
			TestOutcomeCollection testOutcomeCollection) {
		this.testOutcomeCollection = testOutcomeCollection;
	}

	private boolean isExcludingCodeCoverage = false;

	public void setExcludingCodeCoverage(boolean b) {
		isExcludingCodeCoverage = b;
	}
}
