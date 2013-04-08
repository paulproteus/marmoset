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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import sun.nio.cs.ext.ISCII91;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import edu.umd.cs.marmoset.utilities.Objects;
import edu.umd.cs.marmoset.utilities.TextUtilities;

/** Provide a summary of a code review */
public class CodeReviewSummary  implements Comparable<CodeReviewSummary>{
	public static class Info {
		@Nonnull
		public final Submission submission;
		@Nonnull
		public final Project project;
		
		@Nonnull 
		public final Course course;
		@CheckForNull
		public final CodeReviewer author;
		@CheckForNull
		public final CodeReviewAssignment assignment;
		/** map from rubricPK to Rubric */
		public final Map<Integer, Rubric> rubrics = new HashMap<Integer, Rubric>();
		public final Collection<RubricEvaluation> allRubricEvaluations;
		public final Collection<CodeReviewComment> allComments;
		public final Collection<CodeReviewThread> allThreads;
		public final boolean isRequestForHelp;
		/**
		 * Map from codeReviewerPK to CodeReviewer
		 */
		public final Map<Integer, CodeReviewer> reviewers = new HashMap<Integer, CodeReviewer>();

		public Info(Connection conn, Submission submission, @CheckForNull CodeReviewAssignment assignment) throws SQLException {
			if (submission == null)
				throw new NullPointerException("No submission");
			if (assignment != null && assignment.getProjectPK() != submission.getProjectPK())
				throw new IllegalArgumentException();
			this.submission = submission;
			this.project = Project.getByProjectPK(submission.getProjectPK(), conn);
			this.course = Course.getByCoursePK(project.getCoursePK(), conn);
			this.author = CodeReviewer.lookupAuthorBySubmission(
					submission.getSubmissionPK(), conn);

			this.assignment = assignment;
			this.isRequestForHelp = submission.isHelpRequested(conn);
			
			if (!hasFindbugsReviewer(conn)) {
				createFindbugsThreads(conn, submission);
			}
			
			for(CodeReviewer r : CodeReviewer.lookupBySubmissionPK(submission.getSubmissionPK(), conn)) {
				this.reviewers.put(r.getCodeReviewerPK(), r);
			}
		    this.allThreads = CodeReviewThread.lookupBySubmissionPK(submission.getSubmissionPK(), conn);
		    this.allComments = CodeReviewComment.lookupBySubmissionPK(submission.getSubmissionPK(), conn);
		    this.allRubricEvaluations = RubricEvaluation.lookupBySubmissionPK(submission.getSubmissionPK(), conn);
		    if (this.assignment != null)  {
				for(Rubric r : Rubric.lookupByAssignment(this.assignment.getCodeReviewAssignmentPK(), conn)) 
				    this.rubrics.put(r.getRubricPK(), r);
			}
		    for(RubricEvaluation eval : this.allRubricEvaluations) 
		    		if (!this.rubrics.containsKey(eval.getRubricPK())) {
		    			Rubric r = Rubric.lookupByPK(eval.getRubricPK(), conn);
		    			this.rubrics.put(r.getRubricPK(), r);
		    }


		}
	    private boolean hasFindbugsReviewer(Connection conn) throws SQLException {
	        PreparedStatement st = conn.prepareStatement("SELECT * FROM code_reviewer " + "WHERE is_automated = 1 AND known_as = ? "
	                + "AND submission_pk = ?");
	        try {
	            Queries.setStatement(st, "FindBugs", this.submission.getSubmissionPK());
	            ResultSet rs = st.executeQuery();
	            return rs.next();
	        } finally {
	            st.close();
	        }
	    }

	    /** 
		 * Create threads for all the FindBugs warnings for the submission.
		 * 
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
		
	    private CodeReviewer createFindbugsReviewer(Connection conn) throws SQLException {
	        CodeReviewer.Builder builder = new CodeReviewer.Builder(conn, this.submission.getSubmissionPK());
	        builder.setAutomated("FindBugs");
	        return builder.build();
	    }
		
	    
	}
	private static final String FINDBUGS_TEST_TYPE = "findbugs";

  public enum Status { NOT_STARTED, DRAFT, PUBLISHED, INTERACTIVE };

	 final @Nonnull Info info;
	/* Per viewer information */
	final @Nonnull
	Student viewerAsStudent;
	final @Nonnull
	CodeReviewer viewerAsReviewer;
	boolean anyUnpublishedDraftsByViewer;
	boolean anyPublishedCommentsByViewer;
	Map<Integer, Rubric> rubricsUnevaluatedByViewer = new HashMap<Integer, Rubric>();
	
