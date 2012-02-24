package edu.umd.cs.marmoset.review;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import edu.umd.cs.marmoset.modelClasses.CodeReviewComment;
import edu.umd.cs.marmoset.modelClasses.CodeReviewSummary;
import edu.umd.cs.marmoset.modelClasses.CodeReviewThread;
import edu.umd.cs.marmoset.modelClasses.CodeReviewer;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Rubric;
import edu.umd.cs.marmoset.modelClasses.RubricEvaluation;
import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.EditDistance;
import edu.umd.cs.marmoset.utilities.TextUtilities;
import edu.umd.cs.submitServer.SubmitServerDatabaseProperties;
import edu.umd.review.gwt.rpc.dto.CheckboxEvaluationDto;
import edu.umd.review.gwt.rpc.dto.CheckboxRubricDto;
import edu.umd.review.gwt.rpc.dto.CommentDto;
import edu.umd.review.gwt.rpc.dto.DropdownEvaluationDto;
import edu.umd.review.gwt.rpc.dto.DropdownRubricDto;
import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.rpc.dto.NumericEvaluationDto;
import edu.umd.review.gwt.rpc.dto.NumericRubricDto;
import edu.umd.review.gwt.rpc.dto.ReviewerDto;
import edu.umd.review.gwt.rpc.dto.RubricDto;
import edu.umd.review.gwt.rpc.dto.RubricDto.Presentation;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto;
import edu.umd.review.gwt.rpc.dto.RubricEvaluationDto.Status;
import edu.umd.review.gwt.rpc.dto.ThreadDto;
import edu.umd.review.server.dao.ReviewDao;

public class MarmosetDaoService implements ReviewDao {

  final CodeReviewer reviewer;

  final Submission submission;
  final Student student;

  final String nameOfReviewer;
  final boolean isAuthor;
  final Project project;
  CodeReviewer codeAuthor;
  final SubmitServerDatabaseProperties database;
  final ReviewerDto reviewerDto;
  final Map<String, List<String>> text;
  final Map<String, FileDto> files = new TreeMap<String, FileDto>();
  boolean requestReviewOnPublish;

  /** Caches some information about code review. Nulled out whenever we get a new database connection */
  @CheckForNull CodeReviewSummary summary;

  public String toString() {
      return String.format("MarmosetDaoService, code reviewer %d", reviewer.getCodeReviewerPK());

  }

    public @CodeReviewer.PK
    int getCodeReviewerPK() {
        return reviewer.getCodeReviewerPK();
    }

    public @Submission.PK
    int getSubmissionPK() {
        return submission.getSubmissionPK();
    }

    public @Project.PK
    int getProjectPK() {
        return submission.getProjectPK();
    }

  public static class Builder {
    private final SubmitServerDatabaseProperties database;

    public Builder(SubmitServerDatabaseProperties database) {
      this.database = database;
    }

    public MarmosetDaoService buildAssigned(@CodeReviewer.PK int reviewerPK) {
      Connection conn = null;
      try {
        conn = database.getConnection();
        CodeReviewer reviewer = CodeReviewer.lookupByPK(reviewerPK, conn);
        return new MarmosetDaoService(database, reviewer, reviewer.getSubmission());
      } catch (SQLException e) {
        throw new RuntimeException(e);
      } finally {
        release(conn);
      }
    }

