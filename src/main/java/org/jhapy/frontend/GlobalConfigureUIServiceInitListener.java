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

import static org.jhapy.frontend.utils.AppConst.PAGE_ACTIONS;
import static org.jhapy.frontend.utils.AppConst.PAGE_CLOUD_CONFIG_ADMIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_COUNTRIES;
import static org.jhapy.frontend.utils.AppConst.PAGE_ELEMENTS;
import static org.jhapy.frontend.utils.AppConst.PAGE_EUREKA_ADMIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_LOGIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_MAILS_ADMIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_MAIL_TEMPLATES_ADMIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_MAIL_TEMPLATE_ADMIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_MESSAGES;
import static org.jhapy.frontend.utils.AppConst.PAGE_SECURITY_GROUPS;
import static org.jhapy.frontend.utils.AppConst.PAGE_SECURITY_ROLES;
import static org.jhapy.frontend.utils.AppConst.PAGE_SECURITY_USERS;
import static org.jhapy.frontend.utils.AppConst.PAGE_SESSIONS_ADMIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_SMS_ADMIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_SMS_TEMPLATES_ADMIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_SMS_TEMPLATE_ADMIN;

import com.flowingcode.vaadin.addons.errorwindow.ErrorManager;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.theme.lumo.Lumo;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.Provider.Service;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.Cookie;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.jhapy.commons.security.SecurityUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.exceptions.AccessDeniedException;
import org.jhapy.frontend.views.admin.audit.SessionView;
import org.jhapy.frontend.views.admin.configServer.CloudConfigView;
import org.jhapy.frontend.views.admin.eureka.EurekaView;
import org.jhapy.frontend.views.admin.i18n.ActionsView;
import org.jhapy.frontend.views.admin.i18n.ElementsView;
import org.jhapy.frontend.views.admin.i18n.MessagesView;
import org.jhapy.frontend.views.admin.messaging.MailAdminView;
import org.jhapy.frontend.views.admin.messaging.MailTemplateAdminView;
import org.jhapy.frontend.views.admin.messaging.MailTemplatesAdminView;
import org.jhapy.frontend.views.admin.messaging.SmsAdminView;
import org.jhapy.frontend.views.admin.messaging.SmsTemplateAdminView;
import org.jhapy.frontend.views.admin.messaging.SmsTemplatesAdminView;
import org.jhapy.frontend.views.admin.references.CountriesView;
import org.jhapy.frontend.views.admin.security.SecurityKeycloakGroupsView;
import org.jhapy.frontend.views.admin.security.SecurityKeycloakRolesView;
import org.jhapy.frontend.views.admin.security.SecurityKeycloakUsersView;
import org.jhapy.frontend.views.login.LoginView;

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
    String loggerPrefix = getLoggerPrefix("serviceInit");
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
        event.rerouteTo(LoginView.class);
      }
    }
  }
}
