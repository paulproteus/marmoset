package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Ordering;

import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment;
import edu.umd.cs.marmoset.modelClasses.CodeReviewSummary;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.submitServer.UserSession;

public class CodeReviewAssignmentFilter extends SubmitServerFilter {

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
      ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;

    final CodeReviewAssignment codeReviewAssignment = (CodeReviewAssignment) request
        .getAttribute(CODE_REVIEW_ASSIGNMENT);

    Connection conn = null;
    try {

      conn = getConnection();

      int codeReviewAssignmentPK = codeReviewAssignment.getCodeReviewAssignmentPK();
      Collection<Submission> submissionsUnderReview = Submission
          .getSubmissionsUnderReview(codeReviewAssignmentPK, conn);

      if (!submissionsUnderReview.isEmpty()) {
        Collection<CodeReviewer> codeReviewersForAssignment = CodeReviewer.lookupByCodeReviewAssignmentPK(
            codeReviewAssignmentPK, conn);

        Map<Submission, CodeReviewSummary.Info> info = new HashMap<Submission, CodeReviewSummary.Info>();
        for (Submission s : submissionsUnderReview) {
          info.put(s, new CodeReviewSummary.Info(conn, s, codeReviewAssignment));
        }
        Map<CodeReviewer, CodeReviewSummary> summary = new HashMap<CodeReviewer, CodeReviewSummary>();
        for (CodeReviewer r : codeReviewersForAssignment) {
          summary.put(r, new CodeReviewSummary(info.get(r.getSubmission()), r));
        }

        Collection<CodeReviewer> studentCodeReviewersForAssignment = new ArrayList<CodeReviewer>();
        HashMultimap<Integer, CodeReviewer> reviewersForSubmission = HashMultimap.create();
        HashMultimap<Student, CodeReviewer> reviewsByStudent = HashMultimap.create();
        Map<Integer, CodeReviewer> authorForSubmission = new HashMap<Integer, CodeReviewer>();
        for (CodeReviewer r : codeReviewersForAssignment) {
          if (r.isAuthor()) {
            authorForSubmission.put(r.getSubmissionPK(), r);
          } else
            reviewersForSubmission.put(r.getSubmissionPK(), r);
          if (!r.isInstructor() && !r.isAuthor())
            studentCodeReviewersForAssignment.add(r);
          if (!r.isAuthor())
            reviewsByStudent.put(r.getStudent(), r);
        }
        boolean canRevert = true;
        Map<Submission, CodeReviewSummary.Status> codeReviewStatus = new HashMap<Submission, CodeReviewSummary.Status>();

        for (Submission s : submissionsUnderReview) {
          boolean reviewerComments = false;
          Collection<CodeReviewer> reviewers = reviewersForSubmission.get(s.getSubmissionPK());
          CodeReviewer author = authorForSubmission.get(s.getSubmissionPK());
          if (reviewers != null && author != null)
            for (CodeReviewer r : reviewers)
              if (!r.isAutomated() && r.getNumComments() > 0) {
                reviewerComments = true;
                if (!isPrototypeReview(r, author))
                  canRevert = false;
              }
          boolean authorComments = author != null && author.getNumComments() > 0;

          CodeReviewSummary.Status status;
          if (!reviewerComments)
            status = authorComments ? CodeReviewSummary.Status.PUBLISHED : CodeReviewSummary.Status.NOT_STARTED;
          else
            status = authorComments ? CodeReviewSummary.Status.INTERACTIVE : CodeReviewSummary.Status.PUBLISHED;

          codeReviewStatus.put(s, status);

        }
        request.setAttribute("canRevertCodeReview", canRevert);
        request.setAttribute("codeReviewSummary", summary);
        request.setAttribute("codeReviewStatus", codeReviewStatus);
        request.setAttribute("overallCodeReviewStatus", Ordering.natural().max(codeReviewStatus.values()));
        request.setAttribute("reviewersForSubmission", reviewersForSubmission.asMap());
        request.setAttribute("reviewsByStudent", reviewsByStudent.asMap());
        request.setAttribute("authorForSubmission", authorForSubmission);
        request.setAttribute("codeReviewersForAssignment", codeReviewersForAssignment);
        request.setAttribute("studentCodeReviewersForAssignment", studentCodeReviewersForAssignment);
        request.setAttribute("submissionsUnderReview", submissionsUnderReview);
      }

    } catch (SQLException e) {
      throw new ServletException(e);
    } finally {
      releaseConnection(conn);
    }

    chain.doFilter(request, response);
  }

  public boolean isPrototypeReview(CodeReviewer reviewer, CodeReviewer author) {
    return reviewer.getStudent().isPseudoAccount() || author.getStudent().isPseudoAccount() || reviewer.isInstructor()
        && author.isInstructor();
  }
}
