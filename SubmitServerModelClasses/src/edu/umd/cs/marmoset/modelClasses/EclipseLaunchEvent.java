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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.umd.cs.marmoset.utilities.SqlUtilities;

/**
 * @author jspacco
 *
 */
public class EclipseLaunchEvent
{

    public enum Event {DEBUG_JAVA_APPLET, DEBUG_JAVA_APPLICATION, DEBUG_JUNIT,
        RUN_JAVA_APPLET, RUN_JAVA_APPLICATION, RUN_JUNIT,
        ENABLED, OTHER };

    public static class Summary {
        public Summary(Timestamp start, int distinctVersions, EnumMap<Event, Integer> counts) {
            this.start = start;
            this.distinctVersions = distinctVersions;
            this.counts = counts;
            int total = 0;
            for(int t : counts.values())
                total += t;
            this.total = total;
        }
        public String toString() {
            return String.valueOf(start) + ":" + counts;
        }
        public final Timestamp start;
        public final int distinctVersions;
        public final EnumMap<Event, Integer> counts;
        public final int total;
        public Timestamp getStart() {
            return start;
        }
        public int getDistinctVersions() {
            return distinctVersions;
        }
        public EnumMap<Event, Integer> getCounts() {
            return counts;
        }
        public int getTotal() {
            return total;
        }
        public int getRunJUnit() {
            Integer v = counts.get(Event.RUN_JUNIT);
            if (v == null)
                return 0;
            return v;

        }
        public int getDebugJUnit() {
            Integer v = counts.get(Event.DEBUG_JUNIT);
            if (v == null)
                return 0;
            return v;
        }
    }

	public static final String TABLE_NAME = "eclipse_launch_events";
	public static final String[] ATTRIBUTE_NAME_LIST = {
        "eclipse_launch_event_pk",
        "student_registration_pk",
        "project_number",
        "project_pk",
        "checksum",
        "event",
        "timestamp",
        "skew"
	};
	/**
	 * Fully-qualified attributes for courses table.
	 */
	public static final String ATTRIBUTES = Queries.getAttributeList(TABLE_NAME,
		ATTRIBUTE_NAME_LIST);

	private Integer eclipseLaunchEventPK;  // non-NULL, autoincrement
	private @StudentRegistration.PK int studentRegistrationPK; // non-NULL
	private @Project.PK int projectPK;
	private String projectNumber;
	private String md5sum;
	private String event;
	private Timestamp timestamp;
	private int skew;

	public EclipseLaunchEvent() {}

