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


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeTypeException;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.domain.user.BaseUser;
import org.jhapy.dto.utils.AppContext;
import org.jhapy.dto.utils.StoredFile;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.components.navigation.tab.NaviTab;
import org.jhapy.frontend.components.navigation.tab.NaviTabs;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.security.SecurityUtils2;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.views.JHapyMainView;

@CssImport("./styles/components/app-bar.css")
public class AppBar extends FlexBoxLayout {

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
  private ArrayList<Registration> searchValueChangedListeners;

  private Registration searchRegistration;
  private Registration searchEscRegistration;

  public AppBar(String title, NaviTab... tabs) {
    setClassName(CLASS_NAME);
    initMenuIcon();
    initContextIcon();
    initTitle(title);
    initSearch();
    initAvatar();
    initActionItems();
    initContainer();
    initTabs(tabs);
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

  private void initSearch() {
    search = new TextField();
    search.setPlaceholder("Search");
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    //search.setVisible(false);

    searchValueChangedListeners = new ArrayList<>();

    searchArea = new FlexBoxLayout(search);
    searchArea.addClassName(CLASS_NAME + "__container");
    searchArea.setAlignItems(FlexComponent.Alignment.CENTER);
    searchArea.setSpacing(Right.S);
    searchArea.setFlexGrow(1, search);
  }

  private void resetSearchArea() {
    List<Component> toRemove = new ArrayList<>();
    for (int i = 0; i < searchArea.getComponentCount(); i++) {
      if (!searchArea.getComponentAt(i).equals(search)) {
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
      //contextMenu.addItem(currentUserLogin.orElse("N/A"));
      Anchor userSettingsAnchor = new Anchor("http://onlineplantumleditor-keycloak:9080/auth/realms/onlinePlantUmlEditor/account",currentUserLogin.get());
      userSettingsAnchor.setTarget("_blank");
      contextMenu.add(userSettingsAnchor);

      contextMenu.addItem(new Anchor("logout", "Log Out"));
    }
  }

  private void initActionItems() {
    actionItems = new FlexBoxLayout();
    actionItems.addClassName(CLASS_NAME + "__action-items");
    actionItems.setVisible(false);
  }

  private void initContainer() {
    container = new FlexBoxLayout(menuIcon, contextIcon, this.title, searchArea,
        actionItems, avatar);
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
    actionItems.add(component);
    updateActionItemsVisibility();
    return component;
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


  public enum NaviMode {
    MENU, CONTEXTUAL
  }

}
