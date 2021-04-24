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
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.Lumo;
import java.util.Locale;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.components.AppCookieConsent;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.components.navigation.drawer.NaviDrawer;
import org.jhapy.frontend.components.navigation.drawer.NaviItem;
import org.jhapy.frontend.components.navigation.drawer.NaviMenu;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.utils.AppConst;
import org.jhapy.frontend.utils.FontSize;
import org.jhapy.frontend.utils.IconSize;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.TextColor;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.Overflow;
import org.jhapy.frontend.utils.css.Shadow;
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
import org.jhapy.frontend.views.login.LoginView;
import org.springframework.core.env.Environment;

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
@Push
public abstract class JHapyMainView extends FlexBoxLayout
    implements RouterLayout, PageConfigurator, AfterNavigationObserver, HasLogger {

    private static final String CLASS_NAME = "root";
    private final ConfirmDialog confirmDialog;
    protected FlexBoxLayout viewContainer;
    private Div appHeaderOuter;
    private FlexBoxLayout row;
    private NaviDrawer naviDrawer;
    private FlexBoxLayout column;
    private Div appHeaderInner;
    private Div appFooterInner;
    private Environment environment;
    private Div appFooterOuter;

    private AppBar appBar;

    public JHapyMainView(Environment environment) {
        VaadinSession.getCurrent()
            .setErrorHandler((ErrorHandler) errorEvent -> {
                logger().error("Uncaught UI exception",
                    errorEvent.getThrowable());
                getUI().ifPresent(ui -> ui.accessLater(() -> {
                    Notification.show("We are sorry, but an internal error occurred");
                }, () -> {
                }));
            });

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
        initStructure(false, environment.getProperty("APP_VERSION"),
            environment.getProperty("info.tags.environment"));

        // Populate the navigation drawer
        initNaviItems();

        // Configure the headers and footers (optional)
        initHeadersAndFooters();

        getElement().appendChild(new AppCookieConsent().getElement());
    }

    public static JHapyMainView3 get() {
        return (JHapyMainView3) UI.getCurrent().getChildren()
            .filter(component -> RouterLayout.class.isAssignableFrom(component.getClass()))
            .findFirst().orElse(null);
    }

    public Class getHomePage() {
        return null;
    }

    public Class getUserSettingsView() {
        return null;
    }

    public StoredFile getLoggedUserAvatar(SecurityUser securityUser) {
        return null;
    }

    public void afterLogin() {
        //JHapyMainView.get().rebuildNaviItems();
        //UI.getCurrent().navigate(JHapyMainView.get().getHomePage());
    }


    /**
     * Initialise the required components and containers.
     */
    private void initStructure(boolean showSearchMenu, String version, String environnement) {
        naviDrawer = new NaviDrawer(showSearchMenu, version, environnement);

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
        naviDrawer.getMenu().removeAll();
        naviDrawer.toogleSearch();
        initNaviItems();

        if (resetAppBar) {
            appBar.reset();
            appBar.rebuildMenu();
        }
    }

    protected void addToMainMenu(NaviMenu mainMenu) {
    }

    protected void addToSettingsMenu(NaviMenu mainMenu, NaviItem settingMenu) {
    }

    protected boolean hasSettingsMenuEntries() {
        return false;
    }

    protected void addToReferencesMenu(NaviMenu mainMenu, NaviItem referenceMenu) {
    }

    protected boolean hasReferencesMenuEntries() {
        return false;
    }

    protected void addToSecurityMenu(NaviMenu mainMenu, NaviItem referenceMenu) {
    }

    protected boolean hasSecurityMenuEntries() {
        return false;
    }

    /**
     * Initialise the navigation items.
     */
    private void initNaviItems() {
        UI currentUI = UI.getCurrent();

        SecurityUser currentUser = SecurityUtils.getSecurityUser();
        if (currentUser != null && currentUser.getDefaultLocale() != null) {
            if (!UI.getCurrent().getLocale().getLanguage()
                .equals(currentUser.getDefaultLocale().getLanguage())) {
                confirmDialog.setHeader(getTranslation("element.title.confirmLocalChange"));
                confirmDialog.setText(
                    getTranslation("message.confirmLocalChange.message") + "Current = " + UI
                        .getCurrent()
                        .getLocale().getDisplayLanguage() + ", Settings = " + currentUser
                        .getDefaultLocale()
                        .getDisplayLanguage());
                confirmDialog
                    .setConfirmButton(getTranslation("action.localChange.confirm"), event -> {
                        Locale newLocale = currentUser.getDefaultLocale();
                        UI.getCurrent().setLocale(newLocale);
                        UI.getCurrent().getSession().setLocale(newLocale);
                        confirmDialog.close();
                        UI.getCurrent().getPage().executeJs("location.reload();");
                    });
                confirmDialog.setCancelButton(getTranslation("action.localChange.cancel"),
                    event -> confirmDialog.close());

                confirmDialog.open();
            }
        }

        NaviMenu menu = naviDrawer.getMenu();
        if (!SecurityUtils.isUserLoggedIn()) {
            menu.addNaviItem(VaadinIcon.CONNECT,
                currentUI.getTranslation(AppConst.TITLE_LOGIN, currentUI.getLocale()),
                LoginView.class);
        }

        addToMainMenu(menu);

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
                NaviItem settingsMenu = menu.addNaviItem(VaadinIcon.EDIT,
                    currentUI.getTranslation(AppConst.TITLE_SETTINGS, currentUI.getLocale()),
                    null);

                addToSettingsMenu(menu, settingsMenu);

                /*
                 * i18N
                 */
                boolean isDisplayI18n = SecurityUtils.isAccessGranted(ActionsView.class) ||
                    SecurityUtils.isAccessGranted(ElementsView.class) ||
                    SecurityUtils.isAccessGranted(MessagesView.class);

                if (isDisplayI18n) {
                    NaviItem i18nSubmenu = menu
                        .addNaviItem(settingsMenu,
                            currentUI.getTranslation(AppConst.TITLE_I18N, currentUI.getLocale()),
                            null);

                    if (SecurityUtils.isAccessGranted(ActionsView.class)) {
                        menu.addNaviItem(i18nSubmenu,
                            currentUI.getTranslation(AppConst.TITLE_ACTIONS, currentUI.getLocale()),
                            ActionsView.class);
                    }

                    if (SecurityUtils.isAccessGranted(ElementsView.class)) {
                        menu.addNaviItem(i18nSubmenu,
                            currentUI
                                .getTranslation(AppConst.TITLE_ELEMENTS, currentUI.getLocale()),
                            ElementsView.class);
                    }

                    if (SecurityUtils.isAccessGranted(MessagesView.class)) {
                        menu.addNaviItem(i18nSubmenu,
                            currentUI
                                .getTranslation(AppConst.TITLE_MESSAGES, currentUI.getLocale()),
                            MessagesView.class);
                    }
                }

                /*
                 * Reference
                 */
                boolean isReferenceMenuDisplay = hasReferencesMenuEntries() ||
                    SecurityUtils.isAccessGranted(CountriesView.class);

                if (isReferenceMenuDisplay) {
                    NaviItem referenceSubMenu = menu.addNaviItem(settingsMenu,
                        currentUI.getTranslation(AppConst.TITLE_REFERENCES, currentUI.getLocale()),
                        null);

                    addToReferencesMenu(menu, referenceSubMenu);

                    boolean isDisplayReference = SecurityUtils.isAccessGranted(CountriesView.class);

                    if (isDisplayReference) {

                        if (SecurityUtils.isAccessGranted(CountriesView.class)) {
                            menu.addNaviItem(referenceSubMenu,
                                currentUI.getTranslation(AppConst.TITLE_COUNTRIES,
                                    currentUI.getLocale()),
                                CountriesView.class);
                        }
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
                    NaviItem notificationsSubMenu = menu
                        .addNaviItem(settingsMenu,
                            currentUI
                                .getTranslation(AppConst.TITLE_NOTIFICATION_ADMIN,
                                    currentUI.getLocale()),
                            null);

                    if (SecurityUtils.isAccessGranted(MailTemplatesAdminView.class)) {
                        menu.addNaviItem(notificationsSubMenu,
                            currentUI
                                .getTranslation(AppConst.TITLE_MAIL_TEMPLATES_ADMIN,
                                    currentUI.getLocale()),
                            MailTemplatesAdminView.class);
                    }

                    if (SecurityUtils.isAccessGranted(SmsTemplatesAdminView.class)) {
                        menu.addNaviItem(notificationsSubMenu,
                            currentUI.getTranslation(AppConst.TITLE_SMS_TEMPLATES_ADMIN,
                                currentUI.getLocale()),
                            SmsTemplatesAdminView.class);
                    }

                    if (SecurityUtils.isAccessGranted(SmsAdminView.class)) {
                        menu.addNaviItem(notificationsSubMenu,
                            currentUI.getTranslation(AppConst.TITLE_SMS, currentUI.getLocale()),
                            SmsAdminView.class);
                    }

                    if (SecurityUtils.isAccessGranted(MailAdminView.class)) {
                        menu.addNaviItem(notificationsSubMenu,
                            currentUI.getTranslation(AppConst.TITLE_MAILS, currentUI.getLocale()),
                            MailAdminView.class);
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
                    NaviItem securitySubMenu = menu.addNaviItem(settingsMenu,
                        currentUI.getTranslation(AppConst.TITLE_SECURITY, currentUI.getLocale()),
                        null);

                    if (SecurityUtils.isAccessGranted(SecurityKeycloakUsersView.class)) {
                        menu.addNaviItem(securitySubMenu,
                            currentUI.getTranslation(AppConst.TITLE_SECURITY_USERS,
                                currentUI.getLocale()),
                            SecurityKeycloakUsersView.class);
                    }

                    if (SecurityUtils.isAccessGranted(SecurityKeycloakRolesView.class)) {
                        menu.addNaviItem(securitySubMenu,
                            currentUI.getTranslation(AppConst.TITLE_SECURITY_ROLES,
                                currentUI.getLocale()),
                            SecurityKeycloakRolesView.class);
                    }

                    if (SecurityUtils.isAccessGranted(SecurityKeycloakGroupsView.class)) {
                        menu.addNaviItem(securitySubMenu,
                            currentUI.getTranslation(AppConst.TITLE_SECURITY_GROUPS,
                                currentUI.getLocale()),
                            SecurityKeycloakGroupsView.class);
                    }

                    if (SecurityUtils.isAccessGranted(SessionView.class)) {
                        menu.addNaviItem(securitySubMenu,
                            currentUI.getTranslation(AppConst.TITLE_SESSIONS_ADMIN,
                                currentUI.getLocale()),
                            SessionView.class);
                    }

                    addToSecurityMenu(menu, securitySubMenu);
                }
                boolean isDisplayMonitoring =
                    SecurityUtils.isAccessGranted(EurekaView.class) ||
                        SecurityUtils.isAccessGranted(CloudConfigView.class);
                if (isDisplayMonitoring) {
                    NaviItem monitoringSubMenu = menu.addNaviItem(settingsMenu,
                        currentUI.getTranslation(AppConst.TITLE_MONITORING, currentUI.getLocale()),
                        null);

                    if (SecurityUtils.isAccessGranted(EurekaView.class)) {
                        menu.addNaviItem(monitoringSubMenu,
                            currentUI
                                .getTranslation(AppConst.TITLE_EUREKA_ADMIN, currentUI.getLocale()),
                            EurekaView.class);
                    }
                    if (SecurityUtils.isAccessGranted(CloudConfigView.class)) {
                        menu.addNaviItem(monitoringSubMenu,
                            currentUI.getTranslation(AppConst.TITLE_CLOUD_CONFIG_ADMIN,
                                currentUI.getLocale()),
                            CloudConfigView.class);
                    }
                }
            }
        }
    }

    /**
     * Configure the app's inner and outer headers and footers.
     */
    protected void initHeadersAndFooters() {
        //setAppHeaderOuter();
        //setAppFooterOuter();

        // setAppFooterInner();

        appBar = new AppBar();
        UIUtils.setTheme(Lumo.DARK, appBar);
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

        settings.addFavIcon("icon", "frontend/styles/favicons/favicon.ico",
            "256x256");
    }

    public NaviDrawer getNaviDrawer() {
        return naviDrawer;
    }

    public AppBar getAppBar() {
        return appBar;
    }

    public void displayInfoMessage(String message) {
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
    }

    public void displayErrorMessage(String message) {
        Icon icon = UIUtils.createIcon(IconSize.S, TextColor.ERROR, VaadinIcon.WARNING);
        Label label = UIUtils.createLabel(FontSize.XS, TextColor.ERROR, message);

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
    }


    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        NaviItem active = getActiveItem(event);
        if (active != null) {
            getAppBar().setTitle(active.getText());
        }
    }

    private NaviItem getActiveItem(AfterNavigationEvent e) {
        for (NaviItem item : naviDrawer.getMenu().getNaviItems()) {
            if (item.isHighlighted(e)) {
                return item;
            }
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
