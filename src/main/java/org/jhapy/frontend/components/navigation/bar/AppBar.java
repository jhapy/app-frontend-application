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

package org.jhapy.frontend.components.navigation.bar;


import static org.jhapy.frontend.utils.AppConst.THEME_ATTRIBUTE;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Direction;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.Lumo;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.Cookie;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.serviceQuery.SearchQuery;
import org.jhapy.dto.serviceQuery.SearchQueryResult;
import org.jhapy.dto.utils.AppContext;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.tab.NaviTab;
import org.jhapy.frontend.components.navigation.tab.NaviTabs;
import org.jhapy.frontend.components.notification.DefaultNotificationHolder;
import org.jhapy.frontend.components.notification.component.NotificationButton;
import org.jhapy.frontend.components.notification.entity.DefaultNotification;
import org.jhapy.frontend.components.search.overlay.SearchOverlayButton;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.security.SecurityUtils2;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.i18n.MyI18NProvider;
import org.jhapy.frontend.views.JHapyMainView;
import org.jhapy.frontend.views.JHapyMainView3;

@CssImport("./styles/components/app-bar.css")
@SpringComponent
@UIScope
public class AppBar extends FlexBoxLayout implements LocaleChangeObserver, HasLogger {

  private static final Set<String> rtlSet;

  static {
    Set<String> lang = new HashSet<>();
    lang.add("ar"); // Arabic
    lang.add("dv"); // Divehi
    lang.add("fa"); // Persian
    lang.add("ha"); // Hausa
    lang.add("he"); // Hebrew
    lang.add("iw"); // Hebrew
    lang.add("ji"); // Yiddish
    lang.add("ps"); // Pushto
    lang.add("sd"); // Sindhi
    lang.add("ug"); // Uighur
    lang.add("ur"); // Urdu
    lang.add("yi"); // Yiddish

    rtlSet = Collections.unmodifiableSet(lang);
  }

  private final String CLASS_NAME = "app-bar";

  private FlexBoxLayout container;

  private Button menuIcon;
  private Button contextIcon;

  private H4 title;
  private FlexBoxLayout actionItems;
  private Image avatar;

  private FlexBoxLayout tabContainer;
  private NaviTabs tabs;
  private ArrayList<Registration> tabSelectionListeners;
  private Button addTab;

  private FlexBoxLayout searchArea;
  private TextField search;
  private Checkbox activeFilter;
  private ArrayList<Registration> searchValueChangedListeners;

  private NotificationButton notificationButton;
  private SearchOverlayButton<? extends SearchQueryResult, ? extends SearchQuery> searchButton;

  private DefaultNotificationHolder notifications;
  private Registration searchRegistration;
  private Registration searchEscRegistration;

  private MenuItem languageMenu;

  public AppBar() {
    setClassName(CLASS_NAME);
    initMenuIcon();
    initContextIcon();
    initNotification();
    initTitle("");
    initSearch();
    initSearch2();
    initAvatar();
    initActionItems();
    initContainer();
    initTabs();
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    if (!JHapyMainView3.get().hasGlobalSearch()) {
      searchButton.setVisible(false);
    }
    if (!JHapyMainView3.get().hasLanguageSelect()) {
      languageMenu.setVisible(false);
    }

    if (VaadinSession.getCurrent().getAttribute(THEME_ATTRIBUTE) != null) {
      ThemeList themeList = UI.getCurrent().getElement().getThemeList();
      themeList.add(Lumo.DARK);
    }
  }

  public void setNaviMode(NaviMode mode) {
    if (mode.equals(NaviMode.MENU)) {
      menuIcon.setVisible(true);
      contextIcon.setVisible(false);
    } else {
      menuIcon.setVisible(false);
      contextIcon.setVisible(true);
    }
  }

  private void initMenuIcon() {
    menuIcon = UIUtils.createTertiaryInlineButton(VaadinIcon.MENU);
    menuIcon.addClassName(CLASS_NAME + "__navi-icon");
    menuIcon.addClickListener(e -> JHapyMainView.get().getNaviDrawer().toggle());
    UIUtils.setAriaLabel("Menu", menuIcon);
    UIUtils.setLineHeight("1", menuIcon);
  }

