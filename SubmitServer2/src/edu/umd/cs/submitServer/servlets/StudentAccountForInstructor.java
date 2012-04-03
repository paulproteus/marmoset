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

package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;

public class StudentAccountForInstructor extends SubmitServerServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Connection conn = null;
        @CheckForNull
        Course course = (Course) request.getAttribute(COURSE);
        Student user = (Student) request.getAttribute(USER);
        Student student = (Student) request.getAttribute(STUDENT);

        if (!student.getCanImportCourses())
            throw new IllegalArgumentException();
        StudentRegistration instructor = (StudentRegistration) request
                .getAttribute(STUDENT_REGISTRATION);
        if (course != null) {
            
            if (instructor.getStudentPK() != user.getStudentPK())
                throw new IllegalArgumentException();
            if (!instructor.isInstructorModifiy())
                throw new IllegalArgumentException();
        }
        try {
            conn = getConnection();

            Student student2 = createOrFindPseudoStudent(conn, student);
            String redirectUrl = request.getContextPath() + "/view/index.jsp";
            if (course != null) {
                createOrFindPseudoStudentRegistrationFromPseudoStudent(conn, course, instructor,
                        student2);
                redirectUrl = request.getContextPath()
                        + "/view/course.jsp?coursePK=" + course.getCoursePK();
            };

            HttpSession session = request.getSession(false);

            session.invalidate();
            session = request.getSession(true);

            PerformLogin.setUserSession(session, student2, conn);

            response.sendRedirect(redirectUrl);
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);

        }

    }

    public static StudentRegistration createOrFindPseudoStudentRegistration(Connection conn,
            Course course, StudentRegistration instructor, Student student)
            throws SQLException {
        Student student2 = createOrFindPseudoStudent(conn, student);
        return createOrFindPseudoStudentRegistrationFromPseudoStudent(conn, course, instructor, student2);
        
    }
 
    private static StudentRegistration createOrFindPseudoStudentRegistrationFromPseudoStudent(Connection conn,
            Course course, StudentRegistration instructor, Student student2)
            throws SQLException {
        StudentRegistration registration = StudentRegistration
                .lookupByStudentPKAndCoursePK(student2.getStudentPK(),
                        course.getCoursePK(), conn);
        if (registration == null) {
            registration = new StudentRegistration();
            registration.setStudentPK(student2.getStudentPK());
            registration.setCoursePK(course.getCoursePK());
            registration.setClassAccount(instructor.getClassAccount()
                    + "-student");

            registration
                    .setInstructorCapability(StudentRegistration.PSEUDO_STUDENT_CAPABILITY);
            registration.setFirstname(student2.getFirstname());
            registration.setLastname(student2.getLastname());
            registration.setCourse(course.getCourseName());
            registration.setSection(course.getSection());
            registration.setCourseID(-1);
            registration.insert(conn);
        }
        return registration;
    }

    private static  Student createOrFindPseudoStudent(Connection conn, Student student)
            throws SQLException {
        Student student2 = new Student();
        student2.setLastname(student.getLastname());
        student2.setFirstname(student.getFirstname());
        student2.setCampusUID(student.getCampusUID());
        student2.setLoginName(student.getLoginName() + "-student");
        student2.setAccountType(Student.PSEUDO_ACCOUNT);
        student2 = student2.insertOrUpdateCheckingLoginNameAndCampusUID(conn);
        return student2;
    }


}
