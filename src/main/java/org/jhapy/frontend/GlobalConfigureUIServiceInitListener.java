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

import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.text.MessageFormat;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.config.AppProperties;
import org.jhapy.frontend.exceptions.AccessDeniedException;
import org.jhapy.frontend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;

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

  @Autowired
  private AppProperties appProperties;

  @Override
  public void serviceInit(ServiceInitEvent event) {
    event.getSource().addUIInitListener(uiEvent -> {
      var ui = uiEvent.getUI();
      ui.getLoadingIndicatorConfiguration().setApplyDefaultTheme(false);
    });
  }
}
