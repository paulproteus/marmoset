package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import edu.umd.cs.marmoset.utilities.Objects;
import edu.umd.cs.marmoset.utilities.TextUtilities;

/** Provide a summary of a code review */
public class CodeReviewSummary  implements Comparable<CodeReviewSummary>{
	private static final String FINDBUGS_TEST_TYPE = "findbugs";


  final @Nonnull Submission submission;
  final @Nonnull  Project project;
  final @Nonnull  Student viewerAsStudent;
  final @Nonnull CodeReviewer viewerAsReviewer;
  final @CheckForNull CodeReviewer author;
  final @CheckForNull CodeReviewAssignment assignment; /** assignment is null if this is an ad-hoc review */

	/**
	 * Map from codeReviewerPK to CodeReviewer
	 */
	final Map<Integer, CodeReviewer> reviewers = new HashMap<Integer, CodeReviewer>();

	/**
	 * Map from threadPK to CodeReviewThread
	 */
	final Map<Integer, CodeReviewThread> threads = new HashMap<Integer, CodeReviewThread>();
	/**
	 * Map from threadPK to sorted set of comments
	 */
	final Map<Integer, NavigableSet<CodeReviewComment>> comments
	= new HashMap<Integer, NavigableSet<CodeReviewComment>>();

	final Set<CodeReviewComment> allComments = new TreeSet<CodeReviewComment>();
	boolean anyUnpublishedDrafts;

	/** map from rubricPK to Rubric */
	Map<Integer,Rubric> rubrics = new HashMap<Integer,Rubric>();
	Map<Integer,Rubric> unevaluatedRubrics = new HashMap<Integer,Rubric>();
	/** Map from threadPK to RubricEvaluation */
	Map<Integer,RubricEvaluation> rubricEvaluations = new HashMap<Integer,RubricEvaluation>();

	public CodeReviewSummary(Connection conn, CodeReviewer reviewer) throws SQLException {
		this(conn, reviewer.getSubmission(), reviewer.getStudent(), reviewer);
	}
	private CodeReviewSummary(Connection conn, Submission submission, Student student, CodeReviewer reviewer) throws SQLException {
	    if (student == null)
            throw new NullPointerException("No student " + reviewer.getStudentPK() 
                    + " for code reviewer " + reviewer.getCodeReviewerPK());
        if (submission == null)
            throw new NullPointerException("No submission " + reviewer.getSubmissionPK() 
                    + " for code reviewer " + reviewer.getCodeReviewerPK());
        
        this.submission = submission;
		this.viewerAsStudent = student;
		this.viewerAsReviewer = reviewer;
		this.project = Project.getByProjectPK(submission.getProjectPK(), conn);

		if (project == null)
		    throw new NullPointerException("No project " + submission.getProjectPK() 
                    + " for submission" + submission.getProjectPK());
		
		this.author = CodeReviewer.lookupAuthorBySubmission(
				submission.getSubmissionPK(), conn);

		this.assignment = reviewer.getCodeReviewAssignment();
		
		if (!hasFindbugsReviewer(conn)) {
			createFindbugsThreads(conn, submission);
		}
		
		for(CodeReviewer r : CodeReviewer.lookupBySubmissionPK(submission.getSubmissionPK(), conn)) {
			reviewers.put(r.getCodeReviewerPK(), r);
		}
	    for(CodeReviewThread t : CodeReviewThread.lookupBySubmissionPK(submission.getSubmissionPK(), conn)) 
	        if (isVisible(t)) {
	          threads.put(t.getCodeReviewThreadPK(), t);
	      }

		for(CodeReviewComment c : CodeReviewComment.lookupBySubmissionPK(submission.getSubmissionPK(), conn)) {
			int threadPK = c.getCodeReviewThreadPK();
			if (!threads.containsKey(threadPK) || !isVisible(c))
				continue;
			NavigableSet<CodeReviewComment> cs = comments.get(threadPK);
			if (cs == null) {
				cs = new TreeSet<CodeReviewComment>();
				comments.put(threadPK, cs);
			}
			cs.add(c);
			allComments.add(c);
			if (c.isDraft() && isAuthor(c))
				anyUnpublishedDrafts = true;
		}
		Set<Integer> threadsWithContent = new HashSet<Integer>(comments.keySet());

		if (assignment != null) 
			for(Rubric r : Rubric.lookupByAssignment(assignment.getCodeReviewAssignmentPK(), conn)) 
			    rubrics.put(r.getRubricPK(), r);
			        
		if (!isReviewerIsTheAuthor())
	      unevaluatedRubrics.putAll(rubrics);
	
        for (RubricEvaluation e : RubricEvaluation.lookupBySubmissionPK(submission.getSubmissionPK(), conn))
            if (isVisible(e)) {
                int threadPK = e.getCodeReviewThreadPK();
                assert threads.containsKey(threadPK);
                NavigableSet<CodeReviewComment> cs = comments.get(threadPK);
                if (cs == null) {
                    cs = new TreeSet<CodeReviewComment>();
                    comments.put(threadPK, cs);
                }
                rubricEvaluations.put(threadPK, e);
                @Rubric.PK int rubricPK = e.getRubricPK();
                if (!rubrics.containsKey(rubricPK)) {
                    Rubric r = Rubric.lookupByPK(rubricPK, conn);
                    rubrics.put(rubricPK, r);
                    
                }

                if (e.getCodeReviewerPK() == reviewer.getCodeReviewerPK()) {
                    if (e.getStatus().equals("LIVE"))
                        unevaluatedRubrics.remove(e.getRubricPK());
                    else if (e.getStatus().equals("NEW") || e.getStatus().equals("DRAFT"))
                        anyUnpublishedDrafts = true;
                }
            }
		threadsWithContent.addAll(rubricEvaluations.keySet());
		threads.keySet().retainAll(threadsWithContent);
		for(CodeReviewThread t : threads.values()) {
            assert isVisible(t);
            assert hasContent(t);
		}  
	}

