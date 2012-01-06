package edu.umd.review.gwt.rpc.dto;

import java.util.List;

import com.google.common.collect.Lists;


/** Represents a contiguous range of code which is either modified or unmodified. */
public class LineRange implements Comparable<LineRange> {
  private final int start;
  private final boolean modified;
  private final List<String> lines = Lists.newArrayList();

  public LineRange(int start, boolean modified) {
    this.start = start;
    this.modified = modified;
  }

  public void addLine(String line) {
    this.lines.add(line);
  }

  public boolean isModified() {
    return modified;
  }

  public int getStart() {
    return start;
  }

  public String firstLine() {
    return lines.get(0);
  }

  public String lastLine() {
    return lines.get(lines.size() - 1);
  }

  public List<String> getLines() {
    return lines;
  }

  public int size() {
    return lines.size();
  }

  @Override
  public int compareTo(LineRange o) {
    return this.start - o.start;
  }
}
