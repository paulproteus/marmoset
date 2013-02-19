package edu.umd.cs.submitServer.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.submitServer.StudentForUpload;
import edu.umd.cs.submitServer.SubmitServerDatabaseProperties;
import edu.umd.cs.submitServer.dao.RegistrationDao;

public class MySqlAutoRegistrationDaoImpl implements RegistrationDao {
    private final @Nonnull
    Student student;
    private final @Nonnull
    SubmitServerDatabaseProperties props;

    public MySqlAutoRegistrationDaoImpl(Student student,
            SubmitServerDatabaseProperties props) {
        this.student = Preconditions.checkNotNull(student);
        this.props = Preconditions.checkNotNull(props);
    }

    @Override
    public Student getStudent() {
        return student;
    }

    @Override
    public boolean requestRegistration(int coursePK, @Nullable String section) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = props.getConnection();
            Course course = Course.getByCoursePK(coursePK, conn);
            String sections[] = course.getSections();
            if (sections == null && section != null)
                section = null;
            if (sections != null && !Arrays.asList(sections).contains(section))
                throw new IllegalArgumentException("Invalid section: "
                        + section);
            StudentRegistration sr = StudentRegistration
                    .lookupByStudentPKAndCoursePK(student.getStudentPK(),
                            course.getCoursePK(), conn);
            if (sr != null)
                return false;

            StudentForUpload.registerStudent(course, student, section,
                    student.getLoginName(), null, conn);

            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            Queries.closeStatement(stmt);
            Queries.close(conn);
        }
    }

    @Override
    public boolean requestRegistration(int coursePK) {
        return requestRegistration(coursePK, null);
    }

    @Override
    public List<Student> getPendingRegistrations(int coursePK) {
        throw new UnsupportedOperationException();
    }

    public java.util.List<Course> getOpenCourses() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Course> getPendingRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean acceptRegistration(int coursePK, @Student.PK int studentPK) {
        return acceptRegistration(coursePK, studentPK, null);
    }

    @Override
    public boolean acceptRegistration(int coursePK, @Student.PK int studentPK,
            String section) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean denyRegistration(int coursePK, int studentPK) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, String> getRequestSection(int coursePK) {
        throw new UnsupportedOperationException();
    }
}
