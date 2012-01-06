package edu.umd.review.gwt;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * Static utility methods for GWT client code.
 *
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public final class GwtUtils {
  private static final int TRIM_LENGTH = 20;
  private static final String DATE_FORMAT_STRING = "M/d/yyyy 'at' h:mm a";
  private static final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DATE_FORMAT_STRING);
  private static final NumberFormat snapshotIdFormat = NumberFormat.getFormat("0000000000");

  private GwtUtils() {
  }

  public static DateTimeFormat getDateTimeFormat() {
    return dateTimeFormat;
  }

  public static String formatSnapshotId(long id) {
    return snapshotIdFormat.format(id);
  }

  public static String formatTimestamp(long timestamp) {
    Date date = new Date(timestamp);
    return dateTimeFormat.format(date);
  }

  public static String trimComment(String comment) {
    if (comment == null) {
      return comment;
    }
    if (TRIM_LENGTH < comment.length()) {
      return comment.substring(0, TRIM_LENGTH) + "...";
    } else {
      return comment;
    }
  }

  public static RuntimeException wrapAndThrow(Throwable t) {
    if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    }
    throw new RuntimeException(t);
  }
}