  private void initContextIcon() {
    contextIcon = UIUtils
        .createTertiaryInlineButton(VaadinIcon.ARROW_LEFT);
    contextIcon.addClassNames(CLASS_NAME + "__context-icon");
    contextIcon.setVisible(false);
    UIUtils.setAriaLabel("Back", contextIcon);
    UIUtils.setLineHeight("1", contextIcon);
  }

  private void initTitle(String title) {
    this.title = new H4(title);
    this.title.setClassName(CLASS_NAME + "__title");
  }

  private void initNotification() {
    notifications = new DefaultNotificationHolder();
    notifications.addClickListener(
        notification -> {/* Use the listener to react on the click on the notification */});
    notificationButton = new NotificationButton<>(VaadinIcon.BELL, notifications);
  }

  public void addNotification(DefaultNotification defaultNotification) {
    notifications.add(defaultNotification);
  }

  private void initSearch() {
    search = new TextField();
    search.setPlaceholder("Search");
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    //search.setVisible(false);

    activeFilter = new Checkbox(getTranslation("action.search.showInactive"));
    activeFilter.setValue(false);
    searchValueChangedListeners = new ArrayList<>();

    searchArea = new FlexBoxLayout(search, activeFilter);
    searchArea.setVisible(false);
    searchArea.addClassName(CLASS_NAME + "__container");
    searchArea.setAlignItems(FlexComponent.Alignment.CENTER);
    searchArea.setSpacing(Right.S);
    searchArea.setFlexGrow(1, search);
  }

  private void initSearch2() {
    searchButton = new SearchOverlayButton<>();
  }

  private void resetSearchArea() {
    List<Component> toRemove = new ArrayList<>();
    for (int i = 0; i < searchArea.getComponentCount(); i++) {
      if (!searchArea.getComponentAt(i).equals(search) && !searchArea.getComponentAt(i)
          .equals(activeFilter)) {
        toRemove.add(searchArea.getComponentAt(i));
      }
    }
    searchArea.remove(toRemove.toArray(new Component[0]));
  }

  public void addSearchComponents(Component... components) {
    searchArea.add(components);
  }

  public void rebuildMenu() {
    container.remove(avatar);
    initAvatar();
    container.add(avatar);
  }