	public boolean isReviewerIsTheAuthor() {
	    return author != null && author.equals(viewerAsReviewer);
	}
	private boolean isDebug() {
		return false;
	}

	public Project getProject() {
		return project;
	}

	public Student getViewer() {
		return viewerAsStudent;
	}
	public CodeReviewer getAuthor() {
		return author;
	}

	public Submission getSubmission() {
		return submission;
	}

	public @Submission.PK int getSubmissionPK() {
		return submission.getSubmissionPK();
	}

	public String getDescription() {
		if (assignment == null)
			return "ad-hoc";
		return assignment.getDescription();
	}

	public CodeReviewAssignment getAssignment() {
		return assignment;
	}

	public CodeReviewer getCodeReviewer() {
		return viewerAsReviewer;
	}
	public @CodeReviewer.PK int getCodeReviewerPK() {
		return viewerAsReviewer.getCodeReviewerPK();
	}

	public boolean getAnyUnpublishedDraftsByViewer() {
		return anyUnpublishedDrafts;
	}

	public boolean isRequestReviewOnPublish() {
	    Set<Integer> reviewers = getCodeReviewerMap().keySet();
	    return isReviewerIsTheAuthor() && !viewerAsReviewer.isInstructor()
	          && reviewers.size() == 1;
	     
	}
	public boolean isTimely() {
	    if (viewerAsStudent == null)
	        return false;
		if (isNeedsResponse())
			return true;
		if (getAnyUnpublishedDraftsByViewer())
			return true;
		Date now = new Date();
		if (assignment != null && (author == null || viewerAsStudent.getStudentPK() != author.getStudentPK())) {
			if (assignment.getDeadline().after(now))
				return true;
			if (getNumCommentsByViewer() == 0)
				return true;
		}
		return false;
	}

	public Map<Integer,CodeReviewer> getCodeReviewerMap() {
		return Collections.unmodifiableMap(reviewers);
	}
	public Map<Integer,CodeReviewThread> getThreadMap() {
		return Collections.unmodifiableMap(threads);
	}


	public String getOpenCommentAuthors() {

		StringBuilder buf = new StringBuilder();
		for(CodeReviewer r : getCommentsFrom(false)) {
			buf.append(r.getName());
			buf.append(", ");
		}
		if (buf.length() == 0)
			return "";
		buf.setLength(buf.length()-2);
		return buf.toString();
	}

