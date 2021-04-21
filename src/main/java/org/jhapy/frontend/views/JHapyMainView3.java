/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.frontend.views;

import static org.jhapy.frontend.utils.AppConst.SECURITY_USER_ATTRIBUTE;

import com.hazelcast.core.HazelcastInstance;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import de.codecamp.vaadin.components.messagedialog.MessageDialog;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.messageQueue.NewSession;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.client.audit.AuditServices;
import org.jhapy.frontend.components.AppCookieConsent;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.components.navigation.drawer.NaviDrawerWithTreeMenu;
import org.jhapy.frontend.components.search.overlay.SearchOverlayButton;
import org.jhapy.frontend.dataproviders.MenuHierarchicalDataProvider;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.AttributeContextListener;
import org.jhapy.frontend.utils.FontSize;
import org.jhapy.frontend.utils.IconSize;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.SessionInfo;
import org.jhapy.frontend.utils.TextColor;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.Overflow;
import org.jhapy.frontend.utils.css.Shadow;
import org.jhapy.frontend.views.admin.MonitoringAdminView;
import org.jhapy.frontend.views.admin.audit.SessionView;
import org.jhapy.frontend.views.admin.configServer.CloudConfigView;
import org.jhapy.frontend.views.admin.eureka.EurekaView;
import org.jhapy.frontend.views.admin.i18n.ActionsView;
import org.jhapy.frontend.views.admin.i18n.ElementsView;
import org.jhapy.frontend.views.admin.i18n.MessagesView;
import org.jhapy.frontend.views.admin.messaging.MailAdminView;
import org.jhapy.frontend.views.admin.messaging.MailTemplatesAdminView;
import org.jhapy.frontend.views.admin.messaging.SmsAdminView;
import org.jhapy.frontend.views.admin.messaging.SmsTemplatesAdminView;
import org.jhapy.frontend.views.admin.references.CountriesView;
import org.jhapy.frontend.views.admin.security.SecurityKeycloakGroupsView;
import org.jhapy.frontend.views.admin.security.SecurityKeycloakRolesView;
import org.jhapy.frontend.views.admin.security.SecurityKeycloakUsersView;
import org.jhapy.frontend.views.admin.swagger.SwaggersAdminView;
import org.jhapy.frontend.views.menu.MenuData;
import org.jhapy.frontend.views.menu.MenuEntry;
import org.springframework.core.env.Environment;
import org.vaadin.tatu.Tree;