  private void initAvatar() {
    String loggerPrefix = getLoggerPrefix("initAvatar");
    avatar = new Image();
    avatar.setClassName(CLASS_NAME + "__avatar");
    avatar.setAlt("User menu");
    avatar.setSrc(UIUtils.IMG_PATH + "icons8-question-mark-64.png");

    if (SecurityUtils.isUserLoggedIn()) {
      StoredFile userAvatar = AppContext.getInstance().getCurrentAvatar();
      if (userAvatar != null) {
        StoredFile finalUserAvatar = userAvatar;
        avatar.setSrc(new StreamResource(userAvatar.getFilename(),
            () -> new ByteArrayInputStream(finalUserAvatar.getContent())));
      }

      Optional<String> currentUserLogin = SecurityUtils2.getCurrentUserLogin();

      ContextMenu contextMenu = new ContextMenu(avatar);
      contextMenu.setOpenOnClick(true);

      Button languageButton = UIUtils
          .createButton(getTranslation("action.settings.language"), VaadinIcon.GLOBE,
              ButtonVariant.LUMO_TERTIARY_INLINE);
      Locale currentLocale = UI.getCurrent().getSession().getLocale();
      logger().debug(loggerPrefix + "Current locale : " + currentLocale.getLanguage());
      languageMenu = contextMenu.addItem(languageButton);

      List<MenuItem> menuItems = new ArrayList();
      MyI18NProvider.getAvailableLanguagesInDB(getLocale()).forEach(locale -> {
        MenuItem menu = languageMenu.getSubMenu()
            .addItem(new Label(locale.getDisplayLanguage(getLocale())));
        menu.setCheckable(true);
        logger().debug(loggerPrefix + "Add locale : " + locale + " - " + locale.getLanguage());
        menu.setChecked(currentLocale.getLanguage().equals(locale.getLanguage()));
        menu.addClickListener(event -> {
          setLanguage(locale);
          menuItems.forEach(menuItem -> {
            if (!menuItem.equals(menu)) {
              menuItem.setChecked(false);
            }
          });
        });
        menuItems.add(menu);
      });
      /*
      MenuItem frMenu = languageMenu.getSubMenu().addItem(new Label("Francais"));
      MenuItem enMenu = languageMenu.getSubMenu().addItem(new Label("English"));
      MenuItem arMenu = languageMenu.getSubMenu().addItem(new Label("Arabic"));
      frMenu.addClickListener(event -> {
        enMenu.setChecked(false);
        arMenu.setChecked(false);
        setLanguage(Locale.FRENCH);
      });
      frMenu.setCheckable(true);
      frMenu.setChecked(currentLocale.equals(Locale.FRENCH));

      enMenu.addClickListener(event -> {
        frMenu.setChecked(false);
        arMenu.setChecked(false);
        setLanguage(Locale.ENGLISH);
      });
      enMenu.setCheckable(true);
      enMenu.setChecked(currentLocale.equals(Locale.ENGLISH));

      arMenu.addClickListener(event -> {
        enMenu.setChecked(false);
        frMenu.setChecked(false);
        setLanguage(new Locale("ar", "MA"));
      });
      arMenu.setCheckable(true);
      arMenu.setChecked(currentLocale.equals(new Locale("ar", "MA")));
*/

      Button settingsButton = UIUtils.createButton(currentUserLogin.get(), VaadinIcon.USER,
          ButtonVariant.LUMO_TERTIARY_INLINE);
      contextMenu.addItem(settingsButton, event -> {
        if (JHapyMainView3.get().getUserSettingsView() != null) {
          getUI().get().navigate(JHapyMainView3.get().getUserSettingsView());
        }
      });

      ThemeList themeList = UI.getCurrent().getElement().getThemeList();
      Button switchDarkThemeButton = UIUtils
          .createButton(getTranslation("action.global.darkTheme"), VaadinIcon.CIRCLE_THIN,
              ButtonVariant.LUMO_TERTIARY_INLINE);
      if (VaadinSession.getCurrent().getAttribute(THEME_ATTRIBUTE) != null) {
        switchDarkThemeButton.setIcon(VaadinIcon.CHECK_CIRCLE_O.create());
      }

      contextMenu.addItem(switchDarkThemeButton, event -> {
        if (themeList.contains(Lumo.DARK)) {
          themeList.remove(Lumo.DARK);
          switchDarkThemeButton.setIcon(VaadinIcon.CIRCLE_THIN.create());
          VaadinSession.getCurrent().setAttribute(THEME_ATTRIBUTE, null);
        } else {
          themeList.add(Lumo.DARK);
          switchDarkThemeButton.setIcon(VaadinIcon.CHECK_CIRCLE_O.create());
          VaadinSession.getCurrent().setAttribute(THEME_ATTRIBUTE, Lumo.DARK);
        }
      });
      Button exitButton = UIUtils
          .createButton(getTranslation("action.global.logout"), VaadinIcon.EXIT,
              ButtonVariant.LUMO_TERTIARY_INLINE);
      contextMenu.addItem(new Anchor("logout", exitButton));
    }
  }

  private void setLanguage(Locale language) {
    UI.getCurrent().getSession().setLocale(language);
    Cookie languageCookie = new Cookie("PreferredLanguage", language.getLanguage());
    languageCookie.setMaxAge(31449600);
    languageCookie.setPath("/");
    languageCookie.setSecure(true);
    VaadinService.getCurrentResponse().addCookie(languageCookie);

    UI.getCurrent().getPage().reload();
  }

  private void initActionItems() {
    actionItems = new FlexBoxLayout();
    actionItems.addClassName(CLASS_NAME + "__action-items");
    actionItems.setVisible(false);
  }