    public static PreparedStatement makeInsertStatement(Connection conn) throws SQLException {
        String query = Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME, true);
        PreparedStatement stmt = conn.prepareStatement(query);
        return stmt;
    }

    public void fillInInsertStatement(PreparedStatement stmt) throws SQLException {
        putValues(stmt, 1);
    }

	public void insert(Connection conn)
	throws SQLException
	{
		// TODO Check for duplicates!
		String query = Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME, true);
		PreparedStatement stmt=null;
		try {
			stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			putValues(stmt, 1);
			stmt.execute();
			setEclipseLaunchEventPK(Queries.getGeneratedPrimaryKey(stmt));
		} finally {
			Queries.closeStatement(stmt);
		}
	}


	public int fetchValues(ResultSet resultSet, int startingFrom) throws SQLException
	{
		setEclipseLaunchEventPK(SqlUtilities.getInteger(resultSet, startingFrom++));
		setStudentRegistrationPK(StudentRegistration.asPK(resultSet.getInt(startingFrom++)));
		setProjectNumber(resultSet.getString(startingFrom++));
		setProjectPK(Project.asPK(resultSet.getInt(startingFrom++)));
        setMd5sum(resultSet.getString(startingFrom++));
		setEvent(resultSet.getString(startingFrom++));
		setTimestamp(resultSet.getTimestamp(startingFrom++));
		setSkew(resultSet.getInt(startingFrom++));
		return startingFrom;
	}

	private int putValues(PreparedStatement stmt, int index)
	throws SQLException
	{
	    stmt.setInt(index++, getStudentRegistrationPK());
	    stmt.setString(index++, getProjectNumber());
	    stmt.setInt(index++, getProjectPK());
	    stmt.setString(index++, getMd5sum());
	    stmt.setString(index++, getEvent());
	    stmt.setTimestamp(index++, getTimestamp());
	    stmt.setInt(index++, getSkew());
	    return index;
	}

	/**
	 * @return Returns the date.
	 */
	public String getMd5sum() {
		return md5sum;
	}
	/**
	 * @param date The date to set.
	 */
	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}
	/**
	 * @return Returns the eclipseLaunchEventPK.
	 */
	public Integer getEclipseLaunchEventPK() {
		return eclipseLaunchEventPK;
	}
	/**
	 * @param eclipseLaunchEventPK The eclipseLaunchEventPK to set.
	 */
	public void setEclipseLaunchEventPK(Integer eclipseLaunchEventPK) {
		this.eclipseLaunchEventPK = eclipseLaunchEventPK;
	}
	/**
	 * @return Returns the event.
	 */
	public String getEvent() {
		return event;
	}

	public Event getEventEnum() {
	    try {
	        return Event.valueOf(event.replace('-','_').replace(' ','_').toUpperCase());
	    } catch  (RuntimeException e) {
	        return Event.OTHER;
	    }
	}
	/*
	 * @param event The event to set.
	 */
	public void setEvent(String event) {
		if (event.length() > 20)
			event = event.substring(0,20);
		this.event = event;
	}
	/**
	 * @return Returns the projectNumber.
	 */
	public String getProjectNumber() {
		return projectNumber;
	}
	/**
	 * @param projectNumber The projectNumber to set.
	 */
	public void setProjectNumber(String projectNumber) {
		this.projectNumber = projectNumber;
	}
	public @Project.PK int getProjectPK() {
        return projectPK;
    }

    public void setProjectPK(@Project.PK int projectPK) {
        this.projectPK = projectPK;
    }

    /**
	 * @return Returns the studentRegistrationPK.
	 */
	public @StudentRegistration.PK  int getStudentRegistrationPK() {
		return studentRegistrationPK;
	}
	/**
	 * @param studentRegistrationPK The studentRegistrationPK to set.
	 */
	public void setStudentRegistrationPK(@StudentRegistration.PK int studentRegistrationPK) {
		this.studentRegistrationPK = studentRegistrationPK;
	}
	/**
	 * @return Returns the timestamp.
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return Returns the skew.
	 */
	public int getSkew() {
		return skew;
	}
	/**
	 * @param skew The skew to set.
	 */
	public void setSkew(int skew) {
		this.skew = skew;
	}


	private static List<EclipseLaunchEvent> getAllFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        ResultSet rs = stmt.executeQuery();

        List<EclipseLaunchEvent> collection = new LinkedList<EclipseLaunchEvent>();
        while (rs.next())
        {
            EclipseLaunchEvent event = new EclipseLaunchEvent();
            event.fetchValues(rs, 1);
            collection.add(event);
        }
        return collection;
    }


	public static List<EclipseLaunchEvent> lookupEclipseLaunchEventsByProjectPKAndStudentRegistration(
	        Project project,
	        StudentRegistration studentRegistration,
	        Connection conn)
	    throws SQLException
	    {

        String query =
            " SELECT " +ATTRIBUTES+
            " FROM " + TABLE_NAME +
            " WHERE project_pk = ? " +
            " AND  student_registration_pk = ? " +
            " ORDER BY timestamp ASC";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            Queries.setStatement(stmt, project.getProjectPK(), studentRegistration.getStudentRegistrationPK());
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }

	public static int countEclipseLaunchEventsByProjectPKAndStudentRegistration(
            Project project,
            StudentRegistration studentRegistration,
            Connection conn)
        throws SQLException
        {

        String query =
            " SELECT COUNT(*) " +
            " FROM " + TABLE_NAME +
            " WHERE project_pk = ? " +
            " AND  student_registration_pk = ? ";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            Queries.setStatement(stmt, project.getProjectPK(), studentRegistration.getStudentRegistrationPK());
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1);
            return 0;
        } finally {
            Queries.closeStatement(stmt);
        }
    }

	public static List<Summary> summarize(List<EclipseLaunchEvent> events, int window, TimeUnit units) {
	    List<Summary> result = new ArrayList<Summary>();
	    if (events.isEmpty())
	        return result;
	    Timestamp start = null;
	    Timestamp prev = null;
	    long windowMillis = units.toMillis(window);
	    HashSet<String> versions = new HashSet<String>();
	    int knownVersions = 0;
	    EnumMap<Event, Integer> counts = new EnumMap<Event, Integer>(Event.class);
	    for(EclipseLaunchEvent e : events) {
	        if (start == null || e.getTimestamp().getTime() - start.getTime() > windowMillis) {
	            // start new window
	            if (start != null)
	                result.add(new Summary(start, versions.size() - knownVersions, counts));
	            counts =  new EnumMap<Event, Integer>(Event.class);
	            start = e.getTimestamp();
	            knownVersions = versions.size();

	            counts.clear();
	        } else if (prev != null && prev.equals(e.getTimestamp()))
	            continue;

	        Event ee = e.getEventEnum();
	        Integer v = counts.get(ee);
	        versions.add(e.getMd5sum());
	        if (v == null)
	            counts.put(ee, 1);
	        else
	            counts.put(ee,1+v);
	        prev = e.getTimestamp();


	    }
	    result.add(new Summary(start, versions.size() - knownVersions, counts));
        return result;
	}






}