	public String getCommentAuthors() {
		StringBuilder buf = new StringBuilder();
		for(CodeReviewer s : getCommentsFrom(true)) {
			buf.append(s.getName());
			buf.append(", ");
		}
		if (buf.length() == 0)
			return "";
		buf.setLength(buf.length()-2);
		return buf.toString();
	}
	private Collection<CodeReviewer> getCommentsFrom(boolean openOrClosed) {
		Collection<CodeReviewer> result = new TreeSet<CodeReviewer>();
		for (CodeReviewThread t : threads.values()) {
			SortedSet<CodeReviewComment> comments = getComments(t);
			for (CodeReviewComment c : comments) {
				if (isVisible(c) && (openOrClosed || !c.isAck())) {
					CodeReviewer s = getAuthor(c);
					result.add(s);
				}
			}
		}
		result.remove(viewerAsReviewer);
		return result;

	}

	private CodeReviewer getAuthor(CodeReviewComment c) {
		return reviewers.get(c.getCodeReviewerPK());
	}

    public boolean isNeedsResponse(CodeReviewThread t) {
        NavigableSet<CodeReviewComment> comments = getComments(t);
        if (comments.isEmpty()) {
            if (t.getRubricEvaluationPK() == 0)
                throw new IllegalStateException("thread " + t.getCodeReviewThreadPK() + " is empty");
            return viewerIsAuthor();
        }

        for (CodeReviewComment last : comments.descendingSet()) {
            if (last.isDraft())
                continue;
            if (last.isAck())
                return false;
            if (last.isBy(viewerAsReviewer))
                return false;
            if (viewerIsAuthor())
                return true;
            // viewer is code reviewer. Needs to respond if comment is from
            // author and thread was either started by review or by
            // author and review is instructor
            if (last.isBy(author)) {
                @CodeReviewer.PK
                int startedBy = t.getCreatedBy();
                if (startedBy == viewerAsReviewer.getCodeReviewerPK())
                    return true;
                if (startedBy == author.getCodeReviewerPK() && viewerAsReviewer.isInstructor())
                    return true;
            }
            return false;
        }
        return false;
    }
    /**
     * @return
     */
    public boolean viewerIsAuthor() {
        return author != null && viewerAsReviewer.getCodeReviewerPK() == author.getCodeReviewerPK();
    }

	public boolean isNeedsResponse() {
		for(CodeReviewThread t : threads.values())
			if (isNeedsResponse(t)) {
				if (isDebug())
					System.out.printf("Thread %d needs a response%n",
							t.getCodeReviewThreadPK());
				return true;
			}
		return false;
	}

	private boolean isAwaitingResponse(CodeReviewThread t) {
	    if (viewerAsStudent == null)
	        return false;
		SortedSet<CodeReviewComment> comments = getComments(t);
        if (!comments.isEmpty()) {
            CodeReviewComment last = comments.last();
            return !last.isAck() && !last.isDraft() && last.isBy(viewerAsReviewer);
        }
		int rubricEvaluationPK = t.getRubricEvaluationPK();
		if (rubricEvaluationPK == 0)
		    return false;
		return viewerIsAuthor();
	}

	public boolean isAwaitingResponse() {
		for(CodeReviewThread t : threads.values())
			if (isAwaitingResponse(t)) {
				return true;
			}
		return false;
	}

	public int getNumComments() {
		int count = 0;
		for(CodeReviewComment c : allComments)
			if (!c.isJustAck() && !c.isDraft())
				count++;
		for(RubricEvaluation e : rubricEvaluations.values()) {
		    if (!e.isDraft())
		        count++;
		}
		return count;
	}
	public int getNumCommentsByViewer() {
		int count = 0;
		for(CodeReviewComment c : allComments)
			if (byViewer(c) && !c.isJustAck() && !c.isDraft())
                count++;
        for (RubricEvaluation e : rubricEvaluations.values())
            if (e.isBy(viewerAsReviewer) && !e.isDraft())
                count++;
        return count;
	}
	public int getNumCommentsByOthers() {
		int count = 0;
		for(CodeReviewComment c : allComments)
			if (!byViewer(c) && !c.isJustAck() && !c.isDraft())
				count++;
        for (RubricEvaluation e : rubricEvaluations.values())
            if (!e.isBy(viewerAsReviewer) && !e.isDraft())
                count++;
		return count;
	}

  
    private boolean hasContent(CodeReviewThread t) {
        return !getComments(t).isEmpty()
         || rubricEvaluations.get(t.getCodeReviewThreadPK()) != null;
    }