  private void initContainer() {
    container = new FlexBoxLayout(menuIcon, contextIcon, this.title, searchArea,
        actionItems, notificationButton, searchButton, avatar);
    container.addClassName(CLASS_NAME + "__container");
    container.setAlignItems(FlexComponent.Alignment.CENTER);
    container.setFlexGrow(1, searchArea);
    add(container);
  }

  private void initTabs(NaviTab... tabs) {
    addTab = UIUtils.createSmallButton(VaadinIcon.PLUS);
    addTab.addClickListener(e -> this.tabs
        .setSelectedTab(addClosableNaviTab("New Tab", JHapyMainView.get().getHomePage())));
    addTab.setVisible(false);

    this.tabs = tabs.length > 0 ? new NaviTabs(tabs) : new NaviTabs();
    this.tabs.setClassName(CLASS_NAME + "__tabs");
    this.tabs.setVisible(false);
    for (NaviTab tab : tabs) {
      configureTab(tab);
    }

    this.tabSelectionListeners = new ArrayList<>();
    tabContainer = new FlexBoxLayout(this.tabs, addTab);
    tabContainer.addClassName(CLASS_NAME + "__tab-container");
    tabContainer.setAlignItems(FlexComponent.Alignment.CENTER);
    add(tabContainer);
  }

  /* === MENU ICON === */
  public Button getMenuIcon() {
    return menuIcon;
  }

  /* === CONTEXT ICON === */
  public Button getContextIcon() {
    return contextIcon;
  }

  public void setContextIcon(Icon icon) {
    contextIcon.setIcon(icon);
  }

  public String getTitle() {
    return this.title.getText();
  }

  /* === TITLE === */

  public void setTitle(String title) {
    this.title.setText(title);
    UI.getCurrent().getPage().setTitle(title == null ? "<N/A>" : title);
  }

  public Component addActionItem(Component component) {
    if (component == null) {
      return null;
    }
    String loggerPrefix = getLoggerPrefix("addActionItem", System.identityHashCode(this));
    //logger().debug(loggerPrefix+"Add Action Item");
    actionItems.add(component);
    updateActionItemsVisibility();
    return component;
  }

  public void removeActionItem(Component component) {
    String loggerPrefix = getLoggerPrefix("removeActionItem", System.identityHashCode(this));
    //logger().debug(loggerPrefix+"Remove Action Item");
    actionItems.remove(component);
    updateActionItemsVisibility();
  }

  /* === ACTION ITEMS === */

  public Button addActionItem(VaadinIcon icon) {
    Button button = UIUtils.createButton(icon, ButtonVariant.LUMO_SMALL,
        ButtonVariant.LUMO_TERTIARY);
    addActionItem(button);
    return button;
  }

  public void removeAllActionItems() {
    actionItems.removeAll();
    updateActionItemsVisibility();
  }

  public boolean hasActionItems() {
    return actionItems.getComponentCount() > 0;
  }

  public Image getAvatar() {
    return avatar;
  }

  /* === AVATAR == */

  /* === TABS === */
  public void centerTabs() {
    tabs.addClassName(LumoStyles.Margin.Horizontal.AUTO);
  }

