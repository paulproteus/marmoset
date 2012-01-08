package edu.umd.cs.submitServer.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.SubmitServerDatabaseProperties;
import edu.umd.cs.submitServer.dao.RegistrationDao;

public class MySqlRegistrationDaoImpl implements RegistrationDao {
	private final @Nonnull Student student;
	private final @Nonnull SubmitServerDatabaseProperties props;
	
	private enum RequestStatus {
		PENDING,
		APPROVED,
		DENIED
		;
	}
	
	public MySqlRegistrationDaoImpl(Student student, SubmitServerDatabaseProperties props) {
		this.student = Preconditions.checkNotNull(student);
		this.props = Preconditions.checkNotNull(props);
	}
	
	@Override
  public Student getStudent() {
	  return student;
  }

	@Override
  public boolean requestRegistration(int coursePK) {
	  Connection conn = null;
	  PreparedStatement exists = null;
	  PreparedStatement insert = null;
	  try {
	    conn = props.getConnection();
	    exists = conn.prepareStatement("SELECT * from registration_requests " +
	    			"WHERE course_pk = ? AND student_pk = ?");
	    Queries.setStatement(exists, coursePK, student.getStudentPK());
	    ResultSet rs = exists.executeQuery();
	    if (rs.next()) {
	    	// Registration request already exists.
	    	return false;
	    }
	    
	    long timestamp = new Date().getTime();
			insert = conn.prepareStatement("INSERT INTO registration_requests "
			    + "(student_pk, course_pk, timestamp, status) VALUES (?, ? ,? ,?)");
			Queries.setStatement(insert, student.getStudentPK(), coursePK, timestamp, RequestStatus.PENDING);
			insert.executeUpdate();
	    return true;
    } catch (SQLException e) {
	    throw new RuntimeException(e);
    } finally {
    	Queries.closeStatement(exists);
    	Queries.closeStatement(insert);
    	Queries.close(conn);
    }
  }
	
