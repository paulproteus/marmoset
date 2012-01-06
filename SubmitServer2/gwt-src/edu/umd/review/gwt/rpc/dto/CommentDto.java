package edu.umd.review.gwt.rpc.dto;

import java.util.Date;

import net.customware.gwt.dispatch.shared.Result;

import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Longs;

/**
 * DTO for comments, whether published or drafts.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 */
public class CommentDto implements Result, Comparable<CommentDto> {
  private static final int SNIPPET_LENGTH = 20;
  private int id;
  private int threadId;
  private long timestamp;
  private String author;
  private boolean acknowledgement;
  private boolean draft;
  private String contents;

  /**@deprecated GWT only. */
  @SuppressWarnings("unused")
  @Deprecated
  private CommentDto() {
    this.id = 0xdeadbeef;
    this.threadId = 0xdeadbeef;
    this.author = "__NO_AUTHOR__";
  }

  public CommentDto(int id, int threadId, String author) {
    this.id = id;
    this.threadId = threadId;
    this.author = author;
    this.timestamp = new Date().getTime();
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public boolean isAcknowledgement() {
    return this.acknowledgement;
  }
  public boolean isJustAcknowledgement() {
      return this.acknowledgement && contents.equals("Acknowledged.");
  }


  public void setAcknowledgement(boolean ack) {
    this.acknowledgement = ack;
  }

  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  public int getId() {
    return id;
  }

  public int getThreadId() {
    return threadId;
  }

  public String getAuthor() {
    return author;
  }

  public void setDraft(boolean draft) {
    this.draft = draft;
  }

  public boolean isDraft() {
    return this.draft;
  }

  @Override
  public int hashCode() {
    return Longs.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CommentDto)) {
      return false;
    }
    return this.id == ((CommentDto) obj).id;
  }

  /** HACK alert. Fix once we fix the database; should be sorted by timestamp first */
  @Override
  public int compareTo(CommentDto that) {
    int result = ComparisonChain.start()
            .compare(this.id, that.id).compare(this.timestamp, that.timestamp)
        .compare(this.author, that.author)
        .result();
  return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (draft) {
      builder.append("DRAFT ");
    } else {
      builder.append("COMMENT ");
    }
    builder.append(this.id);
    return builder.toString();
  }

  public String getSnippet() {
    return getSnippet(SNIPPET_LENGTH);
  }

  public String getSnippet(int length) {
    int len = Math.min(this.contents.length(), length);
    if (len < contents.length()) {
      return contents.substring(0, len) + "...";
    } else {
      return contents;
    }
  }
}
