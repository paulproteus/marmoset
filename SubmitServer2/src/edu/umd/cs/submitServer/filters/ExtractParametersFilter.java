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

/*
 * Created on Jan 19, 2005
 */
package edu.umd.cs.submitServer.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Queries;
import edu.umd.cs.marmoset.modelClasses.Rubric;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.StudentSubmitStatus;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;
import edu.umd.cs.marmoset.modelClasses.TestSetup;
import edu.umd.cs.marmoset.utilities.SqlUtilities;
import edu.umd.cs.submitServer.MultipartRequest;
import edu.umd.cs.submitServer.ReleaseInformation;
import edu.umd.cs.submitServer.RequestParser;
import edu.umd.cs.submitServer.SubmitServerConstants;
import edu.umd.cs.submitServer.UserSession;

/**
 * Requires a projectPK and optionally a studentRegistrationPK.
 *
 * This filter stores studentRegistration, student, course, project,
 * studentSubmitStatus and submissionList attributes in the request.
 */
public class ExtractParametersFilter extends SubmitServerFilter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpSession session = request.getSession();
		UserSession userSession = (UserSession) session
				.getAttribute(USER_SESSION);
		RequestParser parser = new RequestParser(request,
				getSubmitServerFilterLog(), strictParameterChecking());
		String sortKey = parser.getOptionalCheckedParameter(SORT_KEY);
		Integer testRunPK = parser.getIntegerParameter("testRunPK", null);
		@Submission.PK Integer submissionPK = parser.getIntegerParameter("submissionPK", null);
		Integer projectPK = parser.getIntegerParameter("projectPK", null);
		Integer coursePK = parser.getIntegerParameter(COURSE_PK, null);
		@Student.PK Integer studentPK = Student.asPK(parser.getIntegerParameter("studentPK", null));
		@StudentRegistration.PK Integer studentRegistrationPK = StudentRegistration.asPK(parser.getIntegerParameter(
				"studentRegistrationPK", null));
		Integer testSetupPK = parser.getIntegerParameter("testSetupPK", null);
		Integer codeReviewerPK = parser.getIntegerParameter("codeReviewerPK", null);
		Integer codeReviewAssignmentPK = parser.getIntegerParameter("codeReviewAssignmentPK", null);

		
		String courseKey = parser.getParameter("courseKey");
		String projectNumber = parser.getParameter("projectNumber");
        
		String gradesServer = req.getServletContext().getInitParameter("grades.server");
        request.setAttribute("gradesServer", gradesServer);  
		MultipartRequest multipartRequest = (MultipartRequest) request
				.getAttribute(MULTIPART_REQUEST);
		if (multipartRequest != null) {
			projectPK = multipartRequest.getIntegerParameter("projectPK", null);
			coursePK = multipartRequest.getIntegerParameter(COURSE_PK, null);
			studentPK = Student.asPK(multipartRequest.getIntegerParameter("studentPK", null));
		}

		request.setAttribute(SubmitServerConstants.INSTRUCTOR_CAPABILITY,
				userSession.isSuperUser());
		request.setAttribute(SubmitServerConstants.INSTRUCTOR_ACTION_CAPABILITY, userSession.isSuperUser());

		TestRun testRun = null;
		TestOutcomeCollection testOutcomeCollection = null;
		Submission submission = null;
		CodeReviewer reviewer = null;
		ReleaseInformation releaseInformation = null;
		List<Submission> submissionList = null;
		Project project = null;
		Course course = null;
		Student student = null;
		Student user = null;
		StudentRegistration studentRegistration = null;
		List<Project> projectList = null;
		StudentSubmitStatus studentSubmitStatus = null;
		CodeReviewAssignment codeReviewAssignment = null;
		Collection<Submission> submissionsUnderReview = null;
		Collection<CodeReviewer> codeReviewersForAssignment = null;
		Collection<CodeReviewer> studentCodeReviewersForAssignment = null;
        
		Connection conn = null;
		try {
			conn = getConnection();
			user = Student.lookupByStudentPK(userSession.getStudentPK(), conn);
			request.setAttribute(USER, user);

			List<Course> courseList;
			if (user.isSuperUser()) {
				courseList = Course.lookupAll(conn);
			} else {
				courseList = Course.lookupAllByStudentPK(userSession.getStudentPK(), conn);
			}
			request.setAttribute(SubmitServerConstants.COURSE_LIST, courseList);
			if (courseList.size() == 1) {
				request.setAttribute("singleCourse", Boolean.TRUE);
				Course onlyCourse = courseList.get(0);
				coursePK = onlyCourse.getCoursePK();
				userSession.setOnlyCoursePK(coursePK);
			} else
				request.setAttribute("singleCourse", Boolean.FALSE);
			if (courseKey != null && projectNumber != null) {
			    course = Course.lookupByCourseKey(courseKey, conn);
			    project = Project.lookupByCourseAndProjectNumber(course.getCoursePK(), projectNumber, conn);
			}

            if (testRunPK != null) {
                // Get Test Run
                testRun = TestRun.lookupByTestRunPK(testRunPK, conn);
                submissionPK = testRun.getSubmissionPK();
                submission = Submission
                        .lookupBySubmissionPK(submissionPK, conn);
            }
            if (codeReviewerPK != null) {
                reviewer = CodeReviewer.lookupByPK(codeReviewerPK, conn);
                if (userSession.getStudentPK()
                        != reviewer.getStudentPK()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication Error: you can only specify your own reviews");
                    return;
                }
                submissionPK = reviewer.getSubmissionPK();
                codeReviewAssignmentPK = reviewer.getCodeReviewAssignmentPK();
            }

            if (submissionPK != null) {
                // Get Submission
                submission = Submission
                        .lookupBySubmissionPK(submissionPK, conn);
                // get the defaultTestRun, unless we already got a test run from
                // the previous
                // if block
                if (testRun == null) {
                    testRun = TestRun.lookupByTestRunPK(
                            submission.getCurrentTestRunPK(), conn);
                }
                // if we found a testRun, get its testOutcomeCollection
                if (testRun != null) {
                    testOutcomeCollection = TestOutcomeCollection
                            .lookupByTestRunPK(testRun.getTestRunPK(), conn);
                }

                projectPK = submission.getProjectPK();
                if (studentRegistration == null)
                    studentRegistration = StudentRegistration.lookupBySubmissionPK(
                            submissionPK, conn);
                else if (!studentRegistration.getStudentRegistrationPK().equals(submission.getStudentRegistrationPK()))
                        throw new ServletException("Inconsistent student registration and submission");
                
                if (submission.getNumTestRuns() >= 1) {
                    List<TestRun> testRunList = TestRun
                            .lookupAllBySubmissionPK(submissionPK, conn);
                    request.setAttribute(TEST_RUN_LIST, testRunList);

                    // Collections.reverse(testRunList);
                    // map testSetupPKs to their corresponding project jarfiles
                    Map<Integer, TestSetup> testSetupMap = new HashMap<Integer, TestSetup>();
                    for (TestRun tr : testRunList) {
                        // make sure we have already mapped the project jarfile
                        // to its PK
                        if (!testSetupMap.containsKey(tr.getTestSetupPK())) {
                            TestSetup jarfile = TestSetup.lookupByTestSetupPK(
                                    tr.getTestSetupPK(), conn);
                            testSetupMap.put(jarfile.getTestSetupPK(), jarfile);
                        }
                    }
                    request.setAttribute("testSetupMap", testSetupMap);
                }
            }

            
			if (studentRegistrationPK != null && studentRegistration == null) {
				studentRegistration = StudentRegistration
						.lookupByStudentRegistrationPK(studentRegistrationPK,
								conn);
			}
			if (studentRegistration != null) {
				studentPK = studentRegistration.getStudentPK();
				coursePK = studentRegistration.getCoursePK();
			}
			boolean studentSpecifiedByInstructor = studentPK != null && !studentPK.equals(userSession.getStudentPK());

			if (studentPK == null)
				studentPK = userSession.getStudentPK();


			if (codeReviewAssignmentPK != null && codeReviewAssignmentPK.intValue() != 0) {
				codeReviewAssignment = CodeReviewAssignment.lookupByPK(codeReviewAssignmentPK, conn);
				projectPK = codeReviewAssignment.getProjectPK();
				submissionsUnderReview = Submission.getSubmissionsUnderReview(codeReviewAssignmentPK, conn);
				codeReviewersForAssignment = CodeReviewer.lookupByCodeReviewAssignmentPK(codeReviewAssignmentPK, conn);
				studentCodeReviewersForAssignment
				 = new ArrayList<CodeReviewer>();
				for(CodeReviewer  r : codeReviewersForAssignment) 
				    if (!r.isInstructor() && !r.isAuthor())
				        studentCodeReviewersForAssignment.add(r);
			}


			if (projectPK != null) {
				// Get Project
				project = Project.getByProjectPK(projectPK, conn);

				coursePK = project.getCoursePK();
				if (!project.getVisibleToStudents()
						&& !userSession.hasInstructorCapability(coursePK))
					throw new ServletException("Project is not visible");

				request.setAttribute(PROJECT, project);

				StudentRegistration canonicalAccount = StudentRegistration
						.lookupByStudentRegistrationPK(
								project.getCanonicalStudentRegistrationPK(),
								conn);
				request.setAttribute("canonicalAccount", canonicalAccount);

				student = Student.lookupByStudentPK(studentPK, conn);
				request.setAttribute(STUDENT, student);
				if (!studentSpecifiedByInstructor 
				        && !student.getCampusUID().equals(user.getCampusUID())
				        && !user.isSuperUser()) {
					StudentRegistration reg = StudentRegistration
							.lookupByStudentPKAndCoursePK(studentPK, coursePK,
									conn);
					if (reg == null) {
						throw new ServletException(
								"You cannot view information about this person");
					}
				}

				if (userSession.hasInstructorCapability(coursePK)) {
					request.setAttribute(REVIEW_ASSIGNMENTS_FOR_PROJECT, CodeReviewAssignment.lookupByProjectPK(projectPK, conn));

				}

				if (submissionPK == null || !studentSpecifiedByInstructor) {
					// Get Collection
					submissionList = Submission
							.lookupAllByStudentPKAndProjectPK(studentPK,
									projectPK, conn);

					releaseInformation = new ReleaseInformation(project,
							submissionList);
				}
			}

			if (testSetupPK == null && project != null)
				testSetupPK = project.getTestSetupPK();

			if (testSetupPK != null && !testSetupPK.equals(0)) {
				TestSetup testSetup = TestSetup.lookupByTestSetupPK(
						testSetupPK, conn);
				request.setAttribute(TEST_SETUP, testSetup);
				projectPK = testSetup.getProjectPK();
				if (project == null) {
				    project = Project.getByProjectPK(projectPK, conn);

	                coursePK = project.getCoursePK();
	                if (!project.getVisibleToStudents()
	                        && !userSession.hasInstructorCapability(coursePK))
	                    throw new ServletException("Project is not visible");

	                request.setAttribute(PROJECT, project);
				} else if (project.getProjectPK() != projectPK)
				    throw new IllegalArgumentException(
				            String.format("test setup %d is for project_pk %d, but project_pk %d specified",
				                    testSetup.getTestSetupPK(),
				                    testSetup.getProjectPK(),
				                    project.getProjectPK()));

			}


			if (coursePK != null && !studentSpecifiedByInstructor
					&& userSession.hasInstructorCapability(coursePK)) {
				// get list of students

				// studentRegistrationSet is the sorted component based on the
				// sort key
				List<StudentRegistration> studentRegistrationCollection;
				if (projectPK == null) {
					studentRegistrationCollection = StudentRegistration
							.lookupAllByCoursePK(coursePK, conn);
				} else {

					studentRegistrationCollection = StudentRegistration
							.lookupAllWithAtLeastOneSubmissionByProjectPK(
									projectPK, conn);

					request.setAttribute("studentSubmitStatusMap",
							StudentSubmitStatus.lookupAllByProjectPK(projectPK,
									conn));

					List<StudentRegistration> listOfAllStudents = StudentRegistration
							.lookupAllByCoursePK(coursePK, conn);
					TreeSet<StudentRegistration> studentsWithoutSubmissions = new TreeSet<StudentRegistration>(
							StudentRegistration.getComparator(sortKey));

					TreeSet<StudentRegistration> allStudents = new TreeSet<StudentRegistration>(
							StudentRegistration.getComparator(sortKey));
					allStudents.addAll(listOfAllStudents);
					request.setAttribute("allStudents",
							allStudents);

					studentsWithoutSubmissions.addAll(listOfAllStudents);
					studentsWithoutSubmissions
							.removeAll(studentRegistrationCollection);
					for (Iterator<StudentRegistration> i = studentsWithoutSubmissions
							.iterator(); i.hasNext();) {
						StudentRegistration s = i.next();
						if (!s.isActive() || !s.isNormalStudent())
							i.remove();
					}
					request.setAttribute("studentsWithoutSubmissions",
							studentsWithoutSubmissions);

				}
				TreeSet<StudentRegistration> studentRegistrationSet = new TreeSet<StudentRegistration>(
						StudentRegistration.getComparator(sortKey));
				TreeSet<StudentRegistration> staffStudentRegistrationSet = new TreeSet<StudentRegistration>(
						StudentRegistration.getComparator(sortKey));
				TreeSet<StudentRegistration> justStudentRegistrationSet = new TreeSet<StudentRegistration>(
						StudentRegistration.getComparator(sortKey));
				studentRegistrationSet
						.addAll(studentRegistrationCollection);

				Map<Integer, StudentRegistration> studentRegistrationMap = new HashMap<Integer, StudentRegistration>();
				for (StudentRegistration registration : studentRegistrationSet) {
					studentRegistrationMap.put(
							registration.getStudentRegistrationPK(),
							registration);
					if (registration.isInstructor())
						staffStudentRegistrationSet.add(registration);
					else
						justStudentRegistrationSet.add(registration);
				}
				
				
				request.setAttribute("studentRegistrationMap",
						studentRegistrationMap);

				request.setAttribute("studentRegistrationCollection",
						studentRegistrationCollection);
				request.setAttribute(STUDENT_REGISTRATION_SET,
						studentRegistrationSet);
				request.setAttribute(JUST_STUDENT_REGISTRATION_SET,
						justStudentRegistrationSet);
				request.setAttribute(STAFF_STUDENT_REGISTRATION_SET,
						staffStudentRegistrationSet);
				TreeMap<String, SortedSet<StudentRegistration>> sectionMap =
				        new TreeMap<String, SortedSet<StudentRegistration>>();
				for(StudentRegistration sr : justStudentRegistrationSet) {
				    String section = sr.getSection();
				    if (section == null || section.isEmpty())
				        continue;
				    SortedSet<StudentRegistration> inSection = sectionMap.get(section);
				    if (inSection == null) {
				        inSection = new TreeSet<StudentRegistration>();
				        sectionMap.put(section, inSection);
				    }
				    inSection.add(sr);
				}
				request.setAttribute(SECTION_MAP,
				        sectionMap);
				request.setAttribute(SECTIONS, sectionMap.keySet());
                

			}

			if (coursePK != null && studentPK != null) {
				Map<Integer, StudentSubmitStatus> projectToStudentSubmitStatusMap = lookupStudentSubmitStatusMapByCoursePKAndStudentPK(
						coursePK, studentPK, conn);
				request.setAttribute("projectToStudentSubmitStatusMap",
						projectToStudentSubmitStatusMap);
			}

			if (studentRegistration != null)
				studentPK = studentRegistration.getStudentPK();
			else {
				if (studentPK == null)
					studentPK = userSession.getStudentPK();
				studentRegistration = StudentRegistration
						.lookupByStudentPKAndCoursePK(studentPK, coursePK, conn);
			}

			if (studentRegistration != null && project != null) {
				studentSubmitStatus = StudentSubmitStatus
						.lookupByStudentRegistrationPKAndProjectPK(
								studentRegistration.getStudentRegistrationPK(),
								project.getProjectPK(), conn);
				if (studentSubmitStatus != null && studentSubmitStatus.getPartnerPK() != null
						&& studentSubmitStatus.getPartnerPK().intValue() > 0) {
					StudentRegistration partner =
						StudentRegistration.lookupByStudentRegistrationPK(studentSubmitStatus.getPartnerPK(), conn);
					request.setAttribute("projectPartner", partner);
				} else if (project.isPair()) {
					Collection<StudentRegistration> potentialPartners
					= StudentRegistration.lookupAllByCoursePK(project.getCoursePK(), conn);
					TreeSet<StudentRegistration> sortedPartners = new TreeSet<StudentRegistration>(
							StudentRegistration.nameComparator);
					sortedPartners
							.addAll(potentialPartners);
					sortedPartners.remove(studentRegistration);
					request.setAttribute("potentialPartners", sortedPartners);


				}
			}

			if (coursePK != null) {
				// Get Course and all of its projects
				course = Course.lookupByCoursePK(coursePK, conn);
				projectList = Project.lookupAllByCoursePK(coursePK, conn);
				Collections.reverse(projectList);
				request.setAttribute(COURSE, course);
				request.setAttribute(PROJECT_LIST, projectList);
				request.setAttribute(
						SubmitServerConstants.INSTRUCTOR_CAPABILITY, userSession
										.hasInstructorCapability(coursePK));
				request.setAttribute(SubmitServerConstants.INSTRUCTOR_ACTION_CAPABILITY, userSession
								.hasInstructorActionCapability(coursePK));
			} else if (user.getCanImportCourses()) {
			        request.setAttribute(SubmitServerConstants.INSTRUCTOR_CAPABILITY, true);
			        request.setAttribute(SubmitServerConstants.INSTRUCTOR_ACTION_CAPABILITY, true);
			}


			 if ( submission != null && reviewer == null) {
				if (codeReviewAssignment != null) {
					reviewer = CodeReviewer.lookupByCodeReviewAssignmentSubmissionAndStudentPK(
							codeReviewAssignment.getCodeReviewAssignmentPK(),
							submission.getSubmissionPK(),
							userSession.getStudentPK(), conn);

				} else {
					reviewer = CodeReviewer.lookupBySubmissionAndStudentPK(submission.getSubmissionPK(),
							user.getStudentPK(), conn);
					if (reviewer != null)
						codeReviewAssignment = reviewer.getCodeReviewAssignment();
				}

				request.setAttribute(REVIEWER, reviewer);
			}
			
			if (codeReviewAssignment != null) {
			    Collection<Rubric> rubrics = Rubric.lookupByAssignment(
			            codeReviewAssignment.getCodeReviewAssignmentPK(), conn);
			    request.setAttribute("rubrics", rubrics);
			    Map<Integer,Rubric> rubricMap = new HashMap<Integer,Rubric>();
			    for(Rubric r : rubrics) 
			        rubricMap.put(r.getRubricPK(), r);
			    request.setAttribute("rubricMap", rubricMap);
			              
			}
			
		     boolean viewOfAnotherStudentsCode = !userSession.getStudentPK()
	                    .equals(studentPK);
	            boolean isCodeReviewer = reviewer != null && submission != null
	                    && reviewer.getSubmissionPK() == submission.getSubmissionPK();

			if (project != null && submission == null) {
			    Set<Integer> submissionsWithReviews;
			    if (userSession.hasInstructorCapability(coursePK) && !viewOfAnotherStudentsCode)
			        submissionsWithReviews = Submission.lookupSubmissionsWithReviews(project, conn);
			    else
			        submissionsWithReviews = Submission.lookupSubmissionsWithReviews(project, studentRegistration, conn);
			    request.setAttribute("submissionsWithReviews", submissionsWithReviews);
			}

            if (student == null && studentPK != null) {
                student = Student.lookupByStudentPK(studentPK, conn);
                request.setAttribute(STUDENT, student);
            }
            
			if (viewOfAnotherStudentsCode
					&& !isCodeReviewer
					&& !userSession.hasInstructorCapability(coursePK)
					&& !user.getCampusUID().equals(student.getCampusUID())) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
						"Authentication Error");
				return;
			}

			request.setAttribute("instructorViewOfStudent",
					Boolean.valueOf(viewOfAnotherStudentsCode));
			
			request.setAttribute("instructorCourses",
			       userSession.isSuperUser() ? courseList :  userSession.getInstructorCourses());
			if (testRun != null)
				request.setAttribute(TEST_RUN, testRun);
			if (testOutcomeCollection != null)
				request.setAttribute(TEST_OUTCOME_COLLECTION,
						testOutcomeCollection);
			if (submission != null) {
				request.setAttribute("submission", submission);

			}
			if (releaseInformation != null)
				request.setAttribute("releaseInformation", releaseInformation);
			if (submissionList != null) {
				Collections.reverse(submissionList);
				request.setAttribute(SUBMISSION_LIST, submissionList);
			}

			if (studentRegistration != null)
				request.setAttribute(STUDENT_REGISTRATION, studentRegistration);

			 if (project != null)
	                request.setAttribute(PROJECT, project);

			if (studentSubmitStatus != null)
				request.setAttribute(STUDENT_SUBMIT_STATUS, studentSubmitStatus);

			if (codeReviewAssignment != null)
				request.setAttribute(SubmitServerConstants.CODE_REVIEW_ASSIGNMENT, codeReviewAssignment);

			if (codeReviewersForAssignment != null) { 
				request.setAttribute("codeReviewersForAssignment", codeReviewersForAssignment);
				request.setAttribute("studentCodeReviewersForAssignment", studentCodeReviewersForAssignment);
			}

			if (submissionsUnderReview != null)
				request.setAttribute("submissionsUnderReview", submissionsUnderReview);

			if (course != null)
				request.setAttribute(COURSE, course);
			
			if (projectList != null)
				request.setAttribute(PROJECT_LIST, projectList);
			if (sortKey != null)
				request.setAttribute(SORT_KEY, sortKey);
		} catch (SQLException e) {
			throw new ServletException(e);
		} finally {
			releaseConnection(conn);
		}

		chain.doFilter(request, response);
	}

	/**
	 * Returns a map from projectPKs to studentSubmitStatus records for a given
	 * student and course. The parameters to this are sort of a hack because
	 * this method is only called in this filter when coursePK != null and
	 * studentPK != null to get the submitStatus records to pass to
	 * /view/instructor/student.jsp
	 *
	 * @param coursePK
	 * @param studentPK
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<Integer, StudentSubmitStatus> lookupStudentSubmitStatusMapByCoursePKAndStudentPK(
			Integer coursePK, @Student.PK Integer studentPK, Connection conn)
			throws SQLException {
		String query = " SELECT "
				+ StudentSubmitStatus.ATTRIBUTES
				+ " FROM student_submit_status, student_registration "
				+ " WHERE student_submit_status.student_registration_pk = student_registration.student_registration_pk "
				+ " AND student_registration.course_pk = ? "
				+ " AND student_registration.student_pk = ? ";

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(query);
			SqlUtilities.setInteger(stmt, 1, coursePK);
			SqlUtilities.setInteger(stmt, 2, studentPK);

			ResultSet rs = stmt.executeQuery();

			Map<Integer, StudentSubmitStatus> map = new LinkedHashMap<Integer, StudentSubmitStatus>();
			while (rs.next()) {
				StudentSubmitStatus studentSubmitStatus = new StudentSubmitStatus();
				studentSubmitStatus.fetchValues(rs, 1);
				map.put(studentSubmitStatus.getProjectPK(), studentSubmitStatus);
			}
			return map;
		} finally {
			Queries.closeStatement(stmt);
		}
	}
}