	/**
	 * Map from threadPK to CodeReviewThread
	 */
	final Map<Integer, CodeReviewThread> threads = new HashMap<Integer, CodeReviewThread>();
	/**
	 * Map from threadPK to sorted set of comments
	 */
	final Map<Integer, NavigableSet<CodeReviewComment>> comments = new HashMap<Integer, NavigableSet<CodeReviewComment>>();

	final Set<CodeReviewComment> allVisibleComments = new TreeSet<CodeReviewComment>();

	/** Map from threadPK to RubricEvaluation */
	Map<Integer, RubricEvaluation> rubricEvaluations = new HashMap<Integer, RubricEvaluation>();
		
	public CodeReviewSummary(Connection conn, CodeReviewer reviewer) throws SQLException {
		this(conn, reviewer.getSubmission(), reviewer.getStudent(), reviewer);
	}
	private CodeReviewSummary(Connection conn, Submission submission, Student student, CodeReviewer reviewer) throws SQLException {
		this(new Info(conn, submission, reviewer.getCodeReviewAssignment()), student, reviewer);
	}
	public CodeReviewSummary(Info info,   CodeReviewer reviewer) throws SQLException {
		 this(info, reviewer.getStudent(), reviewer);
	}
	public CodeReviewSummary(Info info,  Student student, CodeReviewer reviewer) throws SQLException {
        if (reviewer == null)
      	  throw new NullPointerException("No code reviewer");
	    if (student == null)
            throw new NullPointerException("No student " + reviewer.getStudentPK() 
                    + " for code reviewer " + reviewer.getCodeReviewerPK());

		if (reviewer.getStudentPK() != student.getStudentPK())
			throw new IllegalArgumentException("PK for student of "
					+ student.getStudentPK()
					+ " doesn't match PK for reviewer of "
					+ reviewer.getStudentPK());

		if (reviewer.getSubmissionPK() != info.submission.getSubmissionPK())
			throw new IllegalArgumentException();
		if (info.assignment != null
				&& info.assignment.getCodeReviewAssignmentPK() != reviewer
						.getCodeReviewAssignmentPK())
			throw new IllegalArgumentException();
	 	   
        this.info = info;
        this.viewerAsStudent = student;
		this.viewerAsReviewer = reviewer;
		
		if (info.project == null)
		    throw new NullPointerException("No project " + info.submission.getProjectPK() 
                    + " for submission" + info.submission.getProjectPK());
		
		
		for(CodeReviewThread t : info.allThreads) 
	        if (isVisible(t)) {
	          threads.put(t.getCodeReviewThreadPK(), t);
	      }

		for(CodeReviewComment c : info.allComments) {
			int threadPK = c.getCodeReviewThreadPK();
			if (!threads.containsKey(threadPK) || !isVisible(c))
				continue;
			NavigableSet<CodeReviewComment> cs = comments.get(threadPK);
			if (cs == null) {
				cs = new TreeSet<CodeReviewComment>();
				comments.put(threadPK, cs);
			}
			cs.add(c);
			allVisibleComments.add(c);
			if (isAuthor(c)) {
			if (c.isDraft())
				anyUnpublishedDraftsByViewer = true;
			else
			    anyPublishedCommentsByViewer = true;
			}
		}
		
		Set<Integer> threadsWithContent = new HashSet<Integer>(comments.keySet());
			        
		if (!isReviewerIsTheAuthor() && info.assignment != null) {
			for(Rubric r : info.rubrics.values())
				if (r.getCodeReviewAssignmentPK() == info.assignment.getCodeReviewAssignmentPK())
					rubricsUnevaluatedByViewer.put(r.getRubricPK(), r);
		}
	
        for (RubricEvaluation e : info.allRubricEvaluations)
            if (isVisible(e)) {
                int threadPK = e.getCodeReviewThreadPK();
                assert threads.containsKey(threadPK);
                NavigableSet<CodeReviewComment> cs = comments.get(threadPK);
                if (cs == null) {
                    cs = new TreeSet<CodeReviewComment>();
                    comments.put(threadPK, cs);
                }
                rubricEvaluations.put(threadPK, e);

                if (e.getCodeReviewerPK() == reviewer.getCodeReviewerPK()) {
                    if (e.getStatus().equals("LIVE"))
                        rubricsUnevaluatedByViewer.remove(e.getRubricPK());
                    else if (e.getStatus().equals("NEW") || e.getStatus().equals("DRAFT"))
                        anyUnpublishedDraftsByViewer = true;
                }
            }
		threadsWithContent.addAll(rubricEvaluations.keySet());
		threads.keySet().retainAll(threadsWithContent);
		for(CodeReviewThread t : threads.values()) {
            assert isVisible(t);
            assert hasContent(t);
		}  
	}

