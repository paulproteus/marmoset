package edu.umd.review.gwt.rpc.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for a single file. Files are uniquely identified by their path.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 */
public class FileDto implements IsSerializable, Comparable<FileDto> {
  private int fileId;
  private String path;
  private List<String> lines;
  private int[] modifiedLines;
  private TreeSet<Integer> modifiedSet = Sets.newTreeSet();
  private int[] linesToShow;
  private TreeSet<ThreadDto> threads = Sets.newTreeSet();
  private TreeSet<Integer> linesWithThreads = Sets.newTreeSet();

  private transient TreeSet<LineRange> ranges = null;

  /**@deprecated GWT only. */
  @SuppressWarnings("unused")
  @Deprecated
  private FileDto() {
    this.path = "__BAD_PATH__";
    this.lines = Collections.emptyList();
  }

  public FileDto(int fileId, String path, List<String> lines) {
    this.fileId = fileId;
    this.path = path;
    this.lines = lines;
  }

  public int getFileId() {
    return fileId;
  }

  public List<String> getLines() {
    return lines;
  }

  public int[] getModifiedLines() {
    return modifiedLines;
  }

  public Iterable<LineRange> getRanges() {
    if (ranges == null) {
      ranges = Sets.newTreeSet();
      Set<Integer> modified = Sets.newTreeSet();
      for (int m : getModifiedLines()) {
        modified.add(m);
      }
      LineRange currentRange = new LineRange(0, modified.contains(0));
      for (int line = 0; line < lines.size(); line++) {
        boolean lineIsModified = modified.contains(line) || hasThreadOnLine(line);
        if (currentRange.isModified() != lineIsModified) {
          ranges.add(currentRange);
          currentRange = new LineRange(line, lineIsModified);
        }
        currentRange.addLine(lines.get(line));
      }
      ranges.add(currentRange);
    }
    return ranges;
  }

  public void setModifiedLines(int[] modifiedLines) {
    this.modifiedLines = modifiedLines;
    for (int line : modifiedLines) {
      modifiedSet.add(line);
    }
  }

  public boolean isModified(int line) {
    return modifiedSet.contains(line);
  }

  public void setLinesToShow(int[] linesToShow) {
    this.linesToShow = linesToShow;
  }

  public int[] getLinesToShow() {
    return linesToShow;
  }

  public String getPath() {
    return path;
  }

  public Collection<? extends ThreadDto> getThreads() {
    return threads;
  }

  public ThreadDto getThread(int id) {
    return null;
  }

  public boolean hasThreadOnLine(int line) {
    return linesWithThreads.contains(line);
  }

  public void addThread(ThreadDto thread) {
    this.threads.add(thread);
    this.linesWithThreads.add(thread.getLine());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FileDto)) {
      return false;
    }
    return this.path.equals(((FileDto) obj).path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

  @Override
  public int compareTo(FileDto that) {
    return this.path.compareTo(that.path);
  }

  @Override
  public String toString() {
    return "FILE " + path;
  }
}