    private boolean isVisible(RubricEvaluation e) {
        if (byViewer(e))
            return true;
        if (e.getStatus().equals("DRAFT"))
            return false;
        if (e.getStatus().equals("DEAD")) {
            CodeReviewThread t = threads.get(e.getCodeReviewThreadPK());
            if (t == null)
                return false;
            if (t.getRubricEvaluationPK() != e.getRubricEvaluationPK())
                return false;
        }
        return canSeeCommentsBy(e.getCodeReviewerPK());
    }

    private boolean isVisible(CodeReviewComment c) {
        if (byViewer(c))
            return true;
        if (c.isDraft())
            return false;
        return canSeeCommentsBy(c.getCodeReviewerPK());
    }

    private boolean isVisible(CodeReviewThread t) {
        return canSeeCommentsBy(t.getCreatedBy());
    }

    private boolean matchingAssignment(@CodeReviewer.PK int codeReviewerPK) {
        if (assignment == null)
            return true;
        CodeReviewer commentAuthor = reviewers.get(codeReviewerPK);
        return commentAuthor.getCodeReviewAssignmentPK() == assignment.getCodeReviewAssignmentPK();
    }
    
 
    
    private boolean canSeeCommentsBy(@CodeReviewer.PK int codeReviewerPK) {
        if (codeReviewerPK == viewerAsReviewer.getCodeReviewerPK())
            return true;
        if (viewerAsReviewer.isOmniscient())
                 return true;
        if (author != null &&  author.getStudentPK() == viewerAsStudent.getStudentPK())
                return true;
        if (assignment != null && assignment.isOtherReviewsVisible() && matchingAssignment(codeReviewerPK))
             return true;
        return false;

    }
    

	private boolean byViewer(CodeReviewComment c) {
		return  c.isBy(viewerAsReviewer);
    }

    private boolean byViewer(RubricEvaluation e) {
        return e.getCodeReviewerPK() == viewerAsReviewer.getCodeReviewerPK();
    }

    private boolean isAuthor(CodeReviewComment c) {
		return byViewer(c);
	}
	public NavigableSet<CodeReviewComment> getComments(CodeReviewThread thread) {
		return comments.get(thread.getCodeReviewThreadPK());
	}

	public Collection<CodeReviewer> getReviewers() {
		return reviewers.values();
	}

	private long getTimestamp() {

		long result = Long.MIN_VALUE;
		for(CodeReviewThread t : threads.values()) {
			for(CodeReviewComment c : comments.get(t.getCodeReviewThreadPK())) {
				long tm = c.getModified().getTime();
				if (result < tm)
					result = tm;
			}
		}
		return result;
	}

    private long getTimestampByViewer() {
        long result = Long.MIN_VALUE;
        if (viewerAsReviewer == null)
            return result;
        for (CodeReviewThread t : threads.values()) {
            for (CodeReviewComment c : comments.get(t.getCodeReviewThreadPK())) {
                if (c.isBy(viewerAsReviewer))
                    result = Math.max(result, c.getModified().getTime());
            }
        }
        for (RubricEvaluation e : rubricEvaluations.values())
            if (e.isBy(viewerAsReviewer)) {
                long time = e.getModified().getTime();
                long newResult = Math.max(result, time);
                result = newResult;
            }

        return result;
    }

    private long getTimestampByOthers() {
        long result = Long.MIN_VALUE;
        if (viewerAsReviewer == null)
            return result;
        for (CodeReviewThread t : threads.values()) {
            for (CodeReviewComment c : comments.get(t.getCodeReviewThreadPK())) {
                if (!c.isBy(viewerAsReviewer)) {
                    long time = c.getModified().getTime();
                    long newResult = Math.max(result, time);
                    result = newResult;
                }
            }
        }
        for (RubricEvaluation e : rubricEvaluations.values())
            if (!e.isBy(viewerAsReviewer)) {
                long time = e.getModified().getTime();
                long newResult = Math.max(result, time);
                result = newResult;
            }

        return result;
    }

	public Timestamp getLastUpdateByViewer() {
		return new Timestamp(getTimestampByViewer());
	}
	public Timestamp getLastUpdateByOthers() {
		return new Timestamp(getTimestampByOthers());
	}

	public Timestamp getLastUpdate() {
		return new Timestamp(getTimestamp());
	}

	public Collection<Rubric> getUnassignedRubrics() {
		return unevaluatedRubrics.values();
	}