	private boolean isInstructor(Connection conn, int coursePK) throws SQLException {
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("SELECT instructor_capability FROM student_registration "
			    + "WHERE course_pk = ? AND student_pk = ?");
			Queries.setStatement(st, coursePK, student.getStudentPK());
			ResultSet rs = st.executeQuery();
			if (!rs.next()) {
				// No registration for this student in this course at all.
				return false;
			}
			String capability = rs.getString(1);
			return !Strings.isNullOrEmpty(capability);
		} finally {
			Queries.closeStatement(st);
		}
	}

	@Override
  public List<Student> getPendingRegistrations(int coursePK) {
	  Connection conn = null;
	  PreparedStatement stmt = null;
	  try {
	  	conn = props.getConnection();
	  	Preconditions.checkArgument(isInstructor(conn, coursePK),
	  	                            "DAO student must be an instructor for course");
	  	// TODO(rwsims): Could be more efficient with a join here if we can build a Student from a ResultSet.
	  	stmt = conn.prepareStatement("SELECT student_pk FROM registration_requests WHERE course_pk = ? AND status = ?");
	  	Queries.setStatement(stmt, coursePK, RequestStatus.PENDING);
	  	ResultSet rs = stmt.executeQuery();
	  	List<Student> students = Lists.newArrayList();
	  	while (rs.next()) {
	  		@Student.PK int studentPK = Student.asPK(rs.getInt(1));
	  		students.add(Student.lookupByStudentPK(studentPK, conn));
	  	}
	  	return students;
    } catch (SQLException e) {
	    throw new RuntimeException(e);
    } finally {
    	Queries.closeStatement(stmt);
    	Queries.close(conn);
    }
  }
	
	public java.util.List<Course> getOpenCourses() {
	  Connection conn = null;
	  PreparedStatement stmt = null;
	  try {
	  	conn = props.getConnection();
			// The following will return a row for every course where the dao's student is neither already
			// registered nor has a pending/denied request.
			stmt = conn.prepareStatement("SELECT * FROM courses "
			    + "NATURAL LEFT JOIN (SELECT * FROM registration_requests WHERE student_pk = ?) AS requests "
					+ "LEFT JOIN (SELECT * FROM student_registration WHERE student_pk = ?) "
			    + "AS registrations ON registrations.course_pk = courses.course_pk "
			    + "WHERE requests.student_pk IS NULL AND registrations.student_pk IS NULL");
	  	Queries.setStatement(stmt, student.getStudentPK(), student.getStudentPK());
	  	ResultSet rs = stmt.executeQuery();
	  	List<Course> courses = Lists.newArrayList();
	  	while (rs.next()) {
	  		int coursePK = rs.getInt("course_pk");
	  		courses.add(Course.getByCoursePK(coursePK, conn));
	  	}
	  	return courses;
    } catch (SQLException e) {
	    throw new RuntimeException(e);
    } finally {
    	Queries.closeStatement(stmt);
    	Queries.close(conn);
    }
  }

	@Override
  public List<Course> getPendingRequests() {
	  Connection conn = null;
	  PreparedStatement stmt = null;
	  try {
	  	conn = props.getConnection();
	  	stmt = conn.prepareStatement("SELECT course_pk FROM registration_requests WHERE student_pk = ? AND status = ?");
	  	Queries.setStatement(stmt, student.getStudentPK(), RequestStatus.PENDING);
	  	ResultSet rs = stmt.executeQuery();
	  	List<Course> courses = Lists.newArrayList();
	  	while (rs.next()) {
	  		courses.add(Course.getByCoursePK(rs.getInt(1), conn));
	  	}
	  	return courses;
    } catch (SQLException e) {
	    throw new RuntimeException(e);
    } finally {
    	Queries.closeStatement(stmt);
    	Queries.close(conn);
    }
  }

	@Override
  public boolean acceptRegistration(int coursePK, @Student.PK int studentPK) {
	  Connection conn = null;
	  PreparedStatement stmt = null;
	  try {
	  	conn = props.getConnection();
	  	stmt = conn.prepareStatement("UPDATE registration_requests SET status = ? "
	  	                             + "WHERE course_pk = ? AND student_pk = ? AND status = ?");
	  	Queries.setStatement(stmt, RequestStatus.APPROVED, coursePK, studentPK, RequestStatus.PENDING);
	  	int rows = stmt.executeUpdate();
	  	if (rows == 0) {
	  		return false;
	  	}
	  	
	  	Student student = Student.getByStudentPK(studentPK, conn);
	  	StudentRegistration reg = new StudentRegistration();
	  	reg.setStudentPK(studentPK);
	  	reg.setCoursePK(coursePK);
	  	reg.setClassAccount(student.getLoginName());
	  	reg.setFirstname(student.getFirstname());
	  	reg.setLastname(student.getLastname());
	  	reg.insert(conn);
	  	
	  	return true;
    } catch (SQLException e) {
	    throw new RuntimeException(e);
    } finally {
    	Queries.closeStatement(stmt);
    	Queries.close(conn);
    }
  }

	@Override
  public boolean denyRegistration(int coursePK, int studentPK) {
	  Connection conn = null;
	  PreparedStatement stmt = null;
	  try {
	  	conn = props.getConnection();
	  	stmt = conn.prepareStatement("UPDATE registration_requests SET status = ? "
	  	                             + "WHERE course_pk = ? AND student_pk = ? AND status = ?");
	  	Queries.setStatement(stmt, RequestStatus.DENIED, coursePK, studentPK, RequestStatus.PENDING);
	  	int rows = stmt.executeUpdate();
	  	return rows > 0;
    } catch (SQLException e) {
	    throw new RuntimeException(e);
    } finally {
    	Queries.closeStatement(stmt);
    	Queries.close(conn);
    }
  }
}
