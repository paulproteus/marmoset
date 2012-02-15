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
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;

import edu.umd.cs.marmoset.utilities.SqlUtilities;

/**
 * @author jspacco
 *
 */
public class TestSetup
{
    private Integer testSetupPK; // non-NULL, autoincrement
    private int projectPK = 0; // non-NULL
    private String jarfileStatus=NEW;
    private int version=0;
    private Timestamp datePosted;
    private String comment;
    private Integer testRunPK; // may be NULL
    private int numTotalTests;
    private int valuePublicTests;
    private int valueReleaseTests;
    private int valueSecretTests;
    private Integer archivePK; // may be NULL

    private byte[] cachedArchive;

    /** Names of columns for this table. */
     static final String[] ATTRIBUTE_NAME_LIST = {
        "test_setup_pk",
        "project_pk",
        "jarfile_status",
        "version",
        "date_posted",
        "comment",
        "test_run_pk",
        "num_total_tests",
        "num_build_tests",
        "num_public_tests",
        "num_release_tests",
        "num_secret_tests",
        "archive_pk"
    };

    /** Name of this table in the database */
    public static final String TABLE_NAME = "test_setups";

    /** Fully-qualified attributes for test_setups table. */
    public static final String ATTRIBUTES = Queries.getAttributeList(TABLE_NAME, ATTRIBUTE_NAME_LIST);
    public static final String NEW = "new";
    public static final String PENDING = "pending";
    public static final String FAILED = "failed";
    public static final String TESTED = "tested";
    public static final String ACTIVE = "active";
    public static final String INACTIVE = "inactive";
    public static final String BROKEN = "broken";


	public void setArchiveForUpload(byte[] bytes)
	{
	    cachedArchive = bytes;
	}

    /**
     * @return Returns the comment.
     */
    public String getComment() {
        return comment;
    }
    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    /**
     * @return Returns the testRunPK.
     */
    public Integer getTestRunPK()
    {
        return testRunPK;
    }
    /**
     * @param testRunPK The testRunPK to set.
     */
    public void setTestRunPK(Integer testRunPK)
    {
        this.testRunPK = testRunPK;
    }
    /**
     * @return Returns the datePosted.
     */
    public Timestamp getDatePosted() {
        return datePosted;
    }
    /**
     * @param datePosted The datePosted to set.
     */
    public void setDatePosted(Timestamp datePosted) {
        this.datePosted = datePosted;
    }
    /**
     * @return Returns the testSetupPK.
     */
    public Integer getTestSetupPK() {
        return testSetupPK;
    }
    /**
     * @param testSetupPK The testSetupPK to set.
     */
    public void setTestSetupPK(Integer testSetupPK) {
        this.testSetupPK = testSetupPK;
    }
    /**
     * @return Returns the projectPK.
     */
    public int getProjectPK() {
        return projectPK;
    }
    /**
     * @param projectPK The projectPK to set.
     */
    public void setProjectPK(int projectPK) {
        this.projectPK = projectPK;
    }
    
