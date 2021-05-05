/*
 *
 *  DO YOU WANNA ENJOY CONFIDENTIAL
 *   __________________
 *
 *   [2018] - [2019] Do You Wanna Play
 *   All Rights Reserved.
 *
 *   NOTICE:  All information contained herein is, and remains the property of "Do You Wanna Play"
 *   and its suppliers, if any. The intellectual and technical concepts contained herein are
 *   proprietary to "Do You Wanna Play" and its suppliers and may be covered by Morocco. and Foreign
 *   Patents, patents in process, and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material is strictly forbidden unless
 *    prior written permission is obtained from "Do You Wanna Play".
 */

package org.jhapy.frontend;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.jhapy.commons.security.SecurityUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.exceptions.AccessDeniedException;

/**
 * Adds before enter listener to check access to views. Adds the Offline banner.
 *
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 2019-03-26
 */
@SpringComponent
@CssImport(value = "./styles/loading-indicator.css")
public class GlobalConfigureUIServiceInitListener implements VaadinServiceInitListener, HasLogger {

  @Override
  public void serviceInit(ServiceInitEvent event) {
    var loggerPrefix = getLoggerPrefix("serviceInit");
    event.getSource().addUIInitListener(uiEvent -> {
      final UI ui = uiEvent.getUI();
      ui.getLoadingIndicatorConfiguration().setApplyDefaultTheme(false);
      //ui.add(new OfflineBanner());
      ui.addBeforeEnterListener(this::beforeEnter);
    });
  }

  /**
   * Reroutes the user if she is not authorized to access the view.
   *
   * @param event before navigation event with event details
   */
  private void beforeEnter(BeforeEnterEvent event) {
    final boolean accessGranted = org.jhapy.frontend.security.SecurityUtils
        .isAccessGranted(event.getNavigationTarget());
    if (!accessGranted) {
      if (SecurityUtils.isUserLoggedIn()) {
        event.rerouteToError(AccessDeniedException.class);
      } else {
        event.rerouteTo("/");
      }
    }
  }
}
