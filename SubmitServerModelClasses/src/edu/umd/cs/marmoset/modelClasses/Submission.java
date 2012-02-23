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

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.meta.TypeQualifier;

import edu.umd.cs.marmoset.utilities.DisplayProperties;
import edu.umd.cs.marmoset.utilities.MarmosetUtilities;
import edu.umd.cs.marmoset.utilities.Multiset;
import edu.umd.cs.marmoset.utilities.SqlUtilities;
import edu.umd.cs.marmoset.utilities.TextUtilities;

/**
 * Object to represent a single row in the submissions table.
 * <p>
 * <b>NOTE:</b>
 * A submission is required to have a non-null submission_timestamp field.
 * This is the sole mechanism that differentiates a submission from a "snapshot"
 * submission (i.e. something that's dumped into the database from CVS)
 *
 *
 * @author daveho
 * @author jspacco
 *
 */
public class Submission implements ITestSummary<Submission> {
	public static final String SUBMISSION_ARCHIVES = "submission_archives";

	@Documented
	@TypeQualifier(applicableTo = Integer.class)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface PK {}

	public static @PK int asPK(  int pk) {
		return pk;
	}
	public static @PK Integer asPK( Integer pk) {
		return pk;
	}
    private static final long MILLISECONDS_IN_ONE_HOUR = 3600000L;

	public static final String TABLE_NAME = "submissions";

	// Possible values for build_status

	public enum BuildStatus {
		/** accepted, project doesn't use testing */
		ACCEPTED,
		/** Accepted, not sent out for testing */
		NEW,
		/** Marked as pending when NEW or RETEST and sent out for testing */
		PENDING,
		/** Testing complete */
		COMPLETE,
		/** Tested, marked by instructor for retest */
		RETEST,
		/** Has problems, do not send to build server. Either marked explicitly, or too many failed build attempts */
		BROKEN;


		public void setStatement(PreparedStatement p, int col) throws SQLException {
			p.setString(col, this.name().toLowerCase());
		}
		public static BuildStatus get(ResultSet rs, int col) throws SQLException  {
			return valueOf(rs.getString(col).toUpperCase());
		}

		public static BuildStatus valueOfAnyCase(String name) {
			return valueOf(name.toUpperCase());
		}

	};

	public static final String SUCCESSFUL = "Successful";
	public static final String ON_TIME= "On-time";
	public static final String LATE= "Late";
	public static final String VERY_LATE= "Very Late";

	// Value if the submission test machine is not known
	public static final String UNKNOWN_TEST_MACHINE = "unknown";

	// XXX these defaults correspond to the database defaults!
	private @Submission.PK Integer submissionPK; // not NULL, autoincrement
	private @StudentRegistration.PK int studentRegistrationPK = 0; 
	private @Project.PK int projectPK = 0; 
	private int numTestRuns = 0;
	private @CheckForNull Integer currentTestRunPK; // may be NULL
	private int submissionNumber = 0;
	private Timestamp submissionTimestamp;
	private String cvsTagTimestamp;
	private Timestamp buildRequestTimestamp;
	private BuildStatus buildStatus = BuildStatus.NEW;
	private int numPendingBuildRequests;
	private int numSuccessfulBackgroundRetests;
	private int numFailedBackgroundRetests;

