package edu.umd.review.gwt.view.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.umd.review.gwt.rpc.dto.FileDto;
import edu.umd.review.gwt.view.FileView;
import edu.umd.review.gwt.view.ThreadView;
import edu.umd.review.gwt.widget.DoubleClickLabel;
import edu.umd.review.gwt.widget.RubricDragger;

/**
 * @author rwsims@umd.edu (Ryan W Sims)
 *
 */
public class FileViewImpl extends Composite implements FileView {
  /** UiBinder interface for FileViewImpl. */
  interface FileViewImplUiBinder extends UiBinder<Widget, FileViewImpl> { }
  private static UiBinder<Widget, FileViewImpl> uiBinder = GWT.create(FileViewImplUiBinder.class);

  private static final int minCollapseLength = 8;
  private static final Logger logger = Logger.getLogger(FileViewImpl.class.getName());

  @UiField Label fileLabel;
  @UiField FlexTable codeGrid;

  @CheckForNull
  private Presenter presenter;
  private final Provider<ThreadView> threadViewProvider;
  private final Map<Integer, FlowPanel> codePanels = Maps.newTreeMap();
  /** Maps the number of a line of code to the row in codeGrid where it is displayed. */
  private final BiMap<Integer, Integer> linesToRows = HashBiMap.create();
  private final Map<Integer, Integer> rowsToLines = linesToRows.inverse();

  private FileDropController controller;

  @Inject
  public FileViewImpl(Provider<ThreadView> threadViewProvider) {
    initWidget(uiBinder.createAndBindUi(this));
    this.threadViewProvider = threadViewProvider;
    controller = new FileDropController();
  }

  private void setLineNumber(int row, int line) {
    codeGrid.setText(row, 0, (line + 1) + ":");
    codeGrid.getCellFormatter().setStyleName(row, 0, "line-number");
    codeGrid.getCellFormatter().addStyleName(row, 0, "code");
  }

  private Widget makeCodeLabel(int line, String code, boolean modified) {
    if (modified) {
      DoubleClickLabel lineLabel = new DoubleClickLabel(code);
      lineLabel.addDoubleClickHandler(makeHandler(line));
      lineLabel.addStyleName("code");
      return lineLabel;
    } else {
      Label label = new Label(Strings.isNullOrEmpty(code) ? " " : code);
      label.addStyleName("code");
      return label;
    }
  }

  private Widget makeElidedLabel(int elidedCount) {
    Label label = new Label("..." + elidedCount + " lines elided...");
    label.addStyleName("code");
    return label;
  }

  private void showCollapsedCode(FileDto file) {
    codeGrid.removeAllRows();
    int row = 0;
    int[] linesToShow = file.getLinesToShow();
    if (linesToShow == null || linesToShow.length == 0) {
      return;
    }
    if (linesToShow[0] > 0) {
        // Many lines elided.
        int dist = linesToShow[0];
        codeGrid.setWidget(row, 1, makeElidedLabel(dist));
        codeGrid.getRowFormatter().setStylePrimaryName(row, "elided-code-label");
        row++;
    }
    List<String> lines = file.getLines();
    int i;
    for (i = 0; i < linesToShow.length; i++) {
      int line = linesToShow[i];
      String code = lines.get(line);
      setLineNumber(row, line);
      FlowPanel codePanel = new FlowPanel();
      codePanel.setStylePrimaryName("code-panel");
      if (file.isModified(line)) {
        codeGrid.getRowFormatter().setStylePrimaryName(row, "modified-code-row");
      } else {
        codeGrid.getRowFormatter().setStylePrimaryName(row, "unmodified-code-row");
      }
      Widget codeWidget = makeCodeLabel(line, code, file.isModified(line));
      codePanel.add(codeWidget);
      codeGrid.setWidget(row, 1, codePanel);
      codePanels.put(line, codePanel);
      linesToRows.put(line, row);
      line++;
      row++;
      if (i < linesToShow.length - 1 && linesToShow[i + 1] > line + 1) {
        // Many lines elided.
        int dist = linesToShow[i + 1] - line;
        codeGrid.setWidget(row, 1, makeElidedLabel(dist));
        codeGrid.getRowFormatter().setStylePrimaryName(row, "elided-code-label");
        row++;
      }
    }
    int lastLine = linesToShow[i - 1];
    if (lastLine < lines.size() - 1) {
        // Many lines elided.
        int dist = (lines.size() - 1) - lastLine;
        codeGrid.setWidget(row, 1, makeElidedLabel(dist));
        codeGrid.getRowFormatter().setStylePrimaryName(row, "elided-code-label");
        row++;
    }
  }

