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

package edu.umd.cs.dbunit;

import java.io.FileInputStream;
import java.sql.Connection;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.utilities.DatabaseUtilities;

/**
 * @author jspacco
 *
 */
public class CourseTest extends DatabaseTestCase
{
    public static final String JUNIT_DIR = "junit";
    private IDatabaseConnection connection=null;

    /**
     * Gets the real database connection
     * @return a real connection to the database
     * @throws Exception
     */
    protected Connection getRealDatabaseConnection()
    throws Exception {
        return getConnection().getConnection();
    }

    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getConnection()
     */
    @Override
	protected IDatabaseConnection getConnection() throws Exception
    {
        Class.forName("org.gjt.mm.mysql.Driver");

        // Got rid of hardcoded username and password, but the
        // my.test.cnf file must exist for this to work properly.
	// Also, the database host must be set per invocation with a system property.
        DatabaseUtilities.getDbProps(System.getenv("HOME") + "/.my.test.cnf");
        DatabaseUtilities.getDbProps(System.getenv("HOME") + "/.my.test.cnf");
	String databaseUrl = System.getProperty("database.jdbc.url");
	Connection jdbcConnection;
	if(databaseUrl == null) {
	    throw new Exception("Cannot connect to database host: system property " +
				"'database.jdbc.url' is not defined");
	} else {
	    jdbcConnection =
		DatabaseUtilities.getConnection(databaseUrl);
	}

        return new DatabaseConnection(jdbcConnection);
    }
    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    @Override
	protected IDataSet getDataSet() throws Exception
    {
        return new FlatXmlDataSet(new FileInputStream(JUNIT_DIR + "/courses.xml"));
    }
    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getSetUpOperation()
     */
    @Override
	protected DatabaseOperation getSetUpOperation() throws Exception
    {
        return DatabaseOperation.CLEAN_INSERT;
    }
    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#getTearDownOperation()
     */
    @Override
	protected DatabaseOperation getTearDownOperation() throws Exception
    {
        return DatabaseOperation.NONE;
    }



    /* (non-Javadoc)
     * @see org.dbunit.DatabaseTestCase#closeConnection(org.dbunit.database.IDatabaseConnection)
     */
    @Override
	protected void closeConnection(IDatabaseConnection arg0) throws Exception
    {
        super.closeConnection(arg0);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        if (connection == null) {
            connection = getConnection();
        }
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        closeConnection(connection);
    }

    public void testLookupAllByCoursePK()
    throws Exception
    {
        Connection conn = connection.getConnection();
        @StudentRegistration.PK int studentRegistrationPK = 1;
        Course course = Course.lookupByCoursePK(studentRegistrationPK, conn);
        assertTrue(course.getCourseName().equals("CMSC132"));
        assertTrue(course.getSemester().equals("Spring 2005"));
    }

    public void testInsert()
    throws Exception
    {
        Connection conn = connection.getConnection();
        Course course = new Course();
        course.setCourseName("courseName");
        course.setDescription("description");
        course.setSection("1");
        course.setSemester("semester");
        course.setUrl("url");

        course.insert(conn);

        Integer coursePK = course.getCoursePK();
        assertEquals(coursePK.intValue(), 1);

        Course insertedCourse = Course.lookupByCoursePK(coursePK, conn);

        assertEquals(course, insertedCourse);
    }
}
