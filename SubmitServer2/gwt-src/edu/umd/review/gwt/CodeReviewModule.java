package edu.umd.review.gwt;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * @author Ryan W Sims (rwsims@umd.edu)
 *
 */
public class CodeReviewModule extends AbstractGinModule {

  @Override
  protected void configure() { }

  @Provides @Singleton
  PickupDragController getDragController() {
    PickupDragController controller = new PickupDragController(RootPanel.get(), false);
    controller.setBehaviorDragProxy(true);
    return controller;
  }

}