	public Info getInfo() {
		return info;
	}
	public boolean isReviewerIsTheAuthor() {
	    return info.author != null && info.author.equals(viewerAsReviewer);
	}
	private boolean isDebug() {
		return false;
	}

	public boolean isAllReviewersRatedByAuthor() {
		for(CodeReviewer r : info.reviewers.values()) {
			if (r.isAuthor() || r.isAutomated()) continue;
			if (r.getRating() != 0) continue;
			for(CodeReviewComment c : allVisibleComments)
					if (c.getCodeReviewerPK() == r.getCodeReviewerPK()) continue;

		        for (RubricEvaluation e : rubricEvaluations.values())
		        		if (e.getCodeReviewerPK() == r.getCodeReviewerPK()) continue;
				return false;
		}
		return true;
	}
	
	public Project getProject() {
		return info.project;
	}
	
	public Course getCourse() {
	    return info.course;
	}

	public Student getViewer() {
		return viewerAsStudent;
	}
	public CodeReviewer getAuthor() {
		return info.author;
	}

	public Submission getSubmission() {
		return info.submission;
	}

	public @Submission.PK int getSubmissionPK() {
		return info.submission.getSubmissionPK();
	}

	public String getDescription() {
		if (info.assignment == null)
			return "ad-hoc";
		return info.assignment.getDescription();
	}

	public CodeReviewAssignment getAssignment() {
		return info.assignment;
	}

	public CodeReviewer getCodeReviewer() {
		return viewerAsReviewer;
	}
	public @CodeReviewer.PK int getCodeReviewerPK() {
		return viewerAsReviewer.getCodeReviewerPK();
	}

	public boolean getAnyUnpublishedDraftsByViewer() {
		return anyUnpublishedDraftsByViewer;
	}
	public boolean isNeedsPublishToRequestHelp() {
	    boolean result = !viewerAsReviewer.isInstructor()
	            && viewerIsAuthor()
	            && !isActive()
	            && !isRequestForHelp();
        return result;
    }
	public boolean isActive() {
	    Set<Integer> reviewers = getCodeReviewerMap().keySet();
	    return isReviewerIsTheAuthor() && anyPublishedCommentsByViewer
	            ||
	          reviewers.size() > 1;
	}
	public boolean isAnyPublishedCommentsByViewer() {
		return anyPublishedCommentsByViewer;
	}
	public boolean isRequestForHelp() {
	    return info.isRequestForHelp;
	}

	public boolean isTimely() {
	    if (viewerAsStudent == null)
	        return false;
		if (isNeedsResponse())
			return true;
		if (getAnyUnpublishedDraftsByViewer())
			return true;
		Date now = new Date();
		if (info.assignment != null && (info.author == null || viewerAsStudent.getStudentPK() != info.author.getStudentPK())) {
			if (info.assignment.getDeadline().after(now))
				return true;
			if (getNumCommentsByViewer() == 0)
				return true;
		}
		return false;
	}