	@CheckForNull
    public RubricEvaluation getRubricForThread(CodeReviewThread t) {
		return rubricEvaluations.get(t.getCodeReviewThreadPK());
	}
	public Rubric getRubric(@Rubric.PK int rubricPK) {
		return rubrics.get(rubricPK);
	}


    private boolean hasFindbugsReviewer(Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement("SELECT * FROM code_reviewer " + "WHERE is_automated = 1 AND known_as = ? "
                + "AND submission_pk = ?");
        try {
            Queries.setStatement(st, "FindBugs", submission.getSubmissionPK());
            ResultSet rs = st.executeQuery();
            return rs.next();
        } finally {
            st.close();
        }
    }

    private CodeReviewer createFindbugsReviewer(Connection conn) throws SQLException {
        CodeReviewer.Builder builder = new CodeReviewer.Builder(conn, submission.getSubmissionPK());
        builder.setAutomated("FindBugs");
        return builder.build();
    }
	
    public static @CheckForNull String getFullPath(Collection<String> fullPaths, String name) {
        String result = null;
        for(String s : fullPaths) {
            if (s.endsWith(name)) {
                if (result != null) return null;
                result = s;
            }
        }
        return result;
    }
	/** 
	 * Create threads for all the FindBugs warnings for the submission.
	 * 
	 * @param conn
	 * @param submission TODO
	 * @return
	 * @throws SQLException
	 */
	private void createFindbugsThreads( Connection conn, Submission submission) {
	    
        Integer testRunPK = submission.getCurrentTestRunPK();
        if (testRunPK == null)
            return;
        PreparedStatement findbugsOutcomes = null;
        try {
             findbugsOutcomes
            = conn.prepareStatement("SELECT test_outcomes.* FROM test_outcomes "
                    + "WHERE test_run_pk = ? AND test_type = ?");
          
            Queries.setStatement(findbugsOutcomes, testRunPK, FINDBUGS_TEST_TYPE);
            ResultSet rs = findbugsOutcomes.executeQuery();
            if (!rs.next())
                return;
            CodeReviewer findbugsReviewer = createFindbugsReviewer(conn);
            byte [] submissionBytes = submission.downloadArchive(conn);
            SortedSet<String> filenames 
              = TextUtilities.scanTextFileNamesInZip(submissionBytes);
            TestRun run = TestRun.lookupByTestRunPK(testRunPK, conn);
            Timestamp now = run.getTestTimestamp();
            int count = 0;
            do {
                String location = rs.getString("short_test_result");
                Preconditions.checkState(!Strings.isNullOrEmpty(location), "Invalid findbugs location");
                System.out.println(location);
                Matcher m = TestOutcome.FINDBUGS_LOCATION_REGEX.matcher(location);
                if (!m.matches()) {
                    System.out.println("skipping " + location);
                    continue;
                }
                String file = m.group(1);
                String fullpath = getFullPath(filenames, file);
                if (fullpath == null)
                    continue;
                // lines are 0-indexed in thread objects.
                int line = m.groupCount() == 1 ? 0 : Integer.parseInt(m.group(2)) - 1;
                CodeReviewThread thread = new CodeReviewThread(conn, submission.getSubmissionPK(), fullpath, line, now,
                        findbugsReviewer.getCodeReviewerPK());

                String commentText = rs.getString("long_test_result");
                CodeReviewComment c = new CodeReviewComment(thread, findbugsReviewer, commentText, now, false, conn);
                count++;
                System.out.println("Added comment " + c.getCodeReviewCommentPK());
            } while (rs.next());
            findbugsReviewer.addComments(conn, count, now);
        } catch (Exception e) {
            assert true;
        } finally {
            Queries.closeStatement(findbugsOutcomes);
        }

    }
	
	@Override
	public int compareTo(CodeReviewSummary that) {
		if (this == that)
			return 0;
		int result = Objects.compareTo(this.getTimestamp(), that.getTimestamp());
		if (result != 0)
			return result;
		return Objects.compareTo(viewerAsReviewer.getCodeReviewerPK(), that.viewerAsReviewer.getCodeReviewerPK());
	}

	@Override
	public int hashCode() {
		return viewerAsReviewer.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof CodeReviewSummary) {
			CodeReviewSummary that = (CodeReviewSummary) o;
			return this.viewerAsReviewer.equals(that.viewerAsReviewer);
		}
		return false;
	}


}