@CssImport(value = "./styles/components/charts.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
@CssImport(value = "./styles/components/floating-action-button.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/components/grid.css", themeFor = "vaadin-grid")
@CssImport("./styles/lumo/border-radius.css")
@CssImport("./styles/lumo/icon-size.css")
@CssImport("./styles/lumo/margin.css")
@CssImport("./styles/lumo/padding.css")
@CssImport("./styles/lumo/shadow.css")
@CssImport("./styles/lumo/spacing.css")
@CssImport("./styles/lumo/typography.css")
@CssImport("./styles/misc/box-shadow-borders.css")
@CssImport(value = "./styles/styles.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge")
public abstract class JHapyMainView3 extends FlexBoxLayout
    implements RouterLayout, PageConfigurator, AfterNavigationObserver, HasLogger {

  private static final String CLASS_NAME = "root";
  private final ConfirmDialog confirmDialog;
  protected FlexBoxLayout viewContainer;
  private Div appHeaderOuter;
  private FlexBoxLayout row;
  private NaviDrawerWithTreeMenu naviDrawer;
  private FlexBoxLayout column;
  private Div appHeaderInner;
  private Div appFooterInner;
  private Environment environment;
  private Div appFooterOuter;
  private AppBar appBar;
  protected MenuHierarchicalDataProvider menuProvider;
  private final HazelcastInstance hazelcastInstance;
  private final List<AttributeContextListener> contextListeners = new ArrayList<>();

  public JHapyMainView3(MenuHierarchicalDataProvider menuProvider,
      HazelcastInstance hazelcastInstance, Environment environment) {
    this.menuProvider = menuProvider;
    this.hazelcastInstance = hazelcastInstance;

    afterLogin();

    this.confirmDialog = new ConfirmDialog();
    confirmDialog.setCancelable(true);
    confirmDialog.setConfirmButtonTheme("raised tertiary error");
    confirmDialog.setCancelButtonTheme("raised tertiary");

    getElement().appendChild(confirmDialog.getElement());

    addClassName(CLASS_NAME);
    //setBackgroundColor(LumoStyles.Color.Contrast._5);
    setFlexDirection(FlexDirection.COLUMN);
    setSizeFull();

    // Initialise the UI building blocks
    initStructure(menuProvider, false, getAltSearchMenu(), environment.getProperty("APP_VERSION"),
        environment.getProperty("info.tags.environment"));

    // Populate the navigation drawer
    initNaviItems();
    postNaviItems(naviDrawer.getMenuComponent());

    // Configure the headers and footers (optional)
    initHeadersAndFooters();

    getElement().appendChild(new AppCookieConsent().getElement());
  }

  public void addAttributeContextListener(AttributeContextListener contextListener) {
    contextListeners.add(contextListener);
  }

  public void removeAttributeContextListener(AttributeContextListener contextListener) {
    contextListeners.remove(contextListener);
  }

  public void fireAttributeContextChanged(String attributeName, Object attributeValue) {
    contextListeners.parallelStream().forEach(contextListener -> contextListener
        .onAttributeContextChanged(attributeName, attributeValue));
  }

  protected Component getAltSearchMenu() {
    return null;
  }

  private ConcurrentMap<String, SessionInfo> retrieveMap() {
    return hazelcastInstance.getMap("userSessions");
  }

  public static JHapyMainView3 get() {
    return (JHapyMainView3) UI.getCurrent().getChildren()
        .filter(component -> RouterLayout.class.isAssignableFrom(component.getClass()))
        .findFirst().orElse(null);
  }

  public SearchOverlayButton<? extends SearchQueryResult, ? extends SearchQuery> getSearchButton() {
    return null;
  }

  public Class getHomePage() {
    return null;
  }

  public Class getUserSettingsView() {
    return null;
  }

  public boolean hasLanguageSelect() {
    return true;
  }

  public boolean hasGlobalSearch() {
    return true;
  }

  public StoredFile getLoggedUserAvatar(SecurityUser securityUser) {
    return null;
  }

  protected String getCurrentUser() {
    return org.jhapy.commons.security.SecurityUtils.getCurrentUserLogin().get();
  }

  public void afterLogin() {
    String loggerPrefix = getLoggerPrefix("afterLogin");

    SecurityUser currentSecurityUser = (SecurityUser) VaadinSession.getCurrent()
        .getAttribute(SECURITY_USER_ATTRIBUTE);
    if (currentSecurityUser == null) {
      currentSecurityUser = SecurityUtils.getSecurityUser();
      if (currentSecurityUser != null) {
        VaadinSession currentSession = VaadinSession.getCurrent();
        VaadinRequest currentRequest = VaadinRequest.getCurrent();

        // 5 minutes
        logger().info(loggerPrefix + "Max Inactive Interval = " + currentSession.getSession()
            .getMaxInactiveInterval());
        // currentSession.getSession().setMaxInactiveInterval( 2 * 60);

        logger().info(
            loggerPrefix + "Create remote session, Session ID = " + currentSession.getSession()
                .getId());
        AuditServices.getAuditServiceQueue().newSession(
            new NewSession(currentSession.getSession().getId(),
                currentSecurityUser.getUsername(), currentRequest.getRemoteAddr(), Instant
                .now(),
                true, null));

        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setJSessionId(currentSession.getSession().getId());
        sessionInfo.setLoginDateTime(LocalDateTime.now());
        sessionInfo.setLastContact(LocalDateTime.now());
        sessionInfo.setSourceIp(currentRequest.getRemoteAddr());
        sessionInfo.setUsername(currentSecurityUser.getUsername());
        retrieveMap().put(sessionInfo.getJSessionId(), sessionInfo);
      }
    }
  }

  /**
   * Initialise the required components and containers.
   */
  private void initStructure(MenuHierarchicalDataProvider menuProvider, boolean showSearchMenu,
      Component altSearchMenu,
      String version, String environnement) {
    naviDrawer = new NaviDrawerWithTreeMenu(menuProvider, showSearchMenu, altSearchMenu, version,
        environnement);

    viewContainer = new FlexBoxLayout();
    viewContainer.addClassName(CLASS_NAME + "__view-container");
    viewContainer.setOverflow(Overflow.HIDDEN);

    column = new FlexBoxLayout(viewContainer);
    column.addClassName(CLASS_NAME + "__column");
    column.setFlexDirection(FlexDirection.COLUMN);
    column.setFlexGrow(1, viewContainer);
    column.setOverflow(Overflow.HIDDEN);

    row = new FlexBoxLayout(naviDrawer, column);
    row.addClassName(CLASS_NAME + "__row");
    row.setFlexGrow(1, column);
    row.setOverflow(Overflow.HIDDEN);
    add(row);
    setFlexGrow(1, row);
  }

  public void rebuildNaviItems() {
    rebuildNaviItems(true);
  }

  public void rebuildNaviItems(boolean resetAppBar) {
    List<MenuEntry> expandedMenus = new ArrayList<>();

    naviDrawer.getMenu().getMenuList().forEach(menuEntry -> {
          if (naviDrawer.getMenuComponent().isExpanded(menuEntry)) {
            expandedMenus.add(menuEntry);
          }
        }
    );

    // naviDrawer.refreshMenu();

    naviDrawer.toogleSearch();
    initNaviItems();

    UI.getCurrent().access(() -> {
      naviDrawer.getMenuComponent().expand(expandedMenus);
      naviDrawer.navigate(naviDrawer.getLastMenuEntry());
    });

    if (resetAppBar) {
      appBar.reset();
      appBar.rebuildMenu();
    }
  }

  public AppBar getAppBar() {
    return appBar;
  }

  protected void addToMainMenu(MenuData menuData) {
  }

  protected void addToSettingsMenu(MenuData menuData, MenuEntry settingMenu) {
  }

  protected boolean hasSettingsMenuEntries() {
    return false;
  }

  protected void addToReferencesMenu(MenuData menuData, MenuEntry referenceMenu) {
  }

  protected boolean hasReferencesMenuEntries() {
    return false;
  }

  protected void addToSecurityMenu(MenuData menuData, MenuEntry securityMenu) {
  }

  protected boolean hasSecurityMenuEntries() {
    return false;
  }

  protected void postNaviItems(Tree<MenuEntry> menu) {

  }

  /**
   * Initialise the navigation items.
   */
  private void initNaviItems() {
    UI currentUI = UI.getCurrent();

    MenuData menuData = naviDrawer.getFreshMenu();

    addToMainMenu(menuData);

    if (SecurityUtils.isUserLoggedIn()) {

      boolean isSettingsDisplayed = hasSettingsMenuEntries() ||
          SecurityUtils.isAccessGranted(ActionsView.class) ||
          SecurityUtils.isAccessGranted(ElementsView.class) ||
          SecurityUtils.isAccessGranted(MessagesView.class) ||
          SecurityUtils.isAccessGranted(CountriesView.class) ||
          SecurityUtils.isAccessGranted(SecurityKeycloakUsersView.class) ||
          SecurityUtils.isAccessGranted(SecurityKeycloakRolesView.class) ||
          SecurityUtils.isAccessGranted(SecurityKeycloakGroupsView.class);

      if (isSettingsDisplayed) {
        MenuEntry settingsSubMenu = new MenuEntry(AppConst.PAGE_SETTINGS);
        settingsSubMenu.setVaadinIcon(VaadinIcon.EDIT);
        settingsSubMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SETTINGS));

        addToSettingsMenu(menuData, settingsSubMenu);

        menuData.addMenuEntry(settingsSubMenu);
        /*
         * i18N
         */
        boolean isDisplayI18n = SecurityUtils.isAccessGranted(ActionsView.class) ||
            SecurityUtils.isAccessGranted(ElementsView.class) ||
            SecurityUtils.isAccessGranted(MessagesView.class);

        if (isDisplayI18n) {
          MenuEntry i18nSubmenu = new MenuEntry(AppConst.PAGE_I18N);
          i18nSubmenu.setVaadinIcon(VaadinIcon.SITEMAP);
          i18nSubmenu.setTitle(currentUI.getTranslation(AppConst.TITLE_I18N));
          i18nSubmenu.setParentMenuEntry(settingsSubMenu);

          menuData.addMenuEntry(i18nSubmenu);

          if (SecurityUtils.isAccessGranted(ActionsView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_ACTIONS);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_ACTIONS));
            subMenu.setTargetClass(ActionsView.class);
            subMenu.setParentMenuEntry(i18nSubmenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }

          if (SecurityUtils.isAccessGranted(ElementsView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_ELEMENTS);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_ELEMENTS));
            subMenu.setTargetClass(ElementsView.class);
            subMenu.setParentMenuEntry(i18nSubmenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }

          if (SecurityUtils.isAccessGranted(MessagesView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_MESSAGES);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_MESSAGES));
            subMenu.setTargetClass(MessagesView.class);
            subMenu.setParentMenuEntry(i18nSubmenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }
        }

        /*
         * Reference
         */

        boolean isReferenceMenuDisplay = hasReferencesMenuEntries() ||
            SecurityUtils.isAccessGranted(CountriesView.class);

        if (isReferenceMenuDisplay) {
          MenuEntry referenceSubMenu = new MenuEntry(AppConst.PAGE_REFERENCES);
          referenceSubMenu.setVaadinIcon(VaadinIcon.SITEMAP);
          referenceSubMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_REFERENCES));
          referenceSubMenu.setParentMenuEntry(settingsSubMenu);

          menuData.addMenuEntry(referenceSubMenu);

          if (hasReferencesMenuEntries()) {
            addToReferencesMenu(menuData, referenceSubMenu);
          }

        }

        /*
         * Notification
         */
        boolean isDisplayNotifications =
            SecurityUtils.isAccessGranted(MailTemplatesAdminView.class) ||
                SecurityUtils.isAccessGranted(SmsTemplatesAdminView.class) ||
                SecurityUtils.isAccessGranted(SmsAdminView.class) ||
                SecurityUtils.isAccessGranted(MailAdminView.class);

        if (isDisplayNotifications) {
          MenuEntry notificationsSubMenu = new MenuEntry(AppConst.PAGE_NOTIFICATIONS);
          notificationsSubMenu.setVaadinIcon(VaadinIcon.SITEMAP);
          notificationsSubMenu
              .setTitle(currentUI.getTranslation(AppConst.TITLE_NOTIFICATION_ADMIN));
          notificationsSubMenu.setParentMenuEntry(settingsSubMenu);

          menuData.addMenuEntry(notificationsSubMenu);

          if (SecurityUtils.isAccessGranted(MailTemplatesAdminView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_MAIL_TEMPLATES_ADMIN);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_MAIL_TEMPLATES_ADMIN));
            subMenu.setTargetClass(MailTemplatesAdminView.class);
            subMenu.setParentMenuEntry(notificationsSubMenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }

          if (SecurityUtils.isAccessGranted(SmsTemplatesAdminView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_SMS_TEMPLATES_ADMIN);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SMS_TEMPLATES_ADMIN));
            subMenu.setTargetClass(SmsTemplatesAdminView.class);
            subMenu.setParentMenuEntry(notificationsSubMenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }

          if (SecurityUtils.isAccessGranted(SmsAdminView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_SMS_ADMIN);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SMS));
            subMenu.setTargetClass(SmsAdminView.class);
            subMenu.setParentMenuEntry(notificationsSubMenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }

          if (SecurityUtils.isAccessGranted(MailAdminView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_MAILS_ADMIN);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_MAILS));
            subMenu.setTargetClass(MailAdminView.class);
            subMenu.setParentMenuEntry(notificationsSubMenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }
        }
        /*
         * Security
         */
        boolean isDisplaySecurity = hasSecurityMenuEntries() ||
            SecurityUtils.isAccessGranted(SecurityKeycloakUsersView.class) ||
            SecurityUtils.isAccessGranted(SecurityKeycloakRolesView.class) ||
            SecurityUtils.isAccessGranted(SecurityKeycloakGroupsView.class);

        if (isDisplaySecurity) {
          MenuEntry securitySubMenu = new MenuEntry(AppConst.PAGE_SECURITY);
          securitySubMenu.setVaadinIcon(VaadinIcon.KEY);
          securitySubMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SECURITY));
          securitySubMenu.setParentMenuEntry(settingsSubMenu);

          menuData.addMenuEntry(securitySubMenu);

          if (SecurityUtils.isAccessGranted(SecurityKeycloakUsersView.class)) {
            MenuEntry subMenu = getSecurityUserMenuEntry(securitySubMenu);

            menuData.addMenuEntry(subMenu);
          }

          if (SecurityUtils.isAccessGranted(SecurityKeycloakRolesView.class)) {
            MenuEntry subMenu = getSecurityRoleMenuEntry(securitySubMenu);

            menuData.addMenuEntry(subMenu);
          }

          if (SecurityUtils.isAccessGranted(SecurityKeycloakGroupsView.class)) {
            MenuEntry subMenu = getSecurityGroupsMenuEntry(securitySubMenu);

            menuData.addMenuEntry(subMenu);
          }

          if (SecurityUtils.isAccessGranted(SessionView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_SESSIONS);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SESSIONS_ADMIN));
            subMenu.setTargetClass(SessionView.class);
            subMenu.setParentMenuEntry(securitySubMenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }

          addToSecurityMenu(menuData, securitySubMenu);
        }
        boolean isDisplayMonitoring =
            SecurityUtils.isAccessGranted(EurekaView.class) ||
                SecurityUtils.isAccessGranted(CloudConfigView.class);
        if (isDisplayMonitoring) {
          MenuEntry monitoringSubMenu = new MenuEntry(AppConst.PAGE_MONITORING);
          monitoringSubMenu.setVaadinIcon(VaadinIcon.GLASSES);
          monitoringSubMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_MONITORING));
          monitoringSubMenu.setParentMenuEntry(settingsSubMenu);

          menuData.addMenuEntry(monitoringSubMenu);

          if (SecurityUtils.isAccessGranted(EurekaView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_EUREKA_ADMIN);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_EUREKA_ADMIN));
            subMenu.setTargetClass(EurekaView.class);
            subMenu.setParentMenuEntry(monitoringSubMenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }
          if (SecurityUtils.isAccessGranted(CloudConfigView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_CLOUD_CONFIG_ADMIN);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_CLOUD_CONFIG_ADMIN));
            subMenu.setTargetClass(CloudConfigView.class);
            subMenu.setParentMenuEntry(monitoringSubMenu);
            subMenu.setHasChildNodes(false);

            menuData.addMenuEntry(subMenu);
          }
          if (SecurityUtils.isAccessGranted(MonitoringAdminView.class)) {
            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_ACTUAL_SESSIONS_ADMIN);
            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_ACTUAL_SESSIONS_ADMIN));
            subMenu.setTargetClass(MonitoringAdminView.class);
            subMenu.setParentMenuEntry(monitoringSubMenu);
            subMenu.setHasChildNodes(false);
            menuData.addMenuEntry(subMenu);
          }
        }
        if (SecurityUtils.isAccessGranted(SwaggersAdminView.class)) {
          MenuEntry swaggersMenu = new MenuEntry(AppConst.TITLE_SWAGGERS_ADMIN);
          swaggersMenu.setVaadinIcon(VaadinIcon.CODE);
          swaggersMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SWAGGERS_ADMIN));
          swaggersMenu.setTargetClass(SwaggersAdminView.class);
          swaggersMenu.setParentMenuEntry(settingsSubMenu);
          swaggersMenu.setHasChildNodes(false);
          menuData.addMenuEntry(swaggersMenu);
        }
      }
      naviDrawer.refreshMenu();
    }
  }

  protected MenuEntry getSecurityUserMenuEntry(MenuEntry parentEntry) {
    MenuEntry subMenu = new MenuEntry(AppConst.PAGE_USERS);
    subMenu.setVaadinIcon(VaadinIcon.QUESTION);
    subMenu.setTitle(UI.getCurrent().getTranslation(AppConst.TITLE_SECURITY_USERS));
    subMenu.setTargetClass(SecurityKeycloakUsersView.class);
    subMenu.setParentMenuEntry(parentEntry);
    subMenu.setHasChildNodes(false);

    return subMenu;
  }

  protected MenuEntry getSecurityRoleMenuEntry(MenuEntry parentEntry) {
    MenuEntry subMenu = new MenuEntry(AppConst.PAGE_ROLES);
    subMenu.setVaadinIcon(VaadinIcon.QUESTION);
    subMenu.setTitle(UI.getCurrent().getTranslation(AppConst.TITLE_SECURITY_ROLES));
    subMenu.setTargetClass(SecurityKeycloakRolesView.class);
    subMenu.setParentMenuEntry(parentEntry);
    subMenu.setHasChildNodes(false);

    return subMenu;
  }

  protected MenuEntry getSecurityGroupsMenuEntry(MenuEntry parentEntry) {
    MenuEntry subMenu = new MenuEntry(AppConst.PAGE_GROUPS);
    subMenu.setVaadinIcon(VaadinIcon.QUESTION);
    subMenu.setTitle(UI.getCurrent().getTranslation(AppConst.TITLE_SECURITY_GROUPS));
    subMenu.setTargetClass(SecurityKeycloakGroupsView.class);
    subMenu.setParentMenuEntry(parentEntry);
    subMenu.setHasChildNodes(false);

    return subMenu;
  }

  /**
   * Configure the app's inner and outer headers and footers.micrometer-core.version
   */
  protected void initHeadersAndFooters() {
    //setAppHeaderOuter();
    //setAppFooterOuter();

    // setAppFooterInner();

    appBar = new AppBar();
    //UIUtils.setTheme(Lumo.DARK, appBar);
    setAppHeaderInner(appBar);
  }

  protected void setAppHeaderOuter(Component... components) {
    if (appHeaderOuter == null) {
      appHeaderOuter = new Div();
      appHeaderOuter.addClassName("app-header-outer");
      getElement().insertChild(0, appHeaderOuter.getElement());
    }
    appHeaderOuter.removeAll();
    appHeaderOuter.add(components);
  }

  protected void setAppHeaderInner(Component... components) {
    if (appHeaderInner == null) {
      appHeaderInner = new Div();
      appHeaderInner.addClassName("app-header-inner");
      column.getElement().insertChild(0, appHeaderInner.getElement());
    }
    appHeaderInner.removeAll();
    appHeaderInner.add(components);
  }

  protected void setAppFooterInner(Component... components) {
    if (appFooterInner == null) {
      appFooterInner = new Div();
      appFooterInner.addClassName("app-footer-inner");
      column.getElement().insertChild(column.getElement().getChildCount(),
          appFooterInner.getElement());
    }
    appFooterInner.removeAll();
    appFooterInner.add(components);

    (new FeederThread(getUI().get(), 3000, appFooterInner, components)).start();
  }

  protected void setAppFooterOuter(Component... components) {
    if (appFooterOuter == null) {
      appFooterOuter = new Div();
      appFooterOuter.addClassName("app-footer-outer");
      getElement().insertChild(getElement().getChildCount(),
          appFooterOuter.getElement());
    }
    appFooterOuter.removeAll();
    appFooterOuter.add(components);
  }

  @Override
  public void configurePage(InitialPageSettings settings) {
    settings.addMetaTag("apple-mobile-web-app-capable", "yes");
    settings.addMetaTag("apple-mobile-web-app-status-bar-style", "black");

    settings.addFavIcon("icon", "icons/icon-192x192.png", "192x192");
  }

  public NaviDrawerWithTreeMenu getNaviDrawer() {
    return naviDrawer;
  }

  public void displayInfoMessage(String message) {
    Icon icon = UIUtils.createIcon(IconSize.S, TextColor.SUCCESS, VaadinIcon.CHECK);
    Label label = UIUtils.createLabel(FontSize.XS, TextColor.BODY, message);

    FlexLayout messageLayout = new FlexLayout(icon, label);

    // Set the alignment
    messageLayout.setAlignItems(Alignment.CENTER);

    // Add spacing and padding
    messageLayout.addClassNames(
        LumoStyles.Spacing.Right.S,
        LumoStyles.Padding.Wide.M
    );

    Notification notification = new Notification(messageLayout);
    notification.setDuration(3000);
    notification.setPosition(Position.TOP_CENTER);

    UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, notification);
    UIUtils.setShadow(Shadow.M, notification);

    notification.open();

    // getAppBar().addNotification(new DefaultNotification(getTranslation("message.global.info"), message, Priority.MEDIUM));
    /*
    Icon icon = UIUtils.createIcon(IconSize.S, TextColor.SUCCESS, VaadinIcon.CHECK);
    Label label = UIUtils.createLabel(FontSize.XS, TextColor.BODY, message);

    FlexLayout footer = new FlexLayout(icon, label);

    // Set the alignment
    footer.setAlignItems(Alignment.CENTER);

    // Add spacing and padding
    footer.addClassNames(
        LumoStyles.Spacing.Right.S,
        LumoStyles.Padding.Wide.M
    );

    // Set background color and shadow
    UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, footer);
    UIUtils.setShadow(Shadow.M, footer);

    setAppFooterInner(footer);

     */
  }

  public void displayErrorMessage(String message) {
    Icon icon = UIUtils.createIcon(IconSize.S, TextColor.ERROR, VaadinIcon.WARNING);
    MessageDialog okDialog = new MessageDialog()
        .setTitle(getTranslation("message.global.error"), icon)
        .setMessage(message);
    okDialog.addButton().text(getTranslation("action.global.close")).primary()
        .closeOnClick().clickShortcutEnter().clickShortcutEscape().closeOnClick();
    okDialog.open();
    /*
    Label label = UIUtils.createLabel(FontSize.XS, TextColor.ERROR, message);

    FlexLayout messageLayout = new FlexLayout(icon, label);

    // Set the alignment
    messageLayout.setAlignItems(Alignment.CENTER);

    // Add spacing and padding
    messageLayout.addClassNames(
        LumoStyles.Spacing.Right.S,
        LumoStyles.Padding.Wide.M
    );

    Notification notification = new Notification( messageLayout );
    notification.setDuration(0);
    notification.setPosition(Position.TOP_CENTER);


    Button closeButton = UIUtils.createSmallButton(getTranslation("action.global.close"));
    closeButton.addThemeVariants( ButtonVariant.LUMO_ERROR);
    closeButton.addClickListener(event -> notification.close());
    FlexLayout footer = new FlexLayout(closeButton);
    footer.setJustifyContentMode(JustifyContentMode.CENTER);
    footer.setWidthFull();

    notification.add(footer);

    UIUtils.setBackgroundColor(LumoStyles.Color.BASE_COLOR, notification);
    UIUtils.setShadow(Shadow.M, notification);

    notification.open();
     */
  }


  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    MenuEntry active = getActiveItem(event);
    if (active != null && StringUtils.isBlank(appBar.getTitle())) {
      appBar.setTitle(active.getTitle());
    }
  }

  private MenuEntry getActiveItem(AfterNavigationEvent e) {
    for (MenuEntry item : naviDrawer.getMenuComponent().getSelectedItems()) {
      return item;
    }
    return null;
  }

  public abstract void onLogout();

  public void beforeLogin() {
  }

  private static class FeederThread extends Thread {

    private final UI ui;
    private final Div appFooterInner;
    private final Component[] components;

    private long delay = 0;

    public FeederThread(UI ui, long delay, Div appFooterInner, Component[] components) {
      this.ui = ui;
      this.delay = delay;
      this.appFooterInner = appFooterInner;
      this.components = components;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(delay);

        // Inform that we are done
        ui.access(() -> appFooterInner.getChildren().forEach(component -> {
              for (Component c : components) {
                if (c.equals(component)) {
                  appFooterInner.remove(component);
                }
              }
            }
        ));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
