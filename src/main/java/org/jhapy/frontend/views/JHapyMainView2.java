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

import static com.github.appreciated.app.layout.entity.Section.HEADER;

import com.github.appreciated.app.layout.addons.notification.DefaultNotificationHolder;
import com.github.appreciated.app.layout.addons.notification.component.NotificationButton;
import com.github.appreciated.app.layout.addons.profile.ProfileButton;
import com.github.appreciated.app.layout.addons.profile.builder.AppBarProfileButtonBuilder;
import com.github.appreciated.app.layout.addons.search.SearchButton;
import com.github.appreciated.app.layout.component.appbar.AppBarBuilder;
import com.github.appreciated.app.layout.component.applayout.LeftLayouts;
import com.github.appreciated.app.layout.component.applayout.LeftLayouts.LeftResponsive;
import com.github.appreciated.app.layout.component.builder.AppLayoutBuilder;
import com.github.appreciated.app.layout.component.menu.left.builder.LeftAppMenuBuilder;
import com.github.appreciated.app.layout.component.menu.left.items.LeftHeaderItem;
import com.github.appreciated.app.layout.component.router.AppLayoutRouterLayout;
import com.github.appreciated.app.layout.entity.DefaultBadgeHolder;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.server.VaadinSession;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.components.AppCookieConsent;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.bar.AppBar;
import org.jhapy.frontend.components.navigation.drawer.NaviDrawer;
import org.jhapy.frontend.components.navigation.drawer.NaviItem;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.utils.AppConst;
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
@Push
public abstract class JHapyMainView2 extends AppLayoutRouterLayout<LeftResponsive>
    implements RouterLayout, PageConfigurator, AfterNavigationObserver, HasLogger {

    private final DefaultNotificationHolder notifications = new DefaultNotificationHolder();
    private final DefaultBadgeHolder badge = new DefaultBadgeHolder(5);
    private final MenuData menuData = new MenuData();
    private final Tree<MenuEntry> menuTree;
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

    private final AppLayoutBuilder<LeftResponsive> appLayoutBuilder;

    public JHapyMainView2(Environment environment) {
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

        SearchButton searchButton = new SearchButton().withValueChangeListener(event -> {
            /* React manually to user inputs */
        });

        ProfileButton profileButton = AppBarProfileButtonBuilder.get()
            .withItem("ProfileButton Entry 1", event -> Notification.show("Profile clicked"))
            .withItem("ProfileButton Entry 2", event -> Notification.show("Profile clicked"))
            .withItem("ProfileButton Entry 3", event -> Notification.show("Profile clicked"))
            .build();

        appLayoutBuilder = AppLayoutBuilder.get(LeftLayouts.LeftResponsive.class)
            .withTitle(getTranslation("element.application.title"))
            .withAppBar(AppBarBuilder.get()
                .add(new NotificationButton<>(VaadinIcon.BELL, notifications))
                .add(profileButton)
                .add(searchButton)
                .build())
            .withAppMenu(LeftAppMenuBuilder.get()
                .addToSection(HEADER,
                    new LeftHeaderItem(getTranslation("element.menu.title"),
                        environment.getProperty("APP_VERSION") + " " + environment
                            .getProperty("info.tags.environment"), "/frontend/images/logo.png")
                )
                .add(menuTree = getNavigationMenu())
                .build());

        init(appLayoutBuilder.build());

        // Configure the headers and footers (optional)
        //initHeadersAndFooters();

        getElement().appendChild(new AppCookieConsent().getElement());
    }

    public static JHapyMainView2 get() {
        return (JHapyMainView2) UI.getCurrent().getChildren()
            .filter(component -> RouterLayout.class.isAssignableFrom(component.getClass()))
            .findFirst().orElse(null);
    }

    class TestSearchResult {

        private final String header;
        private final String description;

        public TestSearchResult(String header, String description) {
            this.header = header;
            this.description = description;
        }

        public String getHeader() {
            return header;
        }

        public String getDescription() {
            return description;
        }
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

    public void rebuildNaviItems() {
        rebuildNaviItems(true);
    }

    public void rebuildNaviItems(boolean resetAppBar) {
        naviDrawer.getMenu().removeAll();
        naviDrawer.toogleSearch();
        //initNaviItems();

        if (resetAppBar) {
            appBar.reset();
            appBar.rebuildMenu();
        }
    }

    protected void addToMainMenu(MenuData menuData) {
    }

    protected void addToSettingsMenu(MenuEntry settingMenu) {
    }

    protected boolean hasSettingsMenuEntries() {
        return false;
    }

    protected void addToReferencesMenu(MenuEntry referenceMenu) {
    }

    protected boolean hasReferencesMenuEntries() {
        return false;
    }

    protected void addToSecurityMenu(MenuEntry securityMenu) {
    }

    protected boolean hasSecurityMenuEntries() {
        return false;
    }

    /**
     * Initialise the navigation items.
     */
    private Tree<MenuEntry> getNavigationMenu() {
        UI currentUI = UI.getCurrent();

        Tree<MenuEntry> tree = new Tree<>(MenuEntry::getTitle);
        tree.setRowsDraggable(true);
        SecurityUser currentUser = SecurityUtils.getSecurityUser();

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

                addToSettingsMenu(settingsSubMenu);

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

                        menuData.addMenuEntry(subMenu);
                    }

                    if (SecurityUtils.isAccessGranted(ElementsView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_ELEMENTS);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_ELEMENTS));
                        subMenu.setTargetClass(ElementsView.class);
                        subMenu.setParentMenuEntry(i18nSubmenu);

                        menuData.addMenuEntry(subMenu);
                    }

                    if (SecurityUtils.isAccessGranted(MessagesView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_MESSAGES);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_MESSAGES));
                        subMenu.setTargetClass(MessagesView.class);
                        subMenu.setParentMenuEntry(i18nSubmenu);

                        menuData.addMenuEntry(subMenu);
                    }
                }

                /*
                 * Reference
                 */
                boolean isReferenceMenuDisplay =
                    hasReferencesMenuEntries() || SecurityUtils
                        .isAccessGranted(CountriesView.class);

                if (isReferenceMenuDisplay) {
                    MenuEntry referenceSubMenu = new MenuEntry(AppConst.PAGE_REFERENCES);
                    referenceSubMenu.setVaadinIcon(VaadinIcon.SITEMAP);
                    referenceSubMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_REFERENCES));
                    referenceSubMenu.setParentMenuEntry(settingsSubMenu);

                    menuData.addMenuEntry(referenceSubMenu);

                    boolean isDisplayReference = SecurityUtils.isAccessGranted(CountriesView.class);

                    if (isDisplayReference) {

                        if (SecurityUtils.isAccessGranted(CountriesView.class)) {
                            MenuEntry subMenu = new MenuEntry(AppConst.PAGE_COUNTRIES);
                            subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                            subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_COUNTRIES));
                            subMenu.setTargetClass(CountriesView.class);
                            subMenu.setParentMenuEntry(referenceSubMenu);

                            menuData.addMenuEntry(subMenu);
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
                    MenuEntry notificationsSubMenu = new MenuEntry(AppConst.PAGE_NOTIFICATIONS);
                    notificationsSubMenu.setVaadinIcon(VaadinIcon.SITEMAP);
                    notificationsSubMenu
                        .setTitle(currentUI.getTranslation(AppConst.TITLE_NOTIFICATION_ADMIN));
                    notificationsSubMenu.setParentMenuEntry(settingsSubMenu);

                    menuData.addMenuEntry(notificationsSubMenu);

                    if (SecurityUtils.isAccessGranted(MailTemplatesAdminView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_MAIL_TEMPLATES_ADMIN);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu.setTitle(
                            currentUI.getTranslation(AppConst.TITLE_MAIL_TEMPLATES_ADMIN));
                        subMenu.setTargetClass(MailTemplatesAdminView.class);
                        subMenu.setParentMenuEntry(notificationsSubMenu);

                        menuData.addMenuEntry(subMenu);
                    }

                    if (SecurityUtils.isAccessGranted(SmsTemplatesAdminView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_SMS_TEMPLATES_ADMIN);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu
                            .setTitle(currentUI.getTranslation(AppConst.TITLE_SMS_TEMPLATES_ADMIN));
                        subMenu.setTargetClass(SmsTemplatesAdminView.class);
                        subMenu.setParentMenuEntry(notificationsSubMenu);

                        menuData.addMenuEntry(subMenu);
                    }

                    if (SecurityUtils.isAccessGranted(SmsAdminView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_SMS_ADMIN);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SMS));
                        subMenu.setTargetClass(SmsAdminView.class);
                        subMenu.setParentMenuEntry(notificationsSubMenu);

                        menuData.addMenuEntry(subMenu);
                    }

                    if (SecurityUtils.isAccessGranted(MailAdminView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_MAILS_ADMIN);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_MAILS));
                        subMenu.setTargetClass(MailAdminView.class);
                        subMenu.setParentMenuEntry(notificationsSubMenu);

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
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_USERS);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SECURITY_USERS));
                        subMenu.setTargetClass(SecurityKeycloakUsersView.class);
                        subMenu.setParentMenuEntry(securitySubMenu);

                        menuData.addMenuEntry(subMenu);
                    }

                    if (SecurityUtils.isAccessGranted(SecurityKeycloakRolesView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_ROLES);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SECURITY_ROLES));
                        subMenu.setTargetClass(SecurityKeycloakRolesView.class);
                        subMenu.setParentMenuEntry(securitySubMenu);

                        menuData.addMenuEntry(subMenu);
                    }

                    if (SecurityUtils.isAccessGranted(SecurityKeycloakGroupsView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_GROUPS);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SECURITY_GROUPS));
                        subMenu.setTargetClass(SecurityKeycloakGroupsView.class);
                        subMenu.setParentMenuEntry(securitySubMenu);

                        menuData.addMenuEntry(subMenu);
                    }

                    if (SecurityUtils.isAccessGranted(SessionView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_SESSIONS);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu.setTitle(currentUI.getTranslation(AppConst.TITLE_SESSIONS_ADMIN));
                        subMenu.setTargetClass(SessionView.class);
                        subMenu.setParentMenuEntry(securitySubMenu);

                        menuData.addMenuEntry(subMenu);
                    }

                    addToSecurityMenu(securitySubMenu);
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

                        menuData.addMenuEntry(subMenu);
                    }
                    if (SecurityUtils.isAccessGranted(CloudConfigView.class)) {
                        MenuEntry subMenu = new MenuEntry(AppConst.PAGE_CLOUD_CONFIG_ADMIN);
                        subMenu.setVaadinIcon(VaadinIcon.QUESTION);
                        subMenu
                            .setTitle(currentUI.getTranslation(AppConst.TITLE_CLOUD_CONFIG_ADMIN));
                        subMenu.setTargetClass(CloudConfigView.class);
                        subMenu.setParentMenuEntry(monitoringSubMenu);

                        menuData.addMenuEntry(subMenu);
                    }
                }
            }
        }

        tree.setItems(menuData.getRootItems(),
            menuData::getChildItems);

        tree.setItemIconProvider(item -> item.getVaadinIcon());
        tree.setItemTitleProvider(MenuEntry::getTitle);

        tree.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null && event.getValue().getTargetClass() != null) {
                UI.getCurrent().navigate(event.getValue().getTargetClass());
            }
        });
        tree.setHeightByRows(true);
        tree.setSizeFull();
        tree.setId("treegridbasic");
        return tree;
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
    }

    public void displayErrorMessage(String message) {
    }

    public void setTitle(String title) {
        appLayoutBuilder.setTitle(title);
        appLayoutBuilder.build();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (!menuTree.getSelectedItems().isEmpty()) {
            setTitle(menuTree.getSelectedItems().iterator().next().getTitle());
        }

/*    NaviItem active = getActiveItem(event);
    if (active != null) {
      getAppBar().setTitle(active.getText());
    }

 */
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
}