  public void dispatchTabs() {
    tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);
  }

  private void configureTab(Tab tab) {
    tab.addClassName(CLASS_NAME + "__tab");
    updateTabsVisibility();
  }

  public Tab addTab(String text) {
    Tab tab = tabs.addTab(text);
    configureTab(tab);
    return tab;
  }

  public Tab addTab(String text,
      Class<? extends Component> navigationTarget) {
    Tab tab = tabs.addTab(text, navigationTarget);
    configureTab(tab);
    return tab;
  }

  public Tab addClosableNaviTab(String text,
      Class<? extends Component> navigationTarget) {
    Tab tab = tabs.addClosableTab(text, navigationTarget);
    configureTab(tab);
    return tab;
  }

  public Tab getSelectedTab() {
    return tabs.getSelectedTab();
  }

  public void setSelectedTab(Tab selectedTab) {
    tabs.setSelectedTab(selectedTab);
  }

  public void setSelectedTabIndex(int selectedTabIdex) {
    tabs.setSelectedIndex(selectedTabIdex);
  }

  public void updateSelectedTab(String text,
      Class<? extends Component> navigationTarget) {
    tabs.updateSelectedTab(text, navigationTarget);
  }

  public void navigateToSelectedTab() {
    tabs.navigateToSelectedTab();
  }

  public void addTabSelectionListener(
      ComponentEventListener<Tabs.SelectedChangeEvent> listener) {
    Registration registration = tabs.addSelectedChangeListener(listener);
    tabSelectionListeners.add(registration);
  }

  public int getTabCount() {
    return tabs.getTabCount();
  }

  public void removeAllTabs() {
    tabSelectionListeners.forEach(registration -> registration.remove());
    tabSelectionListeners.clear();
    tabs.removeAll();
    updateTabsVisibility();
  }

  /* === ADD TAB BUTTON === */
  public void setAddTabVisible(boolean visible) {
    addTab.setVisible(visible);
  }

  /* === SEARCH === */
  public void searchModeOn() {
    menuIcon.setVisible(false);
    title.setVisible(false);
    actionItems.setVisible(false);
    tabContainer.setVisible(false);

    contextIcon.setIcon(new Icon(VaadinIcon.ARROW_BACKWARD));
    contextIcon.setVisible(true);
    searchRegistration = contextIcon
        .addClickListener(e -> searchModeOff());

    searchArea.setVisible(true);
    searchEscRegistration = search.getElement().addEventListener("keypress",
        event -> {
          searchModeOff();
        })
        .setFilter("event.key == 'Escape'");
    search.focus();
  }

  public void addSearchListener(HasValue.ValueChangeListener listener) {
    searchValueChangedListeners.add(search.addValueChangeListener(listener));
    searchValueChangedListeners.add(activeFilter.addValueChangeListener(listener));
  }

  public void setSearchPlaceholder(String placeholder) {
    search.setPlaceholder(placeholder);
  }

  private void searchModeOff() {
    menuIcon.setVisible(true);
    title.setVisible(true);
    tabContainer.setVisible(true);

    updateActionItemsVisibility();
    updateTabsVisibility();

    contextIcon.setVisible(false);
    if (searchRegistration != null) {
      try {
        searchRegistration.remove();
      } catch (IllegalArgumentException ignored) {
      }
    }

    if (searchEscRegistration != null) {
      try {
        searchEscRegistration.remove();
      } catch (IllegalArgumentException ignored) {
      }
    }

    for (int i = 0; i < searchArea.getComponentCount(); i++) {
      Component c = searchArea.getComponentAt(i);
      if (c instanceof HasValue) {
        ((HasValue) c).clear();
      }
    }
    searchArea.setVisible(false);
  }

  /* === RESET === */
  public void reset() {
    title.setText("");
    setNaviMode(AppBar.NaviMode.MENU);
    removeAllActionItems();
    removeAllTabs();
    resetSearchArea();
    searchModeOff();
    if (searchValueChangedListeners != null) {
      searchValueChangedListeners.forEach(Registration::remove);
      searchValueChangedListeners.clear();
    }

  }

  /* === UPDATE VISIBILITY === */
  private void updateActionItemsVisibility() {
    actionItems.setVisible(actionItems.getComponentCount() > 0);
  }

  private void updateTabsVisibility() {
    tabs.setVisible(tabs.getComponentCount() > 0);
  }

  public String getSearchString() {
    return search.getValue();
  }

  public Boolean getSearchShowActive() {
    return activeFilter.getValue();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    if (rtlSet.contains(event.getLocale().getLanguage())) {
      UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT);
    } else {
      UI.getCurrent().setDirection(Direction.LEFT_TO_RIGHT);
    }
  }

  public SearchOverlayButton getSearchButton() {
    if (searchButton == null) {
      searchButton = new SearchOverlayButton<>();
      addActionItem(searchButton);
    }
    return searchButton;
  }

  public void disableGlobalSearch() {
    searchButton.setVisible(false);
  }


  public enum NaviMode {
    MENU, CONTEXTUAL
  }

}
