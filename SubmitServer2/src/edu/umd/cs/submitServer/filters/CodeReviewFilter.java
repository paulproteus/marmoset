package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.review.MarmosetDaoService;
import edu.umd.cs.submitServer.UserSession;
import edu.umd.review.gwt.rpc.dto.ReviewerDto;

public class CodeReviewFilter extends SubmitServerFilter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession) session.getAttribute(USER_SESSION);

        CodeReviewer reviewer = (CodeReviewer) request.getAttribute(REVIEWER);
        Course course = (Course) request.getAttribute(COURSE);
        Student user = (Student) request.getAttribute(USER);
        Submission submission = (Submission) request.getAttribute(SUBMISSION);
        CodeReviewAssignment codeReviewAssignment = (CodeReviewAssignment) request.getAttribute(CODE_REVIEW_ASSIGNMENT);
        Connection conn = null;
        try {

            conn = getConnection();
            StudentRegistration commenter = StudentRegistration.lookupByStudentPKAndCoursePK(user.getStudentPK(),
                course.getCoursePK(), conn);
            if (reviewer == null) {
              if (submission == null) 
                throw new IllegalArgumentException("Have neither reviewer nor submission");
                if (commenter == null) {
                    if (!userSession.isSuperUser()) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                "Authentication Error: You are not registrated for this class");
                                return;
                    }
                    // superuser 
                    reviewer = CodeReviewer.lookupOrAddReviewer(conn, submission.getSubmissionPK(),
                            userSession.getStudentPK(), "",
                            false,
                            true);
                } else if (commenter.isInstructor() ||  submission.getStudentRegistrationPK() == commenter.getStudentRegistrationPK())
                    reviewer = CodeReviewer.lookupOrAddReviewer(conn, submission, commenter);
                else
                    reviewer = CodeReviewer.lookupBySubmissionAndStudentPK(submission.getSubmissionPK(),
                            userSession.getStudentPK(), conn);
                if (reviewer == null)
                    throw new ServletException("Did not find code reviewer for " + submission.getSubmissionPK() + " by "
                            + userSession.getStudentPK());

                request.setAttribute(REVIEWER, reviewer);
                codeReviewAssignment = reviewer.getCodeReviewAssignment();
                request.setAttribute(CODE_REVIEW_ASSIGNMENT, codeReviewAssignment);
                @CodeReviewer.PK Integer next = reviewer.getNext(conn);
                if (next != null)
                    request.setAttribute(NEXT_CODE_REVIEW, next);
            }

            if (codeReviewAssignment != null && !codeReviewAssignment.isVisibleToStudents()
                && commenter != null && commenter.isNormalStudent()
                && !reviewer.isAuthor())
              throw new IllegalArgumentException("Code review assignment not visible");
            
            if (user.getStudentPK() != reviewer.getStudentPK())
              throw new IllegalArgumentException("User and reviewer studentPK don't match");
            reviewer.markAsViewed(conn);
            MarmosetDaoService dao = new MarmosetDaoService(submitServerDatabaseProperties, reviewer);
            ReviewerDto reviewerDto = dao.getReviewer();
            session.setAttribute(reviewerDto.getKey(), dao);
            request.setAttribute("reviewDaoKey", reviewerDto.getKey());
            request.setAttribute("reviewerDto", reviewerDto);
            request.setAttribute("reviewDao", dao);
            request.setAttribute("codeReviewSummary", dao.getSummary());
  

        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            releaseConnection(conn);
        }

        chain.doFilter(request, response);
    }

}
