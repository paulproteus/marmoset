package edu.umd.cs.marmoset.modelClasses;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class LogEntry implements Comparable<LogEntry> {
	public enum Priority {
		HIGH, MEDIUM, LOW, VERYLOW
	};

	public static void projectMadeVisible(Connection conn, Course course,
			Project project, String link) throws SQLException {
		insert(conn, course.getCoursePK(), 0, Priority.MEDIUM, "Project "
				+ project.getTitle() + " published for " + course.getCourseName(), project.getNonnullDescription(),
				link);
	}

	public static void activeNewTestSetup(Connection conn, Course course,
			Project project, @Nonnull String comment, @Nonnull String link) throws SQLException {
		insert(conn, course.getCoursePK(), 0, Priority.MEDIUM,
				"New test setup for " + project.getTitle() + " activated for " + course.getCourseName(),
				comment, link);
	}
	public static void projectRetestedResultsInconsistent(Connection conn, Course course,
			Project project, Student student, @Nonnull String comment, @Nonnull String link) throws SQLException {
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.HIGH,
				"Retest of " + project.getNonnullDescription() + " submission yields different results",
				comment, link);
	}
	public static void extensiongranted(Connection conn, Course course,
			Project project, Student student, int extension, @Nonnull String link) throws SQLException {
		Date newDeadline = new Date(project.getOntime().getTime() + TimeUnit.MILLISECONDS.convert(extension, TimeUnit.HOURS));
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.HIGH,
				"Extension of " + extension + " hours granted for of "
				+ project.getNonnullDescription(),
				"On time deadline is now " +  newDeadline, link);
	}
	public static void submissionMarkedBroken(Connection conn, Course course,
			Project project, Student student, Submission submission,  @Nonnull String link) throws SQLException {
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.HIGH,
				"Submission for  " + project.getNonnullDescription() + " marked as broken by instructor",
				"This submission has been marked as broken by the instructor, and will not be tested", link);
	}
	public static void submissionFailedToBuild(Connection conn, Course course,
			Project project, Student student, Submission submission, @Nonnull String link) throws SQLException {
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.HIGH,
				"Submission for  " + project.getNonnullDescription() + " couldn't be built, marking as broken",
				"This submission was sent out to build server multiple times, and the build server never reported back. Marking as broken", link);
	}
	public static void submissionMade(Connection conn, Course course,
			Project project, Student student, Submission submission, @Nonnull String link) throws SQLException {
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.LOW,
				"Submission for  " + project.getNonnullDescription() + " received",
				submission.getSubmissionTimestamp(),
				"Submission received", link);
	}
	public static void submissionReleaseTested(Connection conn, Course course,
			Project project, Student student, Submission submission, @Nonnull String link) throws SQLException {
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.LOW,
				"Submission for  " + project.getNonnullDescription() + " release tested",
				"Submission release tested", link);
	}
	public static void submissionTested(Connection conn, Course course,
			Project project, Student student, Submission submission, TestOutcome testResults, @Nonnull String link) throws SQLException {
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.MEDIUM,
				"Submission for  " + project.getNonnullDescription() + " tested",
				"Submission tested", link);
	}
	public static void submissionSentToBuildServer(Connection conn, Course course,
			Project project, Student student, Submission submission, TestOutcome testResults, @Nonnull String link) throws SQLException {
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.MEDIUM,
				"Submission for  " + project.getNonnullDescription() + " tested",
				"Submission tested", link);
	}
	public static void submissionRetested(Connection conn, Course course,
			Project project, Student student, Submission submission,
			TestSetup testSetup,
			TestOutcome testResults, @Nonnull String link) throws SQLException {
		String comment = "Submission retested with new test setup";
		if (testSetup.getComment() != null) {
			comment += "; " + testSetup.getComment();
		}
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.MEDIUM,
				"Submission for  " + project.getNonnullDescription() + " retested with new setup",
				comment, link);
	}
	public static void submissionBuildTimedOut(Connection conn, Course course,
			Project project, Student student, Submission submission, @Nonnull String link) throws SQLException {
		insert(conn, course.getCoursePK(), student.getStudentPK(), Priority.MEDIUM,
				"Build of submission for  " + project.getNonnullDescription() + " timed out",
				"Build of submission timed out, being sent out again for retesting", link);
	}

	public static final String TABLE_NAME = "log_entries";

	/**
	 * List of all attributes for courses table.
	 */
	static final String[] ATTRIBUTE_NAME_LIST = { "log_pk", "course_pk",
			"student_pk", "priority", "title", "published", "summary", "link" };

	/**
	 * Fully-qualified attributes for log table.
	 */
	public static final String ATTRIBUTES = Queries.getAttributeList(
			TABLE_NAME, ATTRIBUTE_NAME_LIST);

	private LogEntry(Integer logPK, Integer coursePK,
			@Student.PK Integer studentPK,
			Priority priority, String title, Timestamp published,
			String summary, String link) {
		this.logPK = logPK;
		this.coursePK = coursePK;
		this.studentPK = studentPK;
		this.priority = priority;
		this.title = title;
		this.published = published;
		this.summary = summary;
		this.link = link;

	}

	public int getLogPK() {
		return logPK;
	}

	public int getCoursePK() {
		return coursePK;
	}

	public int getStudentPK() {
		return studentPK;
	}

	public Priority getPriority() {
		return priority;
	}

	public String getTitle() {
		return title;
	}

	public Timestamp getPublished() {
		return published;
	}

	public String getSummary() {
		return summary;
	}

	public String getLink() {
		return link;
	}

	private final int logPK; // non-NULL, autoincrement
	private final int coursePK;
	private final @Student.PK int studentPK;
	private final Priority priority;
	private final String title;
	private final Timestamp published;
	private final String summary;
	private final String link;

	public static LogEntry insert(Connection conn, int coursePK,
			Priority priority, String title, String summary, String link)
			throws SQLException {
		return insert(conn, coursePK, 0, priority, title,
				new Timestamp(System.currentTimeMillis()), summary, link);
	}

	public static LogEntry insert(Connection conn, int coursePK,
			@Student.PK int studentPK,
			Priority priority,  String title, String summary, String link)
			throws SQLException {
		return insert(conn, coursePK, studentPK, priority, title,
				new Timestamp(System.currentTimeMillis()), summary, link);
	}

	public static LogEntry insert(Connection conn, int coursePK,
			@Student.PK int studentPK,
			Priority priority, @Nonnull  String title, Timestamp published,
			@Nonnull  String summary, @Nonnull  String link) throws SQLException {
		String insert = Queries.makeInsertStatementUsingSetSyntax(
				ATTRIBUTE_NAME_LIST, TABLE_NAME, true);
		Integer logPK;
		PreparedStatement stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS );
		try {
			Queries.setStatement(stmt, coursePK, studentPK, priority.ordinal(),
					title, published, summary, link);
			stmt.executeUpdate();

			logPK = Queries.getGeneratedPrimaryKey(stmt);
		} finally {
			Queries.closeStatement(stmt);
		}
		return new LogEntry(logPK, coursePK, studentPK, priority, title,
				published, summary, link);
	}

	public LogEntry(ResultSet resultSet, int startingFrom) throws SQLException {
		this.logPK = resultSet.getInt(startingFrom++);
		this.coursePK = resultSet.getInt(startingFrom++);
		this.studentPK = Student.asPK(resultSet.getInt(startingFrom++));
		this.priority = Priority.values()[resultSet.getInt(startingFrom++)];
		this.title = resultSet.getString(startingFrom++);
		this.published = resultSet.getTimestamp(startingFrom++);
		this.summary = resultSet.getString(startingFrom++);
		this.link = resultSet.getString(startingFrom++);
	}

	public LogEntry(ResultSet resultSet) throws SQLException {
		this(resultSet, 1);
	}

	public static Collection<LogEntry> logEntriesForStudent(Student student,
			Priority priority, Connection conn) throws SQLException {
		String query = " SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE log_entries.student_pk = ? " + " AND log_entries.priority <= ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		Queries.setStatement(stmt, student.getStudentPK(), priority.ordinal());
		TreeSet<LogEntry> result = genEntries(stmt);
		stmt.close();
		return result;
	}

	public static Collection<LogEntry> studentLogEntriesForCourse(
			Course course, Priority priority, Connection conn)
			throws SQLException {
		String query = " SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE log_entries.course_pk = ? " + " AND log_entries.student_pk = 0 "
				+ " AND log_entries.priority <= ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		Queries.setStatement(stmt, course.getCoursePK(), priority.ordinal());
		TreeSet<LogEntry> result = genEntries(stmt);
		stmt.close();
		return result;
	}

	private static TreeSet<LogEntry> genEntries(PreparedStatement stmt)
			throws SQLException {
		ResultSet rs = stmt.executeQuery();

		TreeSet<LogEntry> result = new TreeSet<LogEntry>();
		while (rs.next()) {
			LogEntry log = new LogEntry(rs);
			result.add(log);
		}
		rs.close();
		return result;
	}

	public static Collection<LogEntry> instructorLogEntriesForCourse(
			Course course, Priority priority, Connection conn)
			throws SQLException {
		String query = " SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
				+ " WHERE log_entries.course_pk = ? " + " AND log_entries.priority <= ?";

		PreparedStatement stmt = conn.prepareStatement(query);
		Queries.setStatement(stmt, course.getCoursePK(), priority.ordinal());
		TreeSet<LogEntry> result = genEntries(stmt);
		stmt.close();
		return result;
	}

	private static String XML_BLOCK = "\n";
	private static String XML_INDENT = "\t";
	static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
	static final XMLEvent endSection = eventFactory.createDTD(XML_BLOCK);
	static final XMLEvent tabSection = eventFactory.createDTD(XML_INDENT);


	public static void write(OutputStream out, Collection<LogEntry> entries,
			String title, String link, String description, String context) throws XMLStreamException {
		 XMLOutputFactory output = XMLOutputFactory.newInstance();
		 XMLEventWriter writer = output.createXMLEventWriter
	      (out);
	    SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat(
				"EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	    StartDocument startDocument = eventFactory.createStartDocument();
	    writer.add(startDocument);
	    writer.add(endSection);
	    StartElement rssStart = eventFactory.createStartElement("", "", "rss");
	    writer.add(rssStart);
	    writer.add(eventFactory.createAttribute("version", "2.0"));
	    writer.add(endSection);

	    writer.add(eventFactory.createStartElement("", "", "channel"));
	    writer.add(endSection);

	    createNode(writer, "title", title);
	    createNode(writer, "link", link);
	    createNode(writer, "description",description);
	    createNode(writer, "pubDate", RFC822DATEFORMAT.format(new Date()));

	    for(LogEntry entry : entries) {
	    		entry.write(writer, context);
	    }
	    writer.add(endSection);
	    writer.add(eventFactory.createEndElement("", "", "channel"));
	    writer.add(endSection);
	    writer.add(eventFactory.createEndElement("", "", "rss"));

	    writer.add(endSection);
	    writer.add(eventFactory.createEndDocument());
	    writer.close();

	}

	public void write(XMLEventWriter writer, String context) throws XMLStreamException {
		SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat(
				"EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

		XMLEvent endSection = eventFactory.createDTD(XML_BLOCK);
		writer.add(eventFactory.createStartElement("", "", "item"));
		writer.add(endSection);
		createNode(writer, "title", title);
		createNode(writer, "description", summary);
		createNode(writer, "link", context + link);
		createNode(writer, "guid", "submitserverlog" + logPK, "isPermaLink", "false");
		createNode(writer, "pubDate", RFC822DATEFORMAT.format(published));
		writer.add(eventFactory.createEndElement("", "", "item"));
		writer.add(endSection);

	}

	@Override
	public int hashCode() {
		return logPK;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogEntry))
			return false;

		LogEntry other = (LogEntry) obj;
		return this.logPK == other.logPK;
	}


	private static void createNode(XMLEventWriter eventWriter, String name,
			String value) throws XMLStreamException {
		createNode(eventWriter, name, value, null, null);
	}


	private static void createNode(XMLEventWriter eventWriter, String name,
			String value, String attributeName, String attributeValue) throws XMLStreamException {

		Iterator<Attribute> attributeIterator = null;
		if (attributeName != null) {
			Attribute a = eventFactory.createAttribute(attributeName, attributeValue);
			attributeIterator = Collections.singleton(a).iterator();
		}

		StartElement sElement = eventFactory.createStartElement("", "", name, attributeIterator, null);
		eventWriter.add(tabSection);
		eventWriter.add(sElement);

		Characters characters = eventFactory.createCharacters(value);
		eventWriter.add(characters);

		EndElement eElement = eventFactory.createEndElement("", "", name);
		eventWriter.add(eElement);
		eventWriter.add(endSection);
	}

	@Override
	public int compareTo(LogEntry arg0) {
		int result = this.published.compareTo(arg0.published);
		if (result != 0)
			return result;
		if (logPK < arg0.logPK)
			return -1;
		if (logPK > arg0.logPK)
			return 1;
		return 0;
	}

}