	public Map<Integer,CodeReviewer> getCodeReviewerMap() {
		return Collections.unmodifiableMap(info.reviewers);
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
	
	public Status getStatus() {
	    
		if (allVisibleComments.isEmpty() && rubricEvaluations.isEmpty())
	        return Status.NOT_STARTED;
	
	    boolean published = false;
	    boolean byAuthor = false;
	    boolean byReviewer = false;
	    
	   for(CodeReviewComment c : allVisibleComments) {
	        if (!c.isDraft()) published = true;
	        if (info.author != null && c.getCodeReviewerPK() == info.author.getCodeReviewerPK())
	            byAuthor = true;
	        else
	            byReviewer = true;
	   }
	   for(RubricEvaluation r : rubricEvaluations.values()) 
	       if (!r.isDraft()) {
	           published = true;
	           byReviewer = true;
	       }
	   if (!published)
	       return Status.DRAFT;
	   if (byAuthor && byReviewer)
	       return Status.INTERACTIVE;
	   return Status.PUBLISHED;
	    
	}

	private CodeReviewer getAuthor(CodeReviewComment c) {
		return info.reviewers.get(c.getCodeReviewerPK());
	}

	public @CheckForNull CodeReviewComment lastNonDraftComment(NavigableSet<CodeReviewComment> comments) {
	       
		 for (CodeReviewComment c : comments.descendingSet()) 
	            if (!c.isDraft() && isVisible(c))
	                return c;
		 return null;
	}
	
	public boolean isTimely(CodeReviewComment c) {
		if (info.assignment == null) 
			return true;
		Timestamp deadline = info.assignment.getDeadline();
		if (c.isBy(info.author))
			deadline = new Timestamp(deadline.getTime() + TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));
			
		return c.getModified().compareTo(deadline) < 0;
	}
	public boolean isNeedsResponse(CodeReviewThread t) {
        NavigableSet<CodeReviewComment> comments = getComments(t);
        if (comments.isEmpty()) {
            if (t.getRubricEvaluationPK() == 0)
                throw new IllegalStateException("thread " + t.getCodeReviewThreadPK() + " is empty");
            return viewerIsAuthor();
        }
        CodeReviewComment last = lastNonDraftComment(comments);
        
		if (last == null)
			return false;
		if (last.isAck())
			return false;
		if (last.isBy(viewerAsReviewer))
			return false;
		if (!isTimely(last))
			return false;
		if (viewerIsAuthor())
			return true;
		// viewer is code reviewer. Needs to respond if comment is from
		// author and thread was either started by reviewer or by
		// author and review is instructor
		if (last.isBy(info.author)) {
			@CodeReviewer.PK
			int startedBy = t.getCreatedBy();
			if (startedBy == viewerAsReviewer.getCodeReviewerPK())
				return true;
			if (startedBy == info.author.getCodeReviewerPK()
					&& viewerAsReviewer.isInstructor())
				return true;
		}
		return false;
    }
    /**
     * @return
     */
    public boolean viewerIsAuthor() {
        return info.author != null && viewerAsReviewer.getCodeReviewerPK() == info.author.getCodeReviewerPK();
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
		for(CodeReviewComment c : allVisibleComments)
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
		for(CodeReviewComment c : allVisibleComments)
			if (byViewer(c) && !c.isJustAck() && !c.isDraft())
                count++;
        for (RubricEvaluation e : rubricEvaluations.values())
            if (e.isBy(viewerAsReviewer) && !e.isDraft())
                count++;
        return count;
	}
	public int getNumCommentsByOthers() {
		int count = 0;
		for(CodeReviewComment c : allVisibleComments)
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
        if (info.assignment == null)
            return true;
        CodeReviewer commentAuthor = info.reviewers.get(codeReviewerPK);
        return commentAuthor.getCodeReviewAssignmentPK() == info.assignment.getCodeReviewAssignmentPK();
    }
    
 
    
    private boolean canSeeCommentsBy(@CodeReviewer.PK int codeReviewerPK) {
        if (codeReviewerPK == viewerAsReviewer.getCodeReviewerPK())
            return true;
        if (viewerAsReviewer.isOmniscient())
                 return true;
        if (info.author != null &&  info.author.getStudentPK() == viewerAsStudent.getStudentPK())
                return true;
        if (info.assignment != null && info.assignment.isOtherReviewsVisible() && matchingAssignment(codeReviewerPK))
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
		return info.reviewers.values();
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
		return rubricsUnevaluatedByViewer.values();
	}

	@CheckForNull
    public RubricEvaluation getRubricForThread(CodeReviewThread t) {
		return rubricEvaluations.get(t.getCodeReviewThreadPK());
	}
	public Rubric getRubric(@Rubric.PK int rubricPK) {
		return info.rubrics.get(rubricPK);
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