	private String submitClient = "unknown";
	private Timestamp releaseRequest;
	private boolean releaseEligible;
	private int valuePassedOverall;
	private boolean compileSuccessful;
	private boolean mostRecent = true;
    private int valuePublicTestsPassed;
    private int valueReleaseTestsPassed;
    private int valueSecretTestsPassed;
    private int numFindBugsWarnings;
    private int numChangedLines = -1; // -1 means unknown
	private Integer archivePK; // may be NULL
	/**
	 * This is a write-only field.  Users can set a byte array as the cached archive
	 * for upload, but they can only retrieve the archive via the
	 * {@link #downloadArchive(Connection) downloadArchive} method.
	 * <p>
	 * In fact, 'cachedArchive' is a midleading because we only cache the archive
	 * before we upload it.  We specifically <b>don't</b> perform any caching of
	 * downloads.  The only way to retrieve the bytes of archive is to download them
	 * directly from the database each time.
	 */
	private byte[] cachedArchive;
    private String status;
    private int adjustedScore;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
	    return submissionPK + MarmosetUtilities.hashString("submission");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
	    if (this.getClass() != o.getClass())
	        return false;
	    Submission other = (Submission) o;
	    return submissionPK.intValue() == other.getSubmissionPK().intValue();
	}

	/**
	 * List of all attributes of submissions table.
	 */
	  static final String[] ATTRIBUTE_NAME_LIST = {
		"submission_pk",
		"student_registration_pk",
		"project_pk",
		"num_test_outcomes",
		"current_test_run_pk",
		"submission_number",
		"submission_timestamp",
		"most_recent",
		"cvstag_timestamp",
		"build_request_timestamp",
		"build_status",
		"num_pending_build_requests",
		"num_successful_background_retests",
		"num_failed_background_retests",
		"submit_client",
		"release_request",
		"release_eligible",
		"num_passed_overall",
		"num_build_tests_passed",
		"num_public_tests_passed",
		"num_release_tests_passed",
		"num_secret_tests_passed",
		"num_findbugs_warnings",
		"num_changed_lines",
		"archive_pk"
	};

	/**
	 * Fully-qualified attributes for submissions table.
	 */
	public static final String ATTRIBUTES =
		Queries.getAttributeList(TABLE_NAME, ATTRIBUTE_NAME_LIST);

	/**
	 * Constructor.
	 * All fields will have default values.
	 */
	public Submission() {}

	/**
	 * @return true if this submission has had a release request, false otherwise.
	 */
	public boolean isReleaseTestingRequested() {
	    Timestamp releaseRequest2 = getReleaseRequest();
        return releaseRequest2 != null;
	}
    /**
     * @return Returns the numPassedOverall.
     */
    @Override
	public int getValuePassedOverall()
    {
        return valuePassedOverall;
    }
    /**
     * @param numPassedOverall The numPassedOverall to set.
     */
    public void setValuePassedOverall(int numPassedOverall)
    {
        this.valuePassedOverall = numPassedOverall;
    }
    /**
     * @return Returns the numBuildTestsPassed.
     */
    @Override
	public boolean isCompileSuccessful()
    {
        return compileSuccessful;
    }
    /**
     * @param success The numBuildTestsPassed to set.
     */
    public void setCompileSuccessful(boolean success)
    {
        this.compileSuccessful = success;
    }
    /**
     * @return Returns the numFindBugsWarnings.
     */
    @Override
	public int getNumFindBugsWarnings()
    {
        return numFindBugsWarnings;
    }
    /**
     * @param numFindBugsWarnings The numFindBugsWarnings to set.
     */
    public void setNumFindBugsWarnings(int numFindBugsWarnings)
    {
        this.numFindBugsWarnings = numFindBugsWarnings;
    }
    /** -1 means unknown */
    public int getNumChangedLines() {
        return numChangedLines;
    }
    public void setNumChangedLines(int numChangedLines) {
        this.numChangedLines = numChangedLines;
    }
    /**
     * @return Returns the numPublicTestsPassed.
     */
    @Override
	public int getValuePublicTestsPassed()
    {
        return valuePublicTestsPassed;
    }
    /**
     * @param numPublicTestsPassed The numPublicTestsPassed to set.
     */
    public void setValuePublicTestsPassed(int numPublicTestsPassed)
    {
        this.valuePublicTestsPassed = numPublicTestsPassed;
    }
    /**
     * @return Returns the numReleaseTestsPassed.
     */
    @Override
	public int getValueReleaseTestsPassed()
    {
        return valueReleaseTestsPassed;
    }
    /**
     * @param numReleaseTestsPassed The numReleaseTestsPassed to set.
     */
    public void setValueReleaseTestsPassed(int numReleaseTestsPassed)
    {
        this.valueReleaseTestsPassed = numReleaseTestsPassed;
    }
    /**
     * @return Returns the numSecretTestsPassed.
     */
    @Override
	public int getValueSecretTestsPassed()
    {
        return valueSecretTestsPassed;
    }
    /**
     * @param numSecretTestsPassed The numSecretTestsPassed to set.
     */
    public void setValueSecretTestsPassed(int numSecretTestsPassed)
    {
        this.valueSecretTestsPassed = numSecretTestsPassed;
    }


	public String getTestSummary() {
	    if (getBuildStatus() == BuildStatus.COMPLETE)
	        return String.format("%d | %d | %d | %d",
	                getValuePublicTestsPassed(),
	                getValueReleaseTestsPassed(),
	                getValueSecretTestsPassed(),
	                getNumFindBugsWarnings());
	    return getBuildStatus().toString().toLowerCase();
	}
	/**
	 * @return Returns the releaseRequest.
	 */
	public Timestamp getReleaseRequest() {
		return releaseRequest;
	}
	/**
	 * @param releaseRequest The releaseRequest to set.
	 */
	public void setReleaseRequest(Timestamp releaseRequest) {
		this.releaseRequest = releaseRequest;
	}
    public boolean isReleaseEligible() {
        return releaseEligible;
    }
    public void setReleaseEligible(boolean releaseEligible) {
        this.releaseEligible = releaseEligible;
    }
	/**
	 * @return Returns the buildRequestTimestamp.
	 */
	public Timestamp getBuildRequestTimestamp() {
		return buildRequestTimestamp;
	}
	/**
	 * @param buildRequestTimestamp The buildRequestTimestamp to set.
	 */
	public void setBuildRequestTimestamp(Timestamp buildRequestTimestamp) {
		this.buildRequestTimestamp = buildRequestTimestamp;
	}
	/**
	 * @return Returns the buildStatus.
	 */
	public BuildStatus getBuildStatus() {
		return buildStatus;
	}
	/**
	 * @return Returns whether the build status is complete
	 */
	public boolean isComplete() {
		return buildStatus == BuildStatus.COMPLETE;
	}
	/**
	 * @param buildStatus The buildStatus to set.
	 */
	public void setBuildStatus(BuildStatus buildStatus) {
		this.buildStatus = buildStatus;
	}

	/**
	 * @param buildStatus The buildStatus to set.
	 */
	public void setBuildStatusFromString(String buildStatus) {
		this.buildStatus = BuildStatus.valueOfAnyCase(buildStatus);
	}

	/**
	 * @return the numPendingBuildRequests
	 */
	public int getNumPendingBuildRequests() {
		return numPendingBuildRequests;
	}

	/**
	 * @param numPendingBuildRequests the numPendingBuildRequests to set
	 */
	public void setNumPendingBuildRequests(int numPendingBuildRequests) {
		this.numPendingBuildRequests = numPendingBuildRequests;
	}
	/**
	 * @param numPendingBuildRequests the numPendingBuildRequests to set
	 */
	public void incrementNumPendingBuildRequests() {
		this.numPendingBuildRequests++;
	}

	/**
	 * @return the numSuccessfulBackgroundRetests
	 */
	public int getNumSuccessfulBackgroundRetests() {
		return numSuccessfulBackgroundRetests;
	}

	/**
	 * @param numSuccessfulBackgroundRetests the numSuccessfulBackgroundRetests to set
	 */
	public void setNumSuccessfulBackgroundRetests(int numSuccessfulBackgroundRetests) {
		this.numSuccessfulBackgroundRetests = numSuccessfulBackgroundRetests;
	}
	/**
	 * @param numSuccessfulBackgroundRetests the numSuccessfulBackgroundRetests to set
	 */
	public void incrementNumSuccessfulBackgroundRetests() {
		this.numSuccessfulBackgroundRetests++;
	}

	/**
	 * @return the numFailedBackgroundRetests
	 */
	public int getNumFailedBackgroundRetests() {
		return numFailedBackgroundRetests;
	}

	/**
	 * @param numFailedBackgroundRetests the numFailedBackgroundRetests to set
	 */
	public void setNumFailedBackgroundRetests(int numFailedBackgroundRetests) {
		this.numFailedBackgroundRetests = numFailedBackgroundRetests;
	}
	/**
	 * @param numFailedBackgroundRetests the numFailedBackgroundRetests to set
	 */
	public void incrementNumFailedBackgroundRetests() {
		this.numFailedBackgroundRetests++;
	}

	/**
	 * @return Returns the cvsTagTimestamp.
	 */
	public String getCvsTagTimestamp() {
		return cvsTagTimestamp;
	}
	/**
	 * @param cvsTagTimestamp The cvsTagTimestamp to set.
	 */
	public void setCvsTagTimestamp(String cvsTagTimestamp) {
		this.cvsTagTimestamp = cvsTagTimestamp;
	}
	/**
	 * @return Returns the projectPK.
	 */
	public @Project.PK int getProjectPK() {
		return projectPK;
	}

	/**
	 * @param projectPK The projectPK to set.
	 */
	public void setProjectPK(@Project.PK int projectPK) {
		this.projectPK = projectPK;
	}
	/**
	 * @return Returns the studentRegistrationPK.
	 */
	public @StudentRegistration.PK int getStudentRegistrationPK() {
		return studentRegistrationPK;
	}
	/**
	 * @param studentRegistrationPK The studentRegistrationPK to set.
	 */
	public void setStudentRegistrationPK(@StudentRegistration.PK int studentRegistrationPK) {
		this.studentRegistrationPK = studentRegistrationPK;
	}
	/**
	 * @return Returns the submissionNumber.
	 */
	public int getSubmissionNumber() {
		return submissionNumber;
	}
	/**
	 * @param submissionNumber The submissionNumber to set.
	 */
	public void setSubmissionNumber(String submissionNumber) {
		this.submissionNumber = Integer.parseInt(submissionNumber);
	}
	/**
     * @param submissionNumber The submissionNumber to set.
     */
    public void setSubmissionNumber(int submissionNumber) {
        this.submissionNumber = submissionNumber;
    }
	/**
	 * @return Returns the submissionPK.
	 */
	public @Submission.PK Integer getSubmissionPK() {
		return submissionPK;
	}
	/**
	 * @param submissionPK The submissionPK to set.
	 */
	public void setSubmissionPK(@Submission.PK Integer submissionPK) {
		this.submissionPK = submissionPK;
	}
	/**
	 * @return Returns the formatted submissionTimestamp.
	 */
	public String getFormattedSubmissionTimestamp() {
		return Formats.dateFormat(submissionTimestamp);
	}

	/**
	 * @return Returns the submissionTimestamp.
	 */
	public Timestamp getSubmissionTimestamp() {
		return submissionTimestamp;
	}
	/**
	 * @param submissionTimestamp The submissionTimestamp to set.
	 */
	public void setSubmissionTimestamp(Timestamp submissionTimestamp) {
		this.submissionTimestamp = submissionTimestamp;
	}



	public boolean isMostRecent() {
		return mostRecent;
	}

	public void setMostRecent(boolean mostRecent) {
		this.mostRecent = mostRecent;
	}

	/**
	 * @return Returns the pluginVersion.
	 */
	public String getSubmitClient() {
		return submitClient;
	}
	/**
	 * @param pluginVersion The pluginVersion to set.
	 */
	public void setSubmitClient(String pluginVersion) {
		this.submitClient = pluginVersion;
	}

	/**
	 * Uploads the bytes of a cached archive to the database.
	 * @param conn the connection to the database
	 * @return the archivePK of the newly uploaded archive
	 * @throws SQLException
	 */
	public int uploadCachedArchive(Connection conn) throws SQLException {
		return uploadSubmissionArchive(cachedArchive, conn);
	}

	public static int uploadSubmissionArchive(byte[] bytes, Connection conn)
			throws SQLException {
		return Archive.uploadBytesToArchive(SUBMISSION_ARCHIVES, bytes, conn);
	}
	public static void deleteAbortedSubmissionArchive(int pk, Connection conn)
	throws SQLException {
		 Archive.deleteArchiveEntry(SUBMISSION_ARCHIVES, pk, conn);
	}

	@Deprecated
    public void updateCachedArchive(Connection conn)
    throws SQLException
    {
        Archive.updateBytesInArchive(SUBMISSION_ARCHIVES, archivePK, cachedArchive, conn);
    }
	/**
	 * Does this submission have an archive cached as bytes ready for upload to the database?
	 * @return true if this submission has a cached archive, false otherewise
	 */
	public boolean hasCachedArchive()
	{
	    return cachedArchive != null;
	}
	/**
	 * Sets the byte array of the archive for upload to the database.
	 * @param bytes array of bytes of the cached archive
	 */
	public void setArchiveForUpload(byte[] bytes)
	{
	    cachedArchive = bytes;
	}

	/**
	 * Downloads the bytes of the archive from the database and returns them directly.
	 * @param conn the connection to the database
	 * @return an array of bytes of the cached archive
	 * @throws SQLException
	 */
	public byte[] downloadArchive(Connection conn)
    throws SQLException
    {
	    return Archive.downloadBytesFromArchive(SUBMISSION_ARCHIVES, getArchivePK(), conn);
    }

	public Map<String,List<String>>
    getText(Connection conn) throws SQLException, IOException {
     return getText(conn, null);
}
	public Map<String,List<String>>
		getText(Connection conn, @CheckForNull DisplayProperties fileProperties) throws SQLException, IOException {
		  byte[] archive = downloadArchive(conn);
		  return TextUtilities.scanTextFilesInZip(archive, fileProperties);
	}

    /**
     * @return Returns the numTestOutcomes.
     */
    public int getNumTestRuns() {
        return numTestRuns;
    }
    /**
     * @param numTestOutcomes The numTestOutcomes to set.
     */
    public void setNumTestRuns(int numTestOutcomes) {
        this.numTestRuns = numTestOutcomes;
    }
    /**
     * @return Returns the currentTestRunPK.
     */
    public @CheckForNull Integer getCurrentTestRunPK()
    {
        return currentTestRunPK;
    }
    /**
     * @param currentTestRunPK The currentTestRunPK to set.
     */
    public void setCurrentTestRunPK(Integer currentTestRunPK)
    {
        this.currentTestRunPK = currentTestRunPK;
    }
    /**
     * @return Returns the adjustedScore.
     */
    public int getAdjustedScore()
    {
        return adjustedScore;
    }
    /**
     * @param adjustedScore The adjustedScore to set.
     */
    public void setAdjustedScore(Project project) {
        setAdjustedScore(project, 0);
    }
    /**
     * @param adjustedScore The adjustedScore to set.
     * @param extension     The extension received, if any, for this submission
     */
    public void setAdjustedScore(Project project, int extension)
    {
    	if (!project.isTested())
    		return;
        // check if this submission is late...
        Timestamp extendedOntime = new Timestamp(project.getOntime().getTime() + (extension*MILLISECONDS_IN_ONE_HOUR));

        if (submissionTimestamp.after(extendedOntime))
        {
            if (project.getKindOfLatePenalty().equals(Project.CONSTANT))
            {
                adjustedScore = Math.max(0, getValuePassedOverall() - project.getLateConstant());
            }
            else if (project.getKindOfLatePenalty().equals(Project.MULTIPLIER))
            {
                adjustedScore = Math.max(0, (int)(getValuePassedOverall() * project.getLateMultiplier()));
            }
            else {
                throw new IllegalStateException("Late penalties for a project must be " +
                        "either " +Project.CONSTANT +" or "+ Project.MULTIPLIER);
            }
        }
        else
        {
            adjustedScore = getValuePassedOverall();
        }
    }
    /**
     * @return Returns the status.
     */
    public String getStatus()
    {
        return status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(Project project)
    {
    	// Important to include equals case, otherwise some on-time
    	// submissions are marked as very late
        if (project.getOntime().after(submissionTimestamp) ||
        	project.getOntime().equals(submissionTimestamp))
        {
            status = ON_TIME;
        }
        else if (submissionTimestamp.after(project.getOntime()) &&
                submissionTimestamp.before(project.getLate()))
        {
            status = LATE;
        }
        else status = VERY_LATE;
    }

    public void setStatus(Project project, int extension)
    {
        if (extension == 0)
        {
            setStatus(project);
            return;
        }
        Timestamp extendedOntime = new Timestamp(project.getOntime().getTime() + (extension*MILLISECONDS_IN_ONE_HOUR));
        Timestamp extendedLate = new Timestamp(project.getLate().getTime() + (extension*MILLISECONDS_IN_ONE_HOUR));
        if (extendedOntime.after(submissionTimestamp))
        {
            status = ON_TIME + "-extended-" +extension;
        }
        else if (submissionTimestamp.after(extendedOntime) &&
                submissionTimestamp.before(extendedLate))
        {
            status = LATE + "-extended-" +extension;
        }
        else status = VERY_LATE;
    }

    public Submission(ResultSet resultSet, int startingFrom) throws SQLException {
    		fetchValues(resultSet, startingFrom);
    }


    /**
	 * Populate a Submission from a ResultSet that is positioned
	 * at a row of the submissions table.
	 *
	 * @param resultSet the ResultSet containing the row data
	 * @param startingFrom index specifying where to start fetching attributes from;
	 *   useful if the row contains attributes from multiple tables
	 */
	public int fetchValues(ResultSet resultSet, int startingFrom) throws SQLException {
		setSubmissionPK(asPK(resultSet.getInt(startingFrom++)));
		setStudentRegistrationPK(StudentRegistration.asPK(resultSet.getInt(startingFrom++)));
		setProjectPK(Project.asPK(resultSet.getInt(startingFrom++)));
		setNumTestRuns(resultSet.getInt(startingFrom++));
		setCurrentTestRunPK(SqlUtilities.getInteger(resultSet, startingFrom++));
		setSubmissionNumber(resultSet.getString(startingFrom++));
		setSubmissionTimestamp(resultSet.getTimestamp(startingFrom++));
		setMostRecent(resultSet.getBoolean(startingFrom++));
		setCvsTagTimestamp(resultSet.getString(startingFrom++));
		setBuildRequestTimestamp(resultSet.getTimestamp(startingFrom++));
		setBuildStatus(BuildStatus.get(resultSet, startingFrom++));
		setNumPendingBuildRequests(resultSet.getInt(startingFrom++));
		setNumSuccessfulBackgroundRetests(resultSet.getInt(startingFrom++));
		setNumFailedBackgroundRetests(resultSet.getInt(startingFrom++));
		setSubmitClient(resultSet.getString(startingFrom++));
		setReleaseRequest(resultSet.getTimestamp(startingFrom++));
		setReleaseEligible(resultSet.getBoolean(startingFrom++));
		setValuePassedOverall(resultSet.getInt(startingFrom++));
        setCompileSuccessful(resultSet.getInt(startingFrom++) > 0);
        setValuePublicTestsPassed(resultSet.getInt(startingFrom++));
        setValueReleaseTestsPassed(resultSet.getInt(startingFrom++));
        setValueSecretTestsPassed(resultSet.getInt(startingFrom++));
        setNumFindBugsWarnings(resultSet.getInt(startingFrom++));
        setNumChangedLines(resultSet.getInt(startingFrom++));
        setArchivePK(SqlUtilities.getInteger(resultSet,startingFrom++));
		return startingFrom;
	}

	public static Integer getStudentRegistrationPKFromResultSet(ResultSet resultSet) throws SQLException {
		return SqlUtilities.getInteger(resultSet, 2);
	}

	static int getExtensionFromResultSet(ResultSet rs)
	throws SQLException
	{
	    return rs.getInt("student_submit_status.extension");
	}

	/**
	 * If a submission with
	 * @param conn
	 * @throws SQLException
	 */
	public void insert(Connection conn)
	throws SQLException
	{
	    String insert = Queries.makeInsertStatement(ATTRIBUTE_NAME_LIST.length, ATTRIBUTES, TABLE_NAME);
        if (getArchivePK() == 0 && !hasCachedArchive())
            throw new IllegalStateException("there is no archive for upload, you should call setArchiveForUpload first");

        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        boolean transactionSuccess = false;

	    PreparedStatement stmt = null;
	    try {

	    	    String query = "UPDATE " + TABLE_NAME + " SET most_recent = ? "
					+ "WHERE most_recent=? AND student_registration_pk = ? AND project_pk = ?";
				stmt = conn.prepareStatement(query);

	    	    stmt.setBoolean(1, false);
	    	    stmt.setBoolean(2, true);
	    	    stmt.setInt(3, getStudentRegistrationPK());
	    	    stmt.setInt(4, getProjectPK());
	    	    stmt.executeUpdate();

	        // insert the bytes we have as a new archive in that table
	        stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

	        if (cachedArchive != null)
	        	  setArchivePK(uploadCachedArchive(conn));

	        putValues(stmt, 1);

	        stmt.executeUpdate();
	        conn.commit();
	        transactionSuccess = true;
	        setSubmissionPK(asPK(Queries.getGeneratedPrimaryKey(stmt)));
	    } finally {
	    	    if (!transactionSuccess)
	    	    	  conn.rollback();
	        Queries.closeStatement(stmt);
	    }
	}

	public static int setBuildStatusForAllSubmissions(Connection conn, Project project) 
			throws SQLException {
		return setBuildStatusForAllSubmissions(conn, project, project.isTested() ? BuildStatus.NEW : BuildStatus.ACCEPTED);
	}
			
	public static int clearCachedNumLinesChanged(Connection conn, Project project) 
	        throws SQLException
	        {
	            PreparedStatement stmt = null;
	            try {

	                    String query = "UPDATE " + TABLE_NAME + " SET num_changed_lines = ? "
	                        + "WHERE project_pk = ?";
	                    stmt = Queries.setStatement(conn, query, -1, project.getProjectPK());
	        
	                    return stmt.executeUpdate();
	            } finally {
	                Queries.closeStatement(stmt);
	            }
	        }
	
	public static int setBuildStatusForAllSubmissions(Connection conn, Project project, BuildStatus newStatus) 
	throws SQLException
	{
	    PreparedStatement stmt = null;
	    try {

	    	    String query = "UPDATE " + TABLE_NAME + " SET build_status = ?, num_pending_build_requests = ? "
					+ "WHERE project_pk = ?";
	    	    stmt = Queries.setStatement(conn, query, newStatus, 0, project.getProjectPK());
	
	    	    return stmt.executeUpdate();
	    } finally {
	        Queries.closeStatement(stmt);
	    }
	}

	public void update(Connection conn)
	throws SQLException
	{
	    if (getSubmissionPK() == null)
	        throw new IllegalStateException("You cannot try to update a submission with a null submissionPK");

	    String update = Queries.makeUpdateStatementWithWhereClause(ATTRIBUTE_NAME_LIST, TABLE_NAME, " WHERE submission_pk = ? ");

	    PreparedStatement stmt = conn.prepareStatement(update);

	    int index = putValues(stmt, 1);
	    SqlUtilities.setInteger(stmt, index, getSubmissionPK());

	    stmt.executeUpdate();
	    try {
	        stmt.close();
	    } catch (SQLException ignore) {
	        // ignore
	    }
	}

	/**
	 * Populates a prepared statement with all of the fields in this class starting at
	 * a given index.  Will return the index of the next open slot in the statement.
	 * @param stmt the PreparedStatement to populate
	 * @param index the index of the leaf of the insert graph to start at
	 * @throws SQLException
	 */
	int putValues(PreparedStatement stmt, int index)
	throws SQLException
	{
	    stmt.setInt(index++, getStudentRegistrationPK());
	    stmt.setInt(index++, getProjectPK());
	    stmt.setInt(index++, getNumTestRuns());
	    SqlUtilities.setInteger(stmt, index++, getCurrentTestRunPK());
	    stmt.setString(index++, Integer.toString(getSubmissionNumber()));
	    stmt.setTimestamp(index++, getSubmissionTimestamp());
	    stmt.setBoolean(index++, isMostRecent());
	    stmt.setString(index++, getCvsTagTimestamp());
	    stmt.setTimestamp(index++, getBuildRequestTimestamp());
	    stmt.setString(index++, getBuildStatus().name());
	    stmt.setInt(index++,getNumPendingBuildRequests());
	    stmt.setInt(index++,getNumSuccessfulBackgroundRetests());
	    stmt.setInt(index++,getNumFailedBackgroundRetests());
	    stmt.setString(index++, getSubmitClient());
	    stmt.setTimestamp(index++, getReleaseRequest());
	    stmt.setBoolean(index++, isReleaseEligible());
	    stmt.setInt(index++, getValuePassedOverall());
        stmt.setInt(index++, isCompileSuccessful() ? 1 : 0);
        stmt.setInt(index++, getValuePublicTestsPassed());
        stmt.setInt(index++, getValueReleaseTestsPassed());
        stmt.setInt(index++, getValueSecretTestsPassed());
        stmt.setInt(index++, getNumFindBugsWarnings());
        stmt.setInt(index++, getNumChangedLines());
        SqlUtilities.setInteger(stmt, index++, getArchivePK());

	    return index;
	}

	/**
	 * Finds a submission based on the submissionPK
	 *
	 * @param submissionPK the primary key of the submission
	 * @param conn the database connection to use
	 * @return the Submission object (that represents the row) if a row with submissionPK
	 * exists, null if it doesn't exist
	 *
	 * @throws SQLException
	 */
	public static Submission lookupByStudentPKAndSubmissionPK(
			@Student.PK Integer studentPK,
			@Submission.PK int submissionPK,
			Connection conn)
		throws SQLException
	{
		String query = " SELECT " +ATTRIBUTES+ " "+
		" FROM "+
		" submissions, students, student_registration "+
		" WHERE submissions.submission_pk= ? " +
		" AND submissions.submission_timestamp IS NOT NULL " +
		" AND students.student_pk = ? " +
		" AND submissions.student_registration_pk = student_registration.student_registration_pk " +
		" AND students.student_pk = student_registration.student_pk ";

		PreparedStatement stmt = null;

		stmt = conn.prepareStatement(query);
		SqlUtilities.setInteger(stmt, 1, submissionPK);
        SqlUtilities.setInteger(stmt, 2, studentPK);

		return getFromPreparedStatement(stmt);
	}

	/**
	 * Retrieves a submission from a database by its submissionPK.
	 * @param submissionPK
	 * @param conn
	 * @return the submission; null if no submission exists with the given submissionPK
	 * @throws SQLException
	 */
	public static Submission lookupBySubmissionPK(
			@Submission.PK int submissionPK,
			Connection conn)
		throws SQLException
	{
		// FIXME: Should submission_timestamp != null be enforced here?
	    String query = " SELECT " +ATTRIBUTES+ " "+
		" FROM "+
		" submissions "+
		" WHERE submissions.submission_pk= ? ";

		PreparedStatement stmt = conn.prepareStatement(query);
		SqlUtilities.setInteger(stmt, 1, submissionPK);

		return getFromPreparedStatement(stmt);
	}

	/**
	 * Executes the given preparedStatement to return a Submission.
	 * @param stmt the preparedStatement
	 * @return the submission returned by the preparedStatement; null if no submission
	 * is found.
	 * @throws SQLException
	 */
	private static Submission getFromPreparedStatement(PreparedStatement stmt)
	throws SQLException
	{
	    try {
	        ResultSet rs = stmt.executeQuery();

	        if (rs.first())
	        {
	            return new Submission(rs, 1);
	        }
	        return null;
	    }
	    finally {
	        Queries.closeStatement(stmt);
	    }
	}

    /**
     * Gets a submission from a prepared statement, but doesn't close the statement.
     * Eventually this method should replace all calls to getFromPreparedStatement,
     * which can possibly leak statements.
     * @param stmt
     * @return
     * @throws SQLException
     */
    private static Submission getFromPreparedStatementDontClose(PreparedStatement stmt)
    throws SQLException
    {
        ResultSet rs = stmt.executeQuery();

        if (rs.first())
        {
        		return new Submission(rs, 1);
        }
        return null;
    }

    /**
     * @return Returns the archivePK.
     */
    public Integer getArchivePK()
    {
        return archivePK;
    }
    /**
     * @param archivePK The archivePK to set.
     */
    public void setArchivePK(Integer archivePK)
    {
        this.archivePK = archivePK;
    }

    /**
     * @param project
     * @param stmt
     * @return
     * @throws SQLException
     */
    private static Map<Integer, Submission> getLastSubmissionMapFromStmt(Project project, PreparedStatement stmt)
    throws SQLException {
        Map<Integer, Submission> result = new HashMap<Integer, Submission>();
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Integer studentRegistrationPK = getStudentRegistrationPKFromResultSet(rs);
        	if (result.containsKey(studentRegistrationPK)) continue;
        	Submission submission =  new Submission(rs, 1);
       // set late status
        	submission.setStatus(project);
        	// adjust final score based on late status and late penalty
        	submission.setAdjustedScore(project);
        	result.put(studentRegistrationPK, submission);
        }
        return result;
    }

    /**
     * Finds the last submission.
     * @param studentRegistrationPK
     * @param projectPK
     * @param conn
     * @return the last very-late submission; null if there were no very late submissions
     */
    static public Map<Integer, Submission> lookupLastSubmissionBeforeTimestampMap(
            Project project,
            Timestamp when,
            Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM submissions " +
            " WHERE submissions.project_pk = ? " +
            " AND submissions.submission_timestamp < ? " +
            " ORDER BY submission_timestamp desc ";

        PreparedStatement stmt = null;
        try {
        	stmt = conn.prepareStatement(query);
        	SqlUtilities.setInteger(stmt, 1, project.getProjectPK());
            stmt.setTimestamp(2, when);

        	return getLastSubmissionMapFromStmt(project, stmt);
        } finally {
        	Queries.closeStatement(stmt);
        }
    }





    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
	public int compareTo(Submission arg0) {
        Submission that = (Submission) arg0;
        int result =  getValuePassedOverall() - that.getValuePassedOverall();
        if (result != 0)
            return result;
        result = this.getSubmissionTimestamp().compareTo(that.getSubmissionTimestamp());
        if (result != 0)
            return result;
        return this.getSubmissionPK() - that.getSubmissionPK();
    }

    /**
     * Looks up a submission whose currentTestRunPK is equal to the given testRunPK.
     * @param testRunPK the testRunPK
     * @param conn the connection to the database
     * @return the submission; null if no such submission exists
     */
    public static Submission lookupByTestRunPK(Integer testRunPK, Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM submissions " +
            " WHERE submissions.current_test_run_pk = ? ";

        PreparedStatement stmt = conn.prepareStatement(query);
        SqlUtilities.setInteger(stmt, 1, testRunPK);
        return getFromPreparedStatement(stmt);
    }

    public static List<Submission> lookupAllByStudentPKAndProjectPK(
    		@Student.PK Integer studentPK,
            Integer projectPK,
    		Connection conn)
    throws SQLException
    {
    	String query = "SELECT " +ATTRIBUTES+ " "+
    	" FROM " +
    	" submissions, student_registration " +
    	" WHERE student_registration.student_pk = ? " +
    	" AND submission_timestamp IS NOT NULL " +
    	" AND student_registration.student_registration_pk = submissions.student_registration_pk " +
    	" AND submissions.project_pk = ? " +
    	" ORDER BY submissions.submission_timestamp ASC ";

    	PreparedStatement stmt = conn.prepareStatement(query);

    	SqlUtilities.setInteger(stmt, 1, studentPK);
        SqlUtilities.setInteger(stmt, 2, projectPK);

    	return getListFromPreparedStatement(stmt);
    }

    public static List<Submission> lookupAllReleaseTestedStudentSubmissionsByProjectPK(
        Integer projectPK,
        Connection conn)
    throws SQLException
    {
        String query = "SELECT " +ATTRIBUTES+ " "+
        " FROM " +
        " submissions, student_registration " +
        " WHERE submission_timestamp IS NOT NULL " +
        " AND submissions.project_pk = ? " +
        " AND submissions.release_request IS NOT NULL " +
        " AND submissions.student_registration_pk = student_registration.student_registration_pk " +
        " AND student_registration.instructor_capability IS NULL ";

        PreparedStatement stmt = conn.prepareStatement(query);

        SqlUtilities.setInteger(stmt, 1, projectPK);

        return getListFromPreparedStatement(stmt);
    }

    public static List<Submission> lookupAllStudentSubmissionsByProjectPK(
    Integer projectPK,
    Connection conn)
    throws SQLException
    {
        String query = "SELECT " +ATTRIBUTES+ " "+
        " FROM " +
        " submissions, student_registration, projects " +
        " WHERE submission_timestamp IS NOT NULL " +
        " AND submissions.project_pk = ? " +
        " AND submissions.student_registration_pk = student_registration.student_registration_pk " +
        " AND student_registration.instructor_capability IS NULL " +
        " AND submissions.project_pk = projects.project_pk ";

        PreparedStatement stmt = conn.prepareStatement(query);

        SqlUtilities.setInteger(stmt, 1, projectPK);

        return getListFromPreparedStatement(stmt);
    }

    public static List<Submission> lookupAllByProjectPK(
    		int projectPK,
    		Connection conn)
    throws SQLException
    {
    	String query = "SELECT " +ATTRIBUTES+ " "+
    	" FROM " +
    	" submissions " +
    	" WHERE submission_timestamp IS NOT NULL " +
    	" AND submissions.project_pk = ? ";

    	PreparedStatement stmt = conn.prepareStatement(query);

    	SqlUtilities.setInteger(stmt, 1, projectPK);

    	return getListFromPreparedStatement(stmt);
    }
    public static Collection<Submission> lookupAllByArchivePK(
    		int archivePK,
    		Connection conn)
    throws SQLException
    {
    	String query = "SELECT " +ATTRIBUTES+ " "+
    	" FROM " +
    	" submissions " +
    	" WHERE archive_pk = ? ";

    	PreparedStatement stmt = conn.prepareStatement(query);

    	SqlUtilities.setInteger(stmt, 1, archivePK);

    	return getListFromPreparedStatement(stmt);
    }


    public static List<Submission> lookupAllForReleaseTesting(
    		@Student.PK Integer studentPK,
            Integer projectPK,
    		Connection conn)
    throws SQLException
    {
    	String query = " SELECT " +ATTRIBUTES+ " "+
    	" FROM " +
    	" submissions, student_registration " +
    	" WHERE student_registration.student_pk = ? " +
    	" AND submission_timestamp IS NOT NULL " +
    	" AND student_registration.student_registration_pk = submissions.student_registration_pk " +
    	" AND submissions.project_pk = ? "+
    	" AND submissions.release_request IS NOT NULL " +
    	" ORDER BY submissions.release_request DESC";

    	PreparedStatement stmt = conn.prepareStatement(query);

    	SqlUtilities.setInteger(stmt, 1, studentPK);
        SqlUtilities.setInteger(stmt, 2, projectPK);

    	return getListFromPreparedStatement(stmt);
    }

    private static List<Submission> getListFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
    		ResultSet rs = stmt.executeQuery();

    		List<Submission> submissions = new ArrayList<Submission>();

    		while (rs.next())
    		{
    			Submission submission = new Submission(rs, 1);
    			submissions.add(submission);
    		}
    		return submissions;
    	} finally {
    		Queries.closeStatement(stmt);
    	}
    }

    private static void deleteFindBugsForTestRunPK(Integer testRunPK, Connection conn)
    throws SQLException
    {
    	String query=
    		" DELETE FROM test_outcomes " +
    		" WHERE test_run_pk = ? " +
    		" AND test_type = 'findbugs' ";
    	PreparedStatement stmt=null;
    	try {
    		stmt = conn.prepareStatement(query);
    		SqlUtilities.setInteger(stmt, 1, testRunPK);
    		stmt.execute();
    	} finally {
    		Queries.closeStatement(stmt);
    	}
    }

    public static void replaceFindBugsOutcomes(
    		TestOutcomeCollection newBugWarnings,
    		 @Submission.PK int submissionPK,
    		Connection conn)
    throws SQLException
    {
    	conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        boolean transactionSuccess=false;
        try {
            Submission submission = lookupBySubmissionPK(submissionPK, conn);
            if (submission == null)
                throw new SQLException("Cannot find submissionPK = " +submissionPK);
            if (!submission.isCompileSuccessful())
                throw new IllegalStateException("SubmissionPK = " +submissionPK+ " did not compile!");

            TestRun testRun = TestRun.lookupByTestRunPK(
            		submission.getCurrentTestRunPK(),
            		conn);

            // Delete the old findbugs warnings.
            deleteFindBugsForTestRunPK(testRun.getTestRunPK(), conn);

            // Set the testRunPKs so that these warnings to the correct place.
            for (TestOutcome warning : newBugWarnings.getFindBugsOutcomes()) {
            	warning.setTestRunPK(testRun.getTestRunPK());
            }
            // Insert the new findbugs warnings.
            newBugWarnings.insert(conn);
        } finally {
        	try {
        		if (!transactionSuccess)
        			conn.rollback();
        	} catch (SQLException ignore) {
        		// ignore
        	}
        }
    }

    /**
     * Loads a new set of findbugs outcomes.  The algorithm is as follows:<br>
     * <ul>
     * <li> create a new outcomeCollection is a copy of the existing
     * outcomeCollection minus the FindBugs outcomes
     * <li> insert the new FindBugs outcomes into the new outcomeCollection
     * <li> create and insert into the database a new testRun that is the copy
     * of the exiting testRun but with an updated FindBugs count
     * <li> set the new outcomeCollection's testRunPK to match the new testRun record just inserted
     * <li> insert the new outcomeCollection into the DB
     * <li> update the submission's currentTestRunPK
     * <li> update the submission's numTestOutcomes
     * <li> update the submission's numFindBugsWarnings
     * </ul>
     * @param newFindBugsCollection the collection of new FindBugs warnings
     * @param submissionPK the submissionPK of the submission to be updated
     * @param conn the connection to the database
     * @throws SQLException
     */
    public static void loadNewFindBugsOutcomes(
            TestOutcomeCollection newFindBugsCollection,
            @Submission.PK int submissionPK,
            Connection conn)
    throws SQLException
    {
        // set the lowest transaction level
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        boolean transactionSuccess=false;
        try {
            Submission submission = lookupBySubmissionPK(submissionPK, conn);
            if (submission == null)
                throw new SQLException("Cannot find submissionPK = " +submissionPK);
            if (!submission.isCompileSuccessful())
                throw new IllegalStateException("SubmissionPK = " +submissionPK+ " did not compile!");

            TestRun testRun = TestRun.lookupByTestRunPK(submission.getCurrentTestRunPK(), conn);
            TestOutcomeCollection testOutcomeCollection = TestOutcomeCollection.lookupByTestRunPK(
                    testRun.getTestRunPK(),
                    conn);

            // create a new collection of testOutcomes and copy over everything but the findbugs outcomes
            TestOutcomeCollection newCollection = new TestOutcomeCollection();
            for (Iterator<TestOutcome> ii=testOutcomeCollection.iterator(); ii.hasNext();)
            {
                TestOutcome outcome = ii.next();
                if (!outcome.getTestType().equals(TestOutcome.FINDBUGS_TEST)) {
                    newCollection.add(outcome);
                }
            }

            // insert the new findbugs outcomes
            for (Iterator<TestOutcome> ii=newFindBugsCollection.iterator(); ii.hasNext();)
            {
                newCollection.add(ii.next());
            }

            // clone the testRun object, set the fields that have changed
            TestRun newTestRun = (TestRun)testRun.clone();
            newTestRun.setTestRunPK(null);
            newTestRun.setNumFindBugsWarnings(newFindBugsCollection.size());
            newTestRun.setTestTimestamp(new Timestamp(System.currentTimeMillis()));

            //System.err.println("old testRunPK: " +testRun.getTestRunPK());

            // insert the new testRun row
            newTestRun.insert(conn);

            //System.err.println("new testRunPK: " +newTestRun.getTestRunPK());

            // update the current test run row for this submission
            newCollection.updateTestRunPK(newTestRun.getTestRunPK());
            newCollection.insert(conn);

            // set the currentTestRunPK for this submission to the newly created testRun record
            // and increment the number of test runs
            // and set the new number of findbugs warnings
            submission.setCurrentTestRunPK(newTestRun.getTestRunPK());
            submission.setNumTestRuns(submission.getNumTestRuns() + 1);
            submission.setNumFindBugsWarnings(newFindBugsCollection.size());
            submission.update(conn);

            conn.commit();
            transactionSuccess=true;

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (!transactionSuccess)
                    conn.rollback();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }

    public static void lookupAllRecentBrokenSubmissions(
            Timestamp when, int limit,
            List<Submission> submissionList,
            Connection conn)
        throws SQLException
        {

            String query =
                " SELECT " +ATTRIBUTES
                + " FROM submissions "
                + " WHERE submissions.build_status = ?"
                + " AND submissions.submission_timestamp >= ? "
                + " ORDER BY submissions.submission_timestamp DESC LIMIT ?";
               
            PreparedStatement stmt=Queries.setStatement(conn, query, Submission.BuildStatus.BROKEN, when, limit);
            try {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Submission submission = new Submission(rs, 1);
                    submissionList.add(submission);
                }
            } finally {
                Queries.closeStatement(stmt);
            }
        }
    
    public static void lookupAllWithFailedBackgroundRetestsByProjectPK(
        @Project.PK int projectPK,
        List<Submission> submissionList,
        Connection conn)
    throws SQLException
    {

    	String query =
            " SELECT " +ATTRIBUTES
            + " FROM submissions, test_runs, projects "
    		+ " WHERE "
    		+ "     submissions.project_pk = projects.project_pk "
    		+ " AND submissions.current_test_run_pk = test_runs.test_run_pk "
    		+ " AND projects.project_pk = ? "
    		+ " AND test_runs.test_setup_pk = projects.test_setup_pk "
    		+ " AND num_failed_background_retests > 0";

        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(query);
            SqlUtilities.setInteger(stmt, 1, projectPK);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Submission submission = new Submission(rs, 1);
                submissionList.add(submission);
            }
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    public static void lookupAllWithFailedBackgroundRetestsByProjectPK(
            @Project.PK int projectPK,
            @StudentRegistration.PK  int studentRegistrationPK,
            List<Submission> submissionList,
            Connection conn)
        throws SQLException
        {

            String query =
                " SELECT " +ATTRIBUTES
                + " FROM submissions, test_runs, projects "
                + " WHERE "
                + "     submissions.project_pk = projects.project_pk "
                + " AND submissions.current_test_run_pk = test_runs.test_run_pk "
                + " AND projects.project_pk = ? "
                + " AND submissions.student_registration_pk = ? "
                + " AND test_runs.test_setup_pk = projects.test_setup_pk "
                + " AND num_failed_background_retests > 0";

            PreparedStatement stmt=null;
            try {
                stmt = Queries.setStatement(conn, query, projectPK, studentRegistrationPK);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Submission submission = new Submission(rs, 1);
                    submissionList.add(submission);
                }
            } finally {
                Queries.closeStatement(stmt);
            }
        }

    /**
     * Adjusts the scores of this submission based on the results of background retests.
     * If any of the background retests have failed, then the entire test case is
     * marked as failed.
     * @param conn Connection to the database.
     * @return The adjusted TestOutcomeCollection.
     * @throws SQLException
     */
    public TestOutcomeCollection setAdjustScoreBasedOnFailedBackgroundRetests(Connection conn)
    throws SQLException
    {
        Project project=Project.lookupByProjectPK(getProjectPK(),conn);
        TestRun currentTestRun=TestRun.lookupByTestRunPK(getCurrentTestRunPK(),conn);
        List<TestOutcomeCollection> allTestOutcomeCollections =
            TestOutcomeCollection.lookupAllBySubmissionPKAndTestSetupPK(
                getSubmissionPK(),
                currentTestRun.getTestSetupPK(),
                conn);
        TestOutcomeCollection bestCollection=allTestOutcomeCollections.get(0);
        // If we have more than 1 test run for this submission...
        // Set the scores for the "best" collection of test outcomes to the
        // minimum achieved in any collection of test outcomes.
        if (allTestOutcomeCollections.size() > 1) {
            for (int ii=1; ii < allTestOutcomeCollections.size(); ii++) {
                TestOutcomeCollection collectionToCompare=allTestOutcomeCollections.get(ii);
                for (TestOutcome bestOutcome : bestCollection.getIterableForCardinalTestTypes()) {
                    if (bestOutcome.isPassed()) {
                        TestOutcome outcomeToCompare=collectionToCompare.getOutcomeByTestTypeAndTestNumber(
                            bestOutcome.getTestType(),
                            bestOutcome.getTestNumber());
                        if (outcomeToCompare.isFailed()) {
                            bestOutcome.setOutcome(outcomeToCompare.getOutcome());
                        }
                    }
                }
            }
        }
        // Adjust all the scores of this submission.
        setValuePublicTestsPassed(bestCollection.getValuePublicTestsPassed());
        setValueReleaseTestsPassed(bestCollection.getValueReleaseTestsPassed());
        setValueSecretTestsPassed(bestCollection.getValueSecretTestsPassed());
        setValuePassedOverall(bestCollection.getValuePassedOverall());
        // Set the adjusted score, including any late penalties.
        setStatus(project);
        setAdjustedScore(project);
        return bestCollection;
    }

    public static List<Submission> lookupAllByStudentRegistrationPKAndProjectPK(
        Integer studentRegistrationPK,
        Integer projectPK,
        Connection conn)
    throws SQLException
    {
        String query=
            " SELECT " +ATTRIBUTES+
            " FROM submissions " +
            " WHERE student_registration_pk = ? " +
            " AND project_pk = ? " +
            " ORDER BY submission_number ";

        PreparedStatement stmt=conn.prepareStatement(query);
        SqlUtilities.setInteger(stmt, 1, studentRegistrationPK);
        SqlUtilities.setInteger(stmt, 2, projectPK);
        return getListFromPreparedStatement(stmt);
    }

    public static int countSubmissions(StudentRegistration sr, 
            Connection conn)
        throws SQLException
        {
            String query=
                " SELECT COUNT(*) " +
                " FROM submissions " +
                " WHERE student_registration_pk = ? ";

            PreparedStatement stmt=conn.prepareStatement(query);
            SqlUtilities.setInteger(stmt, 1, sr.getStudentRegistrationPK());
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            	return rs.getInt(1);
            return 0;
        }
    
    public static Submission submitOneSubmission(
        byte[] bytesForUpload,
        String cvsTagTimestamp,
        String classAccount,
        String projectNumber,
        String courseName,
        String semester,
        Connection conn)
    throws SQLException
    {
        boolean transactionSuccess=false;
        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            // look up project
            Project project = Project.lookupByCourseProjectSemester(
                    courseName,
                    projectNumber,
                    semester,
                    conn);

            // look up studentRegistration
            StudentRegistration studentRegistration = StudentRegistration.lookupByCvsAccountAndCoursePK(
                    classAccount,
                    project.getCoursePK(),
                    conn);
            if (studentRegistration == null) {
                // FIXME throw a more descriptive exception than SQLException
                throw new SQLException(classAccount +" is not registered for " +courseName+" in " +semester);
            }

//          ensure that this snapshot has not already been uploaded
            Submission previouslyUploaded = Submission.lookupByCvsTagTimestamp(
                cvsTagTimestamp,
                studentRegistration.getStudentRegistrationPK(),
                project.getProjectPK(),
                conn);

            if (previouslyUploaded != null) {
                // FIXME throw an exception instead of returning null
               throw new SQLException("Already submitted, cvsTagTimestamp: " +cvsTagTimestamp+
                        ", studentRegistrationPK: "
                        +studentRegistration.getStudentRegistrationPK()+
                        ", projectPK: " +project.getProjectPK());
            }

//          find StudentSubmitStatus record
            StudentSubmitStatus studentSubmitStatus
            = StudentSubmitStatus.createOrInsert(
                        project.getProjectPK(),
                        studentRegistration.getStudentRegistrationPK(),
                        conn);


            int submitNumber = -1;
            submitNumber = studentSubmitStatus.getNumberSubmissions() + 1;
            studentSubmitStatus.setNumberSubmissions(submitNumber);
            studentSubmitStatus.update(conn);

            // prepare new snapshot record
            Submission submission=new Submission();
            submission.setStudentRegistrationPK(studentRegistration.getStudentRegistrationPK());

            submission.setProjectPK(project.getProjectPK());

            // Set the cvsTagTimestamp
            submission.setCvsTagTimestamp(cvsTagTimestamp);

            submission.setCvsTagTimestamp(cvsTagTimestamp);
            submission.setSubmissionNumber(Integer.toString(submitNumber));
            // Figure out the submissionTimestamp
            // Note that the cvsTagTimestamp will be:
            // tXXXXXXXXXXXXXXXXX (basically "t" prepended to a call to System.currentTimeMillis()
            long submissionTimesetamp=Long.parseLong(cvsTagTimestamp.substring(1));
            submission.setSubmissionTimestamp(new Timestamp(submissionTimesetamp));

            submission.setBuildStatus(project.getInitialBuildStatus());
            // This is DirectSubmissionUpload
            submission.setSubmitClient("directSubmissionUpload");

            // set the byte array as the archive
            submission.setArchiveForUpload(bytesForUpload);

            submission.insert(conn);
            conn.commit();
            transactionSuccess = true;
            return submission;
        } finally {
            Queries.rollbackIfUnsuccessful(transactionSuccess, conn);
        }
    }


    private static Submission lookupByCvsTagTimestamp(String cvsTagTimestamp,
        Integer studentRegistrationPK,
        Integer projectPK,
        Connection conn)
    throws SQLException
    {
        String sql =
            " SELECT " +ATTRIBUTES+
            " FROM submissions " +
            " WHERE cvstag_timestamp = ? " +
            " AND student_registration_pk = ? " +
            " AND project_pk = ? ";
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(sql);
            stmt.setString(1, cvsTagTimestamp);
            SqlUtilities.setInteger(stmt, 2, studentRegistrationPK);
            SqlUtilities.setInteger(stmt, 3, projectPK);
            return getFromPreparedStatementDontClose(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /**
     * Submit a new submission to the database using a byte array containing the zip archive
     * of the submission and other parameters describing the submission.
     * @param bytesForUpload A byte array containing the zip archive of the submission.
     * @param studentRegistration The student registration making the submission.
     * @param project The project this submission is for.
     * @param cvstagTimestamp The cvstag timestamp.
     * @param submitClientTool The client tool used for the submission (could be Eclipse, web,
     *      ProjectImportTool, DirectSnapshotUploadTool, etc)
     * @param submitClientVersion The version of the client upload tool.
     * @param submissionTimestamp The timestamp when the submission happened.  This is passed
     *      in as a parameter because some of the submissionClientTools will date submissions
     *      differently, for example, the DirectSnapshotUpload tool dumps CVS snapshots
     *      and therefore uses the dates of when the snapshots were recorded.
     * @param conn Connection to the database.
     * @return The submission object that's been uploaded into the database.
     * @throws SQLException If something goes wrong communicating with the database.
     */
    public static Submission submit(
        byte[] bytesForUpload,
        StudentRegistration studentRegistration,
        Project project,
        String cvstagTimestamp,
        String submitClientTool,
        String submitClientVersion,
        Timestamp submissionTimestamp,
        Connection conn)
    throws SQLException
    {
        Submission submission = prepareSubmission(studentRegistration, project,
				cvstagTimestamp, submitClientTool, submitClientVersion,
				submissionTimestamp, conn);

        // set the byte array as the archive
        submission.setArchiveForUpload(bytesForUpload);

        submission.insert(conn);
        return submission;
    }
    /**
     * Submit a new submission to the database using a byte array containing the zip archive
     * of the submission and other parameters describing the submission.
     * @param archivePK primary key for an already loaded submission archive blob
     * @param studentRegistration The student registration making the submission.
     * @param project The project this submission is for.
     * @param cvstagTimestamp The cvstag timestamp.
     * @param submitClientTool The client tool used for the submission (could be Eclipse, web,
     *      ProjectImportTool, DirectSnapshotUploadTool, etc)
     * @param submitClientVersion The version of the client upload tool.
     * @param submissionTimestamp The timestamp when the submission happened.  This is passed
     *      in as a parameter because some of the submissionClientTools will date submissions
     *      differently, for example, the DirectSnapshotUpload tool dumps CVS snapshots
     *      and therefore uses the dates of when the snapshots were recorded.
     * @param conn Connection to the database.
     * @return The submission object that's been uploaded into the database.
     * @throws SQLException If something goes wrong communicating with the database.
     */
    public static Submission submit(
        int archivePK,
        StudentRegistration studentRegistration,
        Project project,
        String cvstagTimestamp,
        String submitClientTool,
        String submitClientVersion,
        Timestamp submissionTimestamp,
        Connection conn)
    throws SQLException
    {
        Submission submission = prepareSubmission(studentRegistration, project,
				cvstagTimestamp, submitClientTool, submitClientVersion,
				submissionTimestamp, conn);

        // set the byte array as the archive
        submission.setArchivePK(archivePK);

        submission.insert(conn);
        return submission;
    }
	/**
	 * @param studentRegistration
	 * @param project
	 * @param cvstagTimestamp
	 * @param submitClientTool
	 * @param submitClientVersion
	 * @param submissionTimestamp
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private static Submission prepareSubmission(
			StudentRegistration studentRegistration, Project project,
			String cvstagTimestamp, String submitClientTool,
			String submitClientVersion, Timestamp submissionTimestamp,
			Connection conn) throws SQLException {
		// find StudentSubmitStatus record
        StudentSubmitStatus studentSubmitStatus
        = StudentSubmitStatus.createOrInsert(
                    project.getProjectPK(),
                    studentRegistration.getStudentRegistrationPK(),
                    conn);


        if (project.getCanonicalStudentRegistrationPK() ==
                studentRegistration.getStudentRegistrationPK())
            TestSetup.resetAllFailedTestSetups(project.getProjectPK(),
                    conn);
        int submissionNumber = studentSubmitStatus.getNumberSubmissions() + 1;
        // figure out how many submissions have already been made
        studentSubmitStatus.setNumberSubmissions(submissionNumber);
        studentSubmitStatus.update(conn);

        // prepare new submission record
        Submission submission = new Submission();
        submission.setStudentRegistrationPK(studentRegistration
                .getStudentRegistrationPK());
        submission.setProjectPK(project.getProjectPK());
        //submission.setNumTestOutcomes(0);
        submission.setSubmissionNumber(Integer.toString(submissionNumber));
        submission.setSubmissionTimestamp(submissionTimestamp);
        // OK if this is null
        submission.setCvsTagTimestamp(cvstagTimestamp);
        submission.setBuildStatus(project.getInitialBuildStatus());
        // figure out the type and version of the submit client
        if (submitClientVersion != null)
            submitClientTool += "-" + submitClientVersion;
        submission.setSubmitClient(submitClientTool);
		return submission;
	}

	public static Collection<Submission> getSubmissionsUnderReview(
			int codeReviewAssignmentPK,
			Connection conn) throws SQLException {
		String query = "SELECT DISTINCT " + Submission.ATTRIBUTES
				+ " FROM " + CodeReviewer.TABLE_NAME
						+ "," + Submission.TABLE_NAME
				+ " WHERE  code_reviewer.code_review_assignment_pk = ? "
				+ " AND  submissions.submission_pk =  code_reviewer.submission_pk";


		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setInt(1, codeReviewAssignmentPK);
			ResultSet rs = stmt.executeQuery();
			LinkedList<Submission> result = new LinkedList<Submission>();

			while (rs.next())
				result.add( new Submission(rs, 1));
			return result;
		} finally {
			stmt.close();
		}
	}
	
	/** Returns the set of submission_pk's that have published reviews */
    public static Set<Integer> lookupSubmissionsWithReviews(Project project, Connection conn) throws SQLException {
        HashSet<Integer> result = new HashSet<Integer>();
        String query = "SELECT DISTINCT submissions.submission_pk FROM code_review_thread, submissions "
                + " WHERE submissions.submission_pk = code_review_thread.submission_pk " 
                + " AND submissions.project_pk = ? "
                + " AND EXISTS (SELECT * FROM code_review_comment " 
                + "   WHERE code_review_thread.code_review_thread_pk "
                + "   = code_review_comment.code_review_thread_pk " 
                + "   and code_review_comment.draft = 0)";
        PreparedStatement stmt = Queries.setStatement(conn, query, project.getProjectPK());
        try {
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                result.add(rs.getInt(1));
           
        } finally {
            stmt.close();
        }
        query = "SELECT DISTINCT submissions.submission_pk FROM code_review_thread, submissions "
                + " WHERE submissions.submission_pk = code_review_thread.submission_pk " 
                + " AND submissions.project_pk = ? "
                + " AND EXISTS (SELECT * FROM rubric_evaluations " 
                + "   WHERE code_review_thread.code_review_thread_pk "
                + "   = rubric_evaluations.code_review_thread_pk " 
                + "   AND rubric_evaluations.status = 'LIVE')";
        stmt = Queries.setStatement(conn, query, project.getProjectPK());
        try {
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                result.add(rs.getInt(1));
        } finally {
            stmt.close();
        }
        return result;
    }
    /** Returns the set of submission_pk's that have published reviews */
    public static Set<Integer> lookupSubmissionsWithReviews(Project project, StudentRegistration studentRegistration,
            Connection conn) throws SQLException {
        if (project == null)
            throw new NullPointerException("no project");
        if (studentRegistration == null)
            throw new NullPointerException("no studentRegistration");
        HashSet<Integer> result = new HashSet<Integer>();
        String query = "SELECT DISTINCT submissions.submission_pk FROM code_review_thread, submissions "
                + " WHERE submissions.submission_pk = code_review_thread.submission_pk " 
                + " AND submissions.project_pk = ? "
                + " AND submissions.student_registration_pk  = ? "
                + " AND EXISTS (SELECT * FROM code_review_comment " 
                + "   WHERE code_review_thread.code_review_thread_pk "
                + "   = code_review_comment.code_review_thread_pk " 
                + "   AND code_review_comment.draft = 0)";
        PreparedStatement stmt = Queries.setStatement(conn, query, project.getProjectPK(), 
                    studentRegistration.getStudentRegistrationPK());
        try {
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                result.add(rs.getInt(1));
        } finally {
            stmt.close();
        }
        query = "SELECT DISTINCT submissions.submission_pk FROM code_review_thread, submissions "
                + " WHERE submissions.submission_pk = code_review_thread.submission_pk " 
                + " AND submissions.project_pk = ? "
                + " AND submissions.student_registration_pk  = ? "
                + " AND EXISTS (SELECT * FROM rubric_evaluations " 
                + "   WHERE code_review_thread.code_review_thread_pk "
                + "   = rubric_evaluations.code_review_thread_pk " 
                + "   AND rubric_evaluations.status = 'LIVE')";
        stmt = Queries.setStatement(conn, query, project.getProjectPK(), 
                studentRegistration.getStudentRegistrationPK());
        try {
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                result.add(rs.getInt(1));
        } finally {
            stmt.close();
        }
        return result;
    }
	public static Multiset<Date> lookupSubmissionTimes(Project project, int maxAge, Connection conn) throws SQLException {
		Multiset<Date> result = new Multiset<Date>(new TreeMap<Date, Integer>());
		for(Submission s : lookupAllByProjectPK(project.getProjectPK(), conn)) {
			long timestamp = s.getSubmissionTimestamp().getTime();
			Date d = getDateHour(timestamp);
			result.add(d);
		}
		return result;
	}
	/**
	 * @param timestamp
	 * @return
	 */
	private static Date getDateHour(long timestamp) {
		Date d = new Date(timestamp);
		d.setMinutes(0);
		d.setSeconds(0);
		return d;
	}
	public static Multiset<Date> lookupSubmissionTimes(Timestamp since, Connection conn) throws SQLException {
		Multiset<Date> result = new Multiset<Date>(new TreeMap<Date, Integer>());
		String query = "SELECT  submission_timestamp "
				+ " FROM " + Submission.TABLE_NAME  
				+ " WHERE ? <= submission_timestamp ";
						
		PreparedStatement stmt = conn.prepareStatement(query);
		
		try {
		    stmt.setTimestamp(1, since);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Timestamp t = rs.getTimestamp(1);
				result.add(getDateHour(t.getTime()));
			}
		} finally {
			stmt.close();
		}
		
		return result;
	}

	public static Map<Submission,Integer> getSlowTestSubmissions(Timestamp since, int minutes,
			Connection conn) throws SQLException {
		String delay = " TIMESTAMPDIFF(MINUTE, submission_timestamp ,test_timestamp ) ";
		String query = "SELECT " + Submission.ATTRIBUTES
				+ " FROM " + TestRun.TABLE_NAME
						+ "," + Submission.TABLE_NAME
						+ "," + TestSetup.TABLE_NAME
				+ " WHERE submissions.current_test_run_pk = test_runs.test_run_pk "
				+ " AND   test_setups.test_setup_pk = test_runs.test_setup_pk "
				+ " AND   test_setups.date_posted < submissions.submission_timestamp "
				+ " AND   ? < submissions.submission_timestamp "
				+ " AND " + delay + "  > ? ";			

		PreparedStatement stmt = conn.prepareStatement(query);
		try {
			stmt.setTimestamp(1, since);
			stmt.setInt(2, minutes);
			ResultSet rs = stmt.executeQuery();
			Map<Submission,Integer> result = new LinkedHashMap<Submission, Integer>();

			while (rs.next()) {
				Submission submission = new Submission(rs, 1);
				TestRun firstRun = submission.getFirstTestRun(conn);
				long realDelay = firstRun.getTestTimestamp().getTime() - submission.getSubmissionTimestamp().getTime();
				int realDelayInMinutes = (int) TimeUnit.MINUTES.convert(realDelay, TimeUnit.MILLISECONDS);
				if (realDelayInMinutes > minutes) 
					result.put( submission, realDelayInMinutes);
			}
			return result;
		} finally {
			stmt.close();
		}
	}
	
	public TestRun getFirstTestRun(Connection conn) throws SQLException {
		Collection<TestRun> runs = TestRun.lookupAllBySubmissionPK(submissionPK, conn);
		TestRun first = null;
		for(TestRun t : runs) {
			if (first == null)
				first = t;
			else if (first.getTestTimestamp().compareTo(t.getTestTimestamp()) > 0)
				first = t;
		}
		return first;
		}
		
	  public  boolean markReviewRequest(Connection conn,
              Project project) {
          String query = "INSERT IGNORE review_requests SET " 
                  + " submission_pk = ?, course_pk = ?";
          PreparedStatement stmt = null;
          try {
          
          stmt = Queries.setStatement(conn, query, getSubmissionPK(),
                  project.getCoursePK());
          return stmt.execute();
          } catch (SQLException e) {
              e.printStackTrace();
              return false;
          } finally {
             Queries.closeStatement(stmt);
          }
      }
      public  boolean removeReviewRequest(Connection conn) {
          String query = "DELETE FROM review_requests WHERE " 
                  + " submission_pk = ?";
          PreparedStatement stmt = null;
          try {
          
          stmt = Queries.setStatement(conn, query, getSubmissionPK());
          return stmt.execute();
          } catch (SQLException e) {
              e.printStackTrace();
              return false;
          } finally {
             Queries.closeStatement(stmt);
          }
      }
      
	  public static List<Submission> lookupAllReviewRequests(
	            int coursePK,
	            Connection conn)
	    throws SQLException
	    {
	        String query = "SELECT " +ATTRIBUTES+ " "+
	        " FROM " +
	        " submissions, review_requests " +
	        " WHERE submissions.submission_pk = review_requests.submission_pk" +
	        " AND review_requests.course_pk = ? " +
	        " ORDER BY submissions.submission_timestamp ASC ";

	        PreparedStatement stmt =  Queries.setStatement(conn, query, coursePK);

	        return getListFromPreparedStatement(stmt);
	    }
}
