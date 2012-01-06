package edu.umd.review.gwt.view.impl;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.review.gwt.AuthorColors;
import edu.umd.review.gwt.CodeReviewResources;

/**
 * @author Ryan W Sims <rwsims@umd.edu>
 *
 */
public class AuthorColorFactory {
  private static final AuthorColors colors;
  private static Map<String, String> authorColors = Maps.newHashMap();
  private static List<String> colorList = Lists.newArrayList();
  private static int nextColor;

  static {
    colors = CodeReviewResources.INSTANCE.colors();
    colors.ensureInjected();
    colorList.add(colors.color1());
    colorList.add(colors.color2());
    colorList.add(colors.color3());
    colorList.add(colors.color4());
    colorList.add(colors.color5());
  }

  public static int getNextColor() {
      int result = nextColor;
      nextColor = (nextColor + 1) % colorList.size();
      return result;

  }

  public String getColor(String author) {
    String color = authorColors.get(author);
    if (color == null) {
      color = colorList.get(getNextColor());
      authorColors.put(author, color);
    }
    return color;
  }
}