    private void release(Connection conn) {
      try {
        database.releaseConnection(conn);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private MarmosetDaoService(SubmitServerDatabaseProperties database, CodeReviewer reviewer, Submission submission) {
    this.database = database;
    this.reviewer = reviewer;
    this.submission = submission;
    Connection conn = null;
    try {
      conn = getConnection();
      this.student = reviewer.getStudent();
      nameOfReviewer = reviewer.getName();
      isAuthor = reviewer.isAuthor();
      CodeReviewSummary summary = getSummary();
      this.requestReviewOnPublish = summary.isRequestReviewOnPublish();
      this.project = summary.getProject();

      this.codeAuthor = summary.getAuthor();
     
      if (isAuthor || codeAuthor == null || reviewer.isInstructor()) {
          StudentRegistration authorAsStudent = StudentRegistration.lookupByStudentRegistrationPK(submission.getStudentRegistrationPK(), conn);
          authorName=  authorAsStudent.getFullname();
      } else
          authorName =  codeAuthor.getName();    

      byte[] archive = submission.downloadArchive(conn);
      text = TextUtilities.scanTextFilesInZip(archive);
      Map<String, BitSet> changed = project.computeDiff(conn, submission, text);
      for (Map.Entry<String, List<String>> e : text.entrySet()) {
        String path = e.getKey();
        List<String> contents = e.getValue();
        int[] bitsSet;
        if (changed == null)
          bitsSet = bitsSet(null, contents);
        else {
          BitSet bitSet = changed.get(path);
          if (bitSet != null && bitSet.cardinality() == 0 && !path.endsWith("StudentTests.java"))
            continue;
          bitsSet = bitsSet(bitSet, contents);
        }
        FileDto file = new FileDto(0, path, contents);
        file.setModifiedLines(bitsSet);
        files.put(path, file);
      }

      reviewerDto = new ReviewerDto(this.nameOfReviewer,
                                    reviewer.getCodeReviewerPK(),
                                    reviewer.getSubmissionPK(),
                                    "review-dao-" + reviewer.getCodeReviewerPK());
      reviewerDto.setAuthor(isAuthor);

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }
  }


  private final String authorName;
  public MarmosetDaoService(SubmitServerDatabaseProperties database, CodeReviewer thisReviewer) {

    this.database = database;
    Connection conn = null;
    try {
      conn = getConnection();
     
      this.reviewer = thisReviewer;
      this.submission = reviewer.getSubmission();
      this.student = reviewer.getStudent();
      nameOfReviewer = reviewer.getName();
      isAuthor = reviewer.isAuthor();
      CodeReviewSummary summary = getSummary();
      this.project = summary.getProject();
      this.requestReviewOnPublish = summary.isRequestReviewOnPublish();
      
      this.codeAuthor = summary.getAuthor();


      if (isAuthor || codeAuthor == null || reviewer.isInstructor()) {
          StudentRegistration authorAsStudent = StudentRegistration.lookupByStudentRegistrationPK(submission.getStudentRegistrationPK(), conn);
          authorName=  authorAsStudent.getFullname();
      } else
          authorName =  codeAuthor.getName();


      byte[] archive = submission.downloadArchive(conn);
      text = TextUtilities
              .scanTextFilesInZip(archive);
      Map<String, BitSet> changed = project.computeDiff(conn, submission, text);
      for (Map.Entry<String, List<String>> e : text.entrySet()) {
        String path = e.getKey();
        List<String> contents = e.getValue();
        int[] modifiedLines;
        int[] linesToShow;
        if (changed == null) {
          modifiedLines = bitsSet(null, contents);
          linesToShow = bitsSet(null, contents);
        } else {
          BitSet modifiedLinesBits = changed.get(path);
          if (modifiedLinesBits != null && modifiedLinesBits.cardinality() == 0
              && !path.endsWith("StudentTests.java"))
            continue;
          modifiedLines = bitsSet(modifiedLinesBits, contents);
          BitSet showBits = EditDistance.showLines(modifiedLinesBits, contents.size());
          showBits.or(getLinesWithThreads(path, contents.size()));
          linesToShow = bitsSet(showBits, contents);
        }
        FileDto file = new FileDto(0, path, contents);
        file.setModifiedLines(modifiedLines);
        file.setLinesToShow(linesToShow);
        if (linesToShow == null || linesToShow.length == 0) {
          continue;
        }
        files.put(path, file);
      }


      reviewerDto = new ReviewerDto(this.nameOfReviewer,
                                    reviewer.getCodeReviewerPK(),
                                    reviewer.getSubmissionPK(),
                                    "review-dao-" + reviewer.getCodeReviewerPK());
      reviewerDto.setAuthor(isAuthor);

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }

  }

  public String getAuthorName() {
      return authorName;
  }
   
  /**
   * Return a BitSet with one bit for every line in a file, where a set bit indicates a thread is
   * present on that line.
   */
  private BitSet getLinesWithThreads(@Nonnull String path, int lines) {
    BitSet withThreads = new BitSet(lines);
    CodeReviewSummary summary = getSummary();
    for (CodeReviewThread thread : summary.getThreadMap().values()) {
      if (path.equals(thread.getFile())) {
        withThreads.set(thread.getLine());
      }
    }
    return withThreads;
  }

  protected CodeReviewer getCodeReviewer() {
    return reviewer;
  }
  protected Connection getConnection() throws SQLException {
    this.summary = null;
    return database.getConnection();
  }

  protected void release(Connection conn) {
    try {
      database.releaseConnection(conn);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public TreeSet<RubricDto> getUnusedRubrics() {
    TreeSet<RubricDto> unassignedRubrics = new TreeSet<RubricDto>();
    for (Rubric r : getSummary().getUnassignedRubrics())
      unassignedRubrics.add(makeRubricDto(r));
    return unassignedRubrics;
  }

  private @Nonnull CodeReviewSummary getSummary() {
    if (summary != null)
      return summary;
    Connection conn = null;
    try {
      conn = getConnection();

      CodeReviewSummary s = new CodeReviewSummary(conn, reviewer);
      summary = s;
      return s;
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }
  }

  @Override
  public Collection<? extends ThreadDto> getGeneralCommentThreads() {
      CodeReviewSummary summary = getSummary();
      TreeSet<ThreadDto> result = new TreeSet<ThreadDto>();
      for (CodeReviewThread t : summary.getThreadMap().values()) {
          String path = t.getFile();
          if (path == null) 
              result.add(getThreadDto(t));
      }
      return result;
  }
      
   private ThreadDto getThreadDto(CodeReviewThread t) {
      
        CodeReviewSummary summary = getSummary();

        int id = t.getCodeReviewThreadPK();
        ThreadDto thread = new ThreadDto(id, t.getFile(), t.getLine(),
                summary.isNeedsResponse(t));
        RubricEvaluation e = summary.getRubricForThread(t);
        if (e != null) {
            Rubric r = summary.getRubric(e.getRubricPK());
            if (r == null) {
                System.out.println("Could not find rubric " + e.getRubricPK()
                        + " for " + e.getRubricEvaluationPK());

            } else {
                RubricEvaluationDto tmp = makeRubricEvaluationDto(t, r, e);
                if (e.getCodeReviewerPK() == reviewer.getCodeReviewerPK()
                        && !e.getStatus().equals("DEAD")) {
                    tmp.setEditable(true);
                }
                thread.setRubricEvaluation(tmp);
            }
       }
       
       Map<Integer, CodeReviewer> reviewers = summary.getCodeReviewerMap();

       for (CodeReviewComment c : summary.getComments(t)) {
         CodeReviewer student = reviewers.get(c.getCodeReviewerPK());
         String name = student.getName();
         CommentDto comment = new CommentDto(c.getCodeReviewCommentPK(),
             id, name);
         comment.setContents(c.getComment());
         comment.setAcknowledgement(c.isAck());
         comment.setTimestamp(c.getModified().getTime());
         if (!c.isDraft()) {
           thread.addPublishedComment(comment);
         } else {
           assert wroteComment(c);
           if (thread.getDraft() != null)
             System.out
                 .printf(" multiple draft comments by %d on thread %d%n",
                     c.getCodeReviewerPK(),
                     c.getCodeReviewThreadPK());
           comment.setDraft(true);
           thread.setDraft(comment);

         }
       }
       return thread;
   }
  @Override
  public Collection<FileDto> getFiles() {

    CodeReviewSummary summary = getSummary();
    for (FileDto f : files.values())
      f.getThreads().clear();

     for (CodeReviewThread t : summary.getThreadMap().values()) {
      String path = t.getFile();
      if (path == null) 
          continue;
      FileDto file = files.get(path);
      if (file == null) {
        System.out.printf(" Cannot find file %s for thread %d%n", path,
            t.getCodeReviewThreadPK());
        continue;
      }
      ThreadDto thread = getThreadDto(t);
      file.addThread(thread);
    }

    return new ArrayList<FileDto>(files.values());
  }

  private boolean wroteComment(CodeReviewComment c) {
    return c.isBy(reviewer);
  }


  @Override
  public ThreadDto createThread(@CheckForNull String file, int line) {
    Connection conn = null;
    try {
      conn = getConnection();

      CodeReviewThread t = new CodeReviewThread(conn,
          submission.getSubmissionPK(), file, line, now(),
          getCodeReviewer().getCodeReviewerPK(), 0);
      ThreadDto threadDto = new ThreadDto(t.getCodeReviewThreadPK(),
          file, line, false);
      return threadDto;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }

  }

  @Override
  public ThreadDto createThreadWithRubric(@CheckForNull String file, int line, RubricDto rubricDto) {
    Connection conn = null;
    try {
      conn = getConnection();
      CodeReviewThread thread = new CodeReviewThread(conn,
                                                     submission.getSubmissionPK(),
                                                     file,
                                                     line,
                                                     now(),
                                                     getCodeReviewer().getCodeReviewerPK());

      Rubric rubric = Rubric.lookupByPK(rubricDto.getId(), conn);
      RubricEvaluation evaluation = new RubricEvaluation(conn,
                                                         rubric,
                                                         getCodeReviewer(),
                                                         thread);
      thread.setAndUpdateRubricEvaluationPK(conn, evaluation.getRubricEvaluationPK());

      ThreadDto threadDto = new ThreadDto(thread.getCodeReviewThreadPK(), file, line, false);
      RubricEvaluationDto rubricEvaluationDto = makeRubricEvaluationDto(thread, rubric, evaluation);
      threadDto.setRubricEvaluation(rubricEvaluationDto);
      return threadDto;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }
  }

  RubricDto makeRubricDto(Rubric rubric) {
    Presentation preso = Presentation.valueOf(rubric.getPresentation());
    Preconditions.checkNotNull(preso);
    switch (preso) {
    case DROPDOWN:
        return new DropdownRubricDto(rubric.getRubricPK(),
                                     rubric.getName(),
                                     rubric.getDescription(),
                                      rubric.getDataAsMap());
      case CHECKBOX:
        return new CheckboxRubricDto(rubric.getRubricPK(),
                                     rubric.getName(),
                                     rubric.getDescription(),
                                     rubric.getDataAsMap());
      case NUMERIC:
        return new NumericRubricDto(rubric.getRubricPK(),
                                    rubric.getName(),
                                    rubric.getDescription(),
                                    rubric.getDataAsMap());
      default:
        throw new IllegalArgumentException("Invalid rubric presentation: " + preso);
    }
  }

  private RubricEvaluationDto makeRubricEvaluationDto(CodeReviewThread thread,
                                                            Rubric rubric,
                                                            RubricEvaluation evaluation) {
    Presentation preso = Presentation.valueOf(rubric.getPresentation());
    Status status = Status.valueOf(evaluation.getStatus());
    Preconditions.checkNotNull(preso);
    Preconditions.checkNotNull(status);
    RubricDto rubricDto = makeRubricDto(rubric);
    int rubricEvaluationPK = evaluation.getRubricEvaluationPK();
    int threadPK = thread.getCodeReviewThreadPK();
    boolean canEdit = evaluation.getCodeReviewerPK() == reviewer.getCodeReviewerPK();
    CodeReviewer evaluationAuthor = getSummary().getCodeReviewerMap().get(evaluation.getCodeReviewerPK());
    String authorName = evaluationAuthor != null ? evaluationAuthor.getName() : "?";
    RubricEvaluationDto dto;
    switch (preso) {
      case DROPDOWN:
        dto = new DropdownEvaluationDto((DropdownRubricDto) rubricDto,
                                        rubricEvaluationPK,
                                        evaluation.getCodeReviewerPK(), authorName,
                                        threadPK,
                                        canEdit, status);
        break;
      case NUMERIC:
        dto = new NumericEvaluationDto((NumericRubricDto) rubricDto,
                                       rubricEvaluationPK,
                                       evaluation.getCodeReviewerPK(), authorName,
                                       threadPK,
                                       canEdit, status);
        break;
      case CHECKBOX:
        dto = new CheckboxEvaluationDto((CheckboxRubricDto) rubricDto,
                                        rubricEvaluationPK,
                                        evaluation.getCodeReviewerPK(),authorName,
                                        threadPK,
                                        canEdit, status);
        break;
      default:
        throw new IllegalArgumentException("Invalid rubric presentation: " + preso);
    }
    String value = evaluation.getValue();
    if (value != null && !value.isEmpty()) {
      dto.setValue(value);
    }
    if (evaluation.getExplanation() != null) {
      dto.setExplanation(evaluation.getExplanation());
    }
    return dto;
  }

  private static Timestamp now() {
    return new Timestamp(System.currentTimeMillis());
  }

  @Override
  public void deleteEmptyThread(@CodeReviewThread.PK int threadId) {
    Connection conn = null;

    try {
      conn = getConnection();
      CodeReviewThread.delete(threadId, conn);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }

  }

  @Override
  public CommentDto createDraft(@CodeReviewThread.PK int threadId, boolean ack) {
    Connection conn = null;
    Timestamp now = now();

    try {
      conn = getConnection();
      CodeReviewComment comment = new CodeReviewComment(threadId,
          getCodeReviewer().getCodeReviewerPK(), "", true, ack, now, conn);
      CommentDto commentDto = new CommentDto(
          comment.getCodeReviewCommentPK(), threadId, nameOfReviewer);
      commentDto.setDraft(true);
      commentDto.setAcknowledgement(ack);
      commentDto.setTimestamp(now.getTime());
      return commentDto;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }

  }

  @Override
  public void saveDraft(CommentDto draft) {
    Connection conn = null;
    Timestamp now = now();
    try {
      conn = getConnection();
      CodeReviewComment.update(draft.getId(), draft.getContents(),
          draft.isAcknowledgement(), now, conn);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }
  }

  @Override
  public void discardDraft(CommentDto draft) {
    Connection conn = null;

    try {
      conn = getConnection();

      CodeReviewComment.delete(draft.getId(), conn);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }
  }

  private static @Nonnull
  int[] bitsSet(BitSet bits, List<String> contents) {
    int sz = contents.size();
    if (bits == null) {
      int[] result = new int[sz];
      for (int i = 0; i < sz; i++)
        result[i] = i;
      return result;
    }
    int[] result = new int[bits.cardinality()];

    int count = 0;
    for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
      assert i < sz;
      result[count++] = i;
    }
    assert count == result.length;
    return result;
  }

  @Override
  public Map<String, TreeSet<ThreadDto>> getThreadsWithDrafts() {
    Collection<FileDto> files = getFiles();
    Map<String, TreeSet<ThreadDto>> result = new TreeMap<String, TreeSet<ThreadDto>>();
    for (FileDto f : files) {
      TreeSet<ThreadDto> threadsWithDrafts = new TreeSet<ThreadDto>();
      for (ThreadDto t : f.getThreads()) {
        RubricEvaluationDto e = t.getRubricEvaluation();
        if (t.getDraft() != null ||
            (e != null && (e.getStatus() == Status.DRAFT || e.getStatus() == Status.NEW))) {
          threadsWithDrafts.add(t);
        }
      }
      if (!threadsWithDrafts.isEmpty())
        result.put(f.getPath(), threadsWithDrafts);
    }
    for (ThreadDto thread : getGeneralCommentThreads()) {
      if (thread.getDraft() == null) {
        continue;
      }
      TreeSet<ThreadDto> ts = result.get("");
      if (ts == null) {
        ts = Sets.newTreeSet();
      }
      ts.add(thread);
      result.put("", ts);
    }
    return result;
  }

  @Override
  public void publishDrafts(Collection<Integer> commentIds, Collection<Integer> evaluationIds) {
    if (commentIds.isEmpty() && evaluationIds.isEmpty())
      return;

    Connection conn = null;
    Timestamp now = now();

    try {
      conn = getConnection();

      for (Integer id : commentIds) {
        CodeReviewComment.publish(id, now, conn);
      }
      for (Integer id : evaluationIds) {
        RubricEvaluation.publish(RubricEvaluation.asPK(id), now, conn);
      }

      reviewer.addComments(conn, commentIds.size(), now);
      didPublish(conn);

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }

  }

  private void didPublish(Connection conn) throws SQLException {
      if (codeAuthor == null) {
          this.codeAuthor = CodeReviewer.lookupOrInsertAuthor(conn, submission, reviewer.getCodeReviewAssignment(), "");
        }
        if (requestReviewOnPublish) {
            submission.markReviewRequest(conn, project);
            requestReviewOnPublish = false;
        }
  }
  @Override
  public void publishAllDrafts() {
    Connection conn = null;

    Timestamp now = now();
    try {
      conn = getConnection();
      int count = CodeReviewComment.publishAll(reviewer, now, conn);
      reviewer.addComments(conn, count, now);
      RubricEvaluation.publishAll(reviewer, now, conn);
     didPublish(conn);

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }
  }


    @Override
  public ReviewerDto getReviewer() {
      return reviewerDto;
  }

  @Override
  public void updateRubricScore(RubricEvaluationDto evaluation) {
    if (evaluation.getAuthorPK() != reviewer.getCodeReviewerPK()) {
      throw new IllegalArgumentException("you do not own this evaluation");
    }
    Connection conn = null;
    try {
      conn = getConnection();
      RubricEvaluation.update(conn,
                              evaluation.getRubricEvaluationPK(),
                              evaluation.getExplanation(),
                              evaluation.getPoints(),
                              evaluation.getValue(),
                              evaluation.getStatus().name());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }
  }

  @Override
  public void deleteRubricScore(RubricEvaluationDto evaluation) {
    if (evaluation.getAuthorPK() != reviewer.getCodeReviewerPK()) {
      throw new IllegalArgumentException("you do not own this evaluation");
    }
    Connection conn = null;
    try {
      conn = getConnection();
      RubricEvaluation.delete(conn, evaluation.getRubricEvaluationPK());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      release(conn);
    }
  }
}