  @Override
  public void setFile(FileDto file) {
    Preconditions.checkNotNull(presenter, "Must set presenter before setting lines.");
    codeGrid.removeAllRows();
    showCollapsedCode(file);
    // TODO(rwsims): Syntax highlighting. Tried external prettify.js library, but prettifying eats
    //               the event handling.
  }

  private DoubleClickHandler makeHandler(final int line) {
    return new DoubleClickHandler() {
      @Override
      public void onDoubleClick(DoubleClickEvent event) {
        if (presenter != null) {
          presenter.onNewThreadAction(line);
        }
      }
    };
  }

  @Override
  public void setPresenter(Presenter presenter) {
    if (presenter == null && this.presenter != null) {
      this.presenter.unregisterDropController(controller);
    }
    this.presenter = presenter;
    if (this.presenter != null) {
      this.presenter.registerDropController(controller);
    }
  }

  @Override
  public ThreadView getThreadView(int line, ThreadView before) {
    FlowPanel threadPanel = Preconditions.checkNotNull(codePanels.get(line),
                                                       "No code panel for line %s", line);
    ThreadView view = threadViewProvider.get();
    if (before != null) {
      int idx = threadPanel.getWidgetIndex(before);
      threadPanel.insert(view, idx);
    } else {
      threadPanel.add(view);
    }
    return view;
  }

  @Override
  public void deleteThreadView(int line, ThreadView view) {
    view.asWidget().removeFromParent();
  }

  @Override
  public void markModifiedLines(int[] lines) {
    for (int line : lines) {
      Integer row = linesToRows.get(line);
      if (row == null) {
        continue;
      }
      codeGrid.getRowFormatter().setStylePrimaryName(row, "modified-code-row");
    }
  }

  @Override
  public void setFileName(String name) {
    this.fileLabel.setText(name);
  }

  private class FileDropController extends AbstractDropController {

    private Widget current = null;
    private int currentRow = 0;

    public FileDropController() {
      super(FileViewImpl.this);
    }

    private void setCurrent(int y) {
      for (int i = 0; i < codeGrid.getRowCount(); i++) {
        Widget w = codeGrid.getWidget(i, 1);
        int top = w.getAbsoluteTop();
        int bottom = top + w.getOffsetHeight();
        if (top <= y && bottom > y) {
          current = w;
          currentRow = i;
        }
      }
    }

    @Override
    public void onMove(DragContext context) {
      if (current == null) {
        setCurrent(context.mouseY);
        if (current != null)
          current.addStyleDependentName("engaged");
        return;
      }
      current.removeStyleDependentName("engaged");
      int y = context.mouseY;
      if (y < current.getAbsoluteTop()) {
        currentRow--;
      } else if (y > current.getAbsoluteTop() + current.getOffsetHeight()) {
        currentRow++;
      }
      current = codeGrid.getWidget(currentRow, 1);
      current.addStyleDependentName("engaged");
    }

    @Override
    public void onLeave(DragContext context) {
      if (current != null)
        current.removeStyleDependentName("engaged");
      current = null;
      currentRow = 0;
    }

    @Override
    public void onDrop(DragContext context) {
      super.onDrop(context);
      if (presenter != null) {
        presenter.newThreadWithRubric(rowsToLines.get(currentRow),
                                      ((RubricDragger) context.draggable).getRubric());
      }
    }
  }
}
