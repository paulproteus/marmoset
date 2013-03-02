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
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.HashMultimap;

import edu.umd.cs.marmoset.modelClasses.CodeReviewAssignment;
import edu.umd.cs.marmoset.modelClasses.CodeReviewSummary;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;

public class PrintCodeReviewAssignmentGrades extends SubmitServerServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Connection conn = null;
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();
    Project project = (Project) request.getAttribute("project");
    Set<StudentRegistration> registrationSet = (Set<StudentRegistration>) request
        .getAttribute("studentRegistrationSet");
    Map<CodeReviewer, CodeReviewSummary> summary = (Map<CodeReviewer, CodeReviewSummary>) request
        .getAttribute("codeReviewSummary");
    final boolean hasRubrics = (Boolean) request.getAttribute("hasRubrics");

    final CodeReviewAssignment codeReviewAssignment = (CodeReviewAssignment) request
        .getAttribute(CODE_REVIEW_ASSIGNMENT);
    Map<Student, Collection<CodeReviewer>> reviewsByStudent = (Map<Student, Collection<CodeReviewer>>) request
        .getAttribute("reviewsByStudent");

    Map<Integer, Collection<CodeReviewer>> reviewsByStudentPK = new HashMap<Integer, Collection<CodeReviewer>>();

    for (Map.Entry<Student, Collection<CodeReviewer>> e : reviewsByStudent.entrySet()) {
      reviewsByStudentPK.put(e.getKey().getStudentPK(), e.getValue());
    }

    Collection<CodeReviewer> authors = ((Map<Integer, CodeReviewer>) request.getAttribute("authorForSubmission"))
        .values();
    CSVWriter writer = new CSVWriter(out);

    HashMultimap<Integer, CodeReviewer> authorsByStudentPK = HashMultimap.create();
    for (CodeReviewer author : authors) {
      authorsByStudentPK.put(author.getStudentPK(), author);
    }
    for (StudentRegistration sr : registrationSet) {
      if (sr.isInstructor() == codeReviewAssignment.isByStudents())
        continue;
      if (sr.isPseudoStudent())
        continue;

      Collection<CodeReviewer> reviews = reviewsByStudentPK.get(sr.getStudentPK());
      Collection<CodeReviewer> authorByStudentPK = authorsByStudentPK.get(sr.getStudentPK());

      int rubricsEvaluated = 0;
      int commentsMade = 0;
      int responded = 0;
      int total = 0;
      if (reviews != null)
        for (CodeReviewer r : reviews) {
          CodeReviewSummary s = summary.get(r);
          total++;
          if (!s.isNeedsResponse())
            responded++;

          if (s.isAnyPublishedCommentsByViewer())
            commentsMade++;
          if (!s.getUnassignedRubrics().isEmpty())
            rubricsEvaluated++;

        }

      int authorResponded = 0;
      int authorTotal = 0;

      if (authorByStudentPK != null)
        for (CodeReviewer r : authorByStudentPK) {
          CodeReviewSummary s = summary.get(r);
          if (s.getThreadMap().isEmpty())
            continue;
          if (!s.isNeedsResponse())
            authorResponded++;
          authorTotal++;
        }

      writer.flush();
      out.printf("%d %d %d  %d %d%n", commentsMade, responded, total, authorResponded, authorTotal);
      if (total > 0) {
        if (commentsMade == total)
          write(writer, sr.getClassAccount(), "Comments", 1, "");
        else if (commentsMade > 0)
          write(writer, sr.getClassAccount(), "Comments", 0,
              String.format("Comments made on only %d/%d reviews", commentsMade, total));
        else
          write(writer, sr.getClassAccount(), "Comments", 0, "No comments made");
        if (hasRubrics) {
          if (rubricsEvaluated == total)

            write(writer, sr.getClassAccount(), "Rubrics", 1, "");
          else if (rubricsEvaluated > 0)
            write(writer, sr.getClassAccount(), "Rubrics", 0,
                String.format("Rubrics full evaluated on only %d/%d reviews", rubricsEvaluated, total));
          write(writer, sr.getClassAccount(), "Rubrics", 0, "No rubrics evaluated");
        }
        if (commentsMade == 0 && rubricsEvaluated == 0)
          write(writer, sr.getClassAccount(), "Reviewer Responses", 0, "No reviews performed");
        else if (responded == total)
          write(writer, sr.getClassAccount(), "Reviewer Responses", 1, "");
        else
          write(writer, sr.getClassAccount(), "Reviewer Responses", 0,
              String.format("Only ack'd or responded to author on %d/%d reviews", authorResponded, authorTotal));
      }

      if (authorTotal == 0)
        write(writer, sr.getClassAccount(), "Author Responses", 1, "No reviews to respond to");
      else if (authorResponded == authorTotal)
        write(writer, sr.getClassAccount(), "Author Responses", 1, "");
      else
        write(writer, sr.getClassAccount(), "Author Responses", 0,
            String.format("Only ack'd or responded to %d/%d reviews", authorResponded, authorTotal));


    }

    writer.close();

  }
}