    public boolean hasJarFileStatus(String ... anyOf) {
        for(String s : anyOf)
            if (jarfileStatus.equals(s))
                return true;
        return false;
    }
    /**
     * @return Returns the jarfileStatus.
     */
    public String getJarfileStatus()
    {
        return jarfileStatus;
    }
    /**
     * @param jarfileStatus The jarfileStatus to set.
     */
    public void setJarfileStatus(String jarfileStatus)
    {
        this.jarfileStatus = jarfileStatus;
    }
    /**
     * @return Returns the version.
     */
    public int getVersion() {
        return version;
    }
    /**
     * @param version The version to set.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    public int getValuePublicTests() {
        return valuePublicTests;
    }
    public void setValuePublicTests(int numPublicTests) {
        this.valuePublicTests = numPublicTests;
    }
    public int getValueReleaseTests() {
        return valueReleaseTests;
    }
    public void setValueReleaseTests(int numReleaseTests) {
        this.valueReleaseTests = numReleaseTests;
    }
    public int getValueSecretTests() {
        return valueSecretTests;
    }
    public void setValueSecretTests(int numSecretTests) {
        this.valueSecretTests = numSecretTests;
    }
    public int getValueTotalTests() {
        return numTotalTests;
    }
    public void setValueTotalTests(int numTotalTests) {
        this.numTotalTests = numTotalTests;
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

    
    public String getDescription() {
        if (comment == null || comment.isEmpty())
            return "#" + version;
        return "#" + version +": " + comment;
    }
    /**
     * Fetches a TestSetup row from the database using the given ResultSet
     * starting at the given index.
     * <p>
     * <b>NOTE:</b> This retrieves only the archivePK, not the actual bytes, which
     * must be loaded separately using the {@link dowloadArchive(Connection)} method.
     *
     * @param rs the ResultSet
     * @param startingFrom the index
     * @return the index of the next column in the ResultSet
     * @throws SQLException
     */
    public int fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
        setTestSetupPK(rs.getInt(startingFrom++));
        setProjectPK(rs.getInt(startingFrom++));
        setJarfileStatus(rs.getString(startingFrom++));
        setVersion(rs.getInt(startingFrom++));
        setDatePosted(rs.getTimestamp(startingFrom++));
        setComment(rs.getString(startingFrom++));
        setTestRunPK(SqlUtilities.getInteger(rs, startingFrom++));
        setValueTotalTests(rs.getInt(startingFrom++));
        if (rs.getInt(startingFrom++) != 1) {
            // FIXME:
        }
        setValuePublicTests(rs.getInt(startingFrom++));
        setValueReleaseTests(rs.getInt(startingFrom++));
        setValueSecretTests(rs.getInt(startingFrom++));
        setArchivePK(SqlUtilities.getInteger(rs, startingFrom++));
        return startingFrom;
    }
    /**
     * @param testSetupPK2
     * @param conn
     * @return
     */
    public static TestSetup lookupByTestSetupPK(Integer testSetupPK, Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM  " + TABLE_NAME +
            " WHERE test_setup_pk = ? ";

        PreparedStatement stmt = conn.prepareStatement(query);

        SqlUtilities.setInteger(stmt, 1, testSetupPK);
        return getFromPreparedStatement(stmt);
    }

    /**
     * Returns a collection of project jarfiles for a given projectPK.
     * @param projectPK
     * @param conn
     * @return
     */
    public static Collection<TestSetup> lookupAllByProjectPK(Integer projectPK, Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM  " + TABLE_NAME +
            " WHERE project_pk = ? ";

        PreparedStatement stmt = conn.prepareStatement(query);

        SqlUtilities.setInteger(stmt, 1, projectPK);
        ResultSet rs = stmt.executeQuery();

        Collection<TestSetup> allTestSetups = new LinkedList<TestSetup>();
        while (rs.next()) {
            TestSetup jarFile = new TestSetup();
            jarFile.fetchValues(rs, 1);
            allTestSetups.add(jarFile);
            //Debug.print("Got project with PK " +project.getProjectPK());
        }
        return allTestSetups;

    }

    private static TestSetup getFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
            ResultSet rs = stmt.executeQuery();
            if (rs.first())
            {
                TestSetup testSetup = new TestSetup();
                testSetup.fetchValues(rs, 1);
                return testSetup;
            }
            return null;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
    }

    /**
     * @param conn
     */
    public void insert(Connection conn)
    throws SQLException
    {
        String insert = Queries.makeInsertStatement(
                ATTRIBUTE_NAME_LIST.length,
                ATTRIBUTES,
                TABLE_NAME);
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

            if (cachedArchive == null)
                throw new IllegalStateException("there is no archive for upload, you should call setArchiveForUpload first");
            setArchivePK(Archive.uploadBytesToArchive("test_setup_archives", cachedArchive, conn));

            int index=1;
            putValues(stmt, index);

            stmt.executeUpdate();

            setTestSetupPK(Queries.getGeneratedPrimaryKey(stmt));

        } finally {
            Queries.closeStatement(stmt);
        }
    }

	public byte[] downloadArchive(Connection conn)
    throws SQLException
    {
	    return Archive.downloadBytesFromArchive("test_setup_archives", getArchivePK(), conn);
    }


    public void update(Connection conn)
    throws SQLException
    {
        String update = Queries.makeUpdateStatementWithWhereClause(
                ATTRIBUTE_NAME_LIST,
                TABLE_NAME,
                " WHERE test_setup_pk = ? ");

        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(update);
            int index=1;
            index=putValues(stmt, index);
            SqlUtilities.setInteger(stmt, index, getTestSetupPK());

            stmt.executeUpdate();

        } finally {
            Queries.closeStatement(stmt);
        }
    }

    public static void resetAllFailedTestSetups(Integer projectPK, Connection conn)
    throws SQLException
    {
        String update =
            " UPDATE test_setups " +
            " SET " +
            " jarfile_status = ? " +

            " WHERE (jarfile_status = ? || jarfile_status = ? || jarfile_status = ?)" +
            " AND project_pk = ? ";
        PreparedStatement stmt=null;
        try {
            // update statement
            stmt = conn.prepareStatement(update);
            stmt.setString(1, NEW );
            // where clause
            stmt.setString(2, PENDING );
            stmt.setString(3, TESTED );
            stmt.setString(4, FAILED);
            SqlUtilities.setInteger(stmt, 5, projectPK);

            stmt.executeUpdate();
        } finally {
                if (stmt != null) stmt.close();
        }
    }

    private int putValues(PreparedStatement stmt, int index)
    throws SQLException
    {
        stmt.setInt(index++, getProjectPK());
        stmt.setString(index++, getJarfileStatus());
        stmt.setInt(index++, getVersion());
        stmt.setTimestamp(index++, getDatePosted());
        stmt.setString(index++, getComment());
        SqlUtilities.setInteger(stmt, index++, getTestRunPK());
        stmt.setInt(index++, getValueTotalTests());
        stmt.setInt(index++, 1);
        stmt.setInt(index++, getValuePublicTests());
        stmt.setInt(index++, getValueReleaseTests());
        stmt.setInt(index++, getValueSecretTests());
        SqlUtilities.setInteger(stmt, index++, getArchivePK());
        return index;
    }

    /**
     * Upload a new testSetup (test-setup) into the database given a byte array
     * of the zip archive of the test-setup file, the project the test-setup is associated with,
     * and a comment describing the uploaded archive.
     * @param byteArray Byte array containing of the zip archive of the test-setup file.
     * @param project The project the test-setup is to be associated with.
     * @param comment A comment (possibly empty or null) describing the reason for uploading
     *      a new archive.
     * @param conn The connection to the database.
     * @return The result TestSetup object.
     * @throws SQLException If something doesn't work.
     */
    public static TestSetup submit(
        byte[] byteArray,
        Project project,
        String comment,
        Connection conn) throws SQLException
    {
        // create new testSetup record
        TestSetup testSetup = new TestSetup();
        testSetup.setComment(comment);
        testSetup.setDatePosted(new Timestamp(System.currentTimeMillis()));
        testSetup.setProjectPK(project.getProjectPK());
        testSetup.setArchiveForUpload(byteArray);

        // insert the new jarfile in its default state
        testSetup.insert(conn);
        return testSetup;
    }
}
