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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.umd.cs.marmoset.utilities.SqlUtilities;


/**
 * @author jspacco
 * TODO Factor out this class; it's useless!  The static lookup methods in here
 * should just return a List.
 */
public class CourseCollection
{
	private List<Course> courseList;

	public CourseCollection()
	{
		courseList = new ArrayList<Course>();
	}

	public Iterator<Course> iterator()
	{
	    return courseList.iterator();
	}

	public boolean isEmpty()
	{
	    return courseList.isEmpty();
	}

	public void add(Course course)
	{
		courseList.add(course);
	}

	public List<Course> getCollection()
	{
		return courseList;
	}

    /**
     * Looks up all the courses a student is taking.
     *
     * @param studentPK the student PK
     * @param conn the connection to the database
     * @return The collection of courses the student is taking.
     * This may be an empty collection.
     * @throws SQLException
     */
    public static CourseCollection lookupCoursesByStudentPK(
    		@Student.PK Integer studentPK,
    		Connection conn) throws SQLException
    {
    	String query = "SELECT " +Course.ATTRIBUTES+ " "+
    	"FROM courses, student_registration "+
    	"WHERE student_registration.student_pk = ? "+
    	"AND student_registration.course_pk = courses.course_pk ";

    	PreparedStatement stmt = conn.prepareStatement(query);
    	SqlUtilities.setInteger(stmt, 1, studentPK);

    	return getAllFromPreparedStatement(stmt);
    }

    /**
     * Looks up all the courses currently in the database.
     * @param conn The connection to the database.
     * @return
     * @throws SQLException
     */
    public static CourseCollection lookupAll(Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +Course.ATTRIBUTES+
            " FROM courses " +
            " ORDER BY semester DESC, coursename ASC, section ASC";

        PreparedStatement stmt = conn.prepareStatement(query);
        return getAllFromPreparedStatement(stmt);
    }

    private static CourseCollection getAllFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
            ResultSet rs = stmt.executeQuery();

            CourseCollection courses = new CourseCollection();
            while (rs.next())
            {
                Course course = new Course();
                course.fetchValues(rs, 1);
                courses.add(course);
            }
            return courses;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignore) {
                    // Ignore
                }
            }
        }
    }

    public static CourseCollection lookupAllWithModifyCapability(
    		@Student.PK  Integer studentPK,
            Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +Course.ATTRIBUTES+ " " +
            " FROM courses, student_registration " +
            " WHERE student_registration.student_pk = ? " +
            " AND student_registration.course_pk = courses.course_pk " +
            " AND student_registration.instructor_capability = ? ";

        PreparedStatement stmt = conn.prepareStatement(query);
        SqlUtilities.setInteger(stmt, 1, studentPK);
        stmt.setString(2, StudentRegistration.MODIFY_CAPABILITY);

        return getAllFromPreparedStatement(stmt);
    }


    public static CourseCollection lookupAllWithReadOnlyCapability(
    		@Student.PK Integer studentPK,
            Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +Course.ATTRIBUTES+ " " +
            " FROM courses, student_registration " +
            " WHERE student_registration.student_pk = ? " +
            " AND student_registration.course_pk = courses.course_pk " +
            " AND ( student_registration.instructor_capability = ? OR " +
            "	    student_registration.instructor_capability = ?) ";

        PreparedStatement stmt = conn.prepareStatement(query);
        SqlUtilities.setInteger(stmt, 1, studentPK);
        stmt.setString(2, StudentRegistration.READ_ONLY_CAPABILITY);
        stmt.setString(3, StudentRegistration.MODIFY_CAPABILITY);

        return getAllFromPreparedStatement(stmt);
    }
}
