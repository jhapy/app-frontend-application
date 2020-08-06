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

package org.jhapy.frontend.components.navigation.drawer;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.QueryParameters;
import elemental.json.JsonObject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.Display;
import org.jhapy.frontend.utils.css.Overflow;
import org.jhapy.frontend.views.menu.MenuData;
import org.jhapy.frontend.views.menu.MenuEntry;
import org.vaadin.tatu.Tree;

@CssImport("./styles/components/navi-drawer.css")
@JsModule("./swipe-away.js")
public class NaviDrawerWithTreeMenu extends Div
    implements AfterNavigationObserver, HasLogger {

  private final String CLASS_NAME = "navi-drawer";
  private final String RAIL = "rail";
  private final String OPEN = "open";

  private Div scrim;

  private Div mainContent;
  private TextField search;
  private Div scrollableArea;

  private Button railButton;
  private Tree<MenuEntry> menu;
  private MenuData menuData;

  public NaviDrawerWithTreeMenu(boolean showSearchMenu, String version, String environement) {
    setClassName(CLASS_NAME);

    initScrim();
    initMainContent();

    initHeader();
    if (showSearchMenu) {
      initSearch();
    }

    initScrollableArea();
    initMenu();

    initFooter(version, environement);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    UI ui = attachEvent.getUI();
    ui.getPage().executeJavaScript("window.addSwipeAway($0,$1,$2,$3)",
        mainContent.getElement(), this, "onSwipeAway",
        scrim.getElement());
  }

  @ClientCallable
  public void onSwipeAway(JsonObject data) {
    close();
  }

  private void initScrim() {
    // Backdrop on small viewports
    scrim = new Div();
    scrim.addClassName(CLASS_NAME + "__scrim");
    scrim.addClickListener(event -> close());
    add(scrim);
  }

  private void initMainContent() {
    mainContent = new Div();
    mainContent.addClassName(CLASS_NAME + "__content");
    add(mainContent);
  }

  private void initHeader() {
    mainContent.add(new BrandExpression(getTranslation("element.application.title")));
  }

  private void initSearch() {
    search = new TextField();
    search.addValueChangeListener(e -> menu.getDataProvider().withConvertedFilter(
        filterValue -> item -> item.getTitle().startsWith( e.getValue())) );
    search.setClearButtonVisible(true);
    search.setPlaceholder("Search");
    search.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    mainContent.add(search);

    search.setVisible(SecurityUtils.isUserLoggedIn());
  }

  public void toogleSearch() {
    if (search != null) {
      search.setVisible(SecurityUtils.isUserLoggedIn());
    }
  }

  private void initScrollableArea() {
    scrollableArea = new Div();
    scrollableArea.addClassName(CLASS_NAME + "__scroll-area");
    mainContent.add(scrollableArea);
  }

  public void refreshMenu() {
    menu.setItems(menuData.getRootItems(),
        menuData::getChildItems);
  }

  public void addMenuEntry( MenuEntry menuEntry ) {
    menuData.addMenuEntry( menuEntry );
  }

  private void initMenu() {
    menuData = new MenuData();
    menu = new Tree<>(MenuEntry::getTitle);
    menu.setSizeFull();
    menu.setHeightByRows(true);
    menu.setMinWidth("400px");
    menu.setHeightByRows(true);
    menu.setId("treegridbasic");
    menu.getStyle().set("font-size", "0.8em");

    menu.setItemIconProvider(item -> item.getIcon());
    menu.setItemTitleProvider(MenuEntry::getTitle);
    menu.addItemClickListener(event -> {
      if (event.getItem() != null && event.getItem().getTargetClass() != null) {
        navigate( event.getItem());
      };
    });
    menu.addExpandListener(menuEntryTreeGridExpandEvent -> {
      Collection<MenuEntry> items = menuEntryTreeGridExpandEvent.getItems();
      if ( ! items.isEmpty()) {
        MenuEntry item = items.iterator().next();
        menu.select(item);
        if (item.getTargetClass() != null)
          navigate(item);
      }
    });
    menu.addCollapseListener(menuEntryTreeGridCollapseEvent -> {
      Collection<MenuEntry> items = menuEntryTreeGridCollapseEvent.getItems();
      if ( ! items.isEmpty()) {
        MenuEntry item = items.iterator().next();
        menu.select(item);
        if (item.getTargetClass() != null)
          navigate(item);
      }
    });

    GridContextMenu<MenuEntry> contextMenu=  menu.addContextMenu();
    contextMenu.setDynamicContentHandler(menuEntry -> {
      if (menuEntry == null ) {
        return false;
      }
      if (menuEntry.getContextMenu() == null )
        return false;

      contextMenu.removeAll();

      menuEntry.getContextMenu().forEach( menuItem -> {
        if (menuItem.getClickListener() == null) {
          if (menuItem.getComponent() != null) {
            contextMenu.add(menuItem.getComponent());
          } else {
            contextMenu.add(menuItem.getTitle());
          }
        } else {
          if (menuItem.getComponent() != null) {
            contextMenu.addItem(menuItem.getComponent(), menu -> menuItem.getClickListener().accept(menu));
          } else {
            contextMenu.addItem(menuItem.getTitle(), menu -> menuItem.getClickListener().accept(menu));
          }
        }
      } );
      return  true;
    });
    scrollableArea.add(menu);
  }

  private void navigate( MenuEntry menuEntry ) {
    String loggerPrefix = getLoggerPrefix("navigate", menuEntry);

    if ( menuEntry.getTargetParams() != null ) {
      logger().debug(loggerPrefix+"Navigate with Target Params " + menuEntry.getTargetParams());
      UI.getCurrent().navigate(menuEntry.getTargetClass(),  menuEntry.getTargetParams().toString() );
    } else
    if ( menuEntry.getTargetId() != null ) {
      logger().debug(loggerPrefix+"Navigate with Target ID");
      String targetId = menuEntry.getTargetId();

        UI.getCurrent().navigate(menuEntry.getTargetClass(), menuEntry.getTargetId());
    }
    else {
      logger().debug(loggerPrefix+"Navigate without Target Params or Target ID");
      UI.getCurrent().navigate(menuEntry.getTargetClass());
    }
  }

  private void initFooter(String version, String environement) {
    if (StringUtils.isNotBlank(version)) {
      Label l = UIUtils.createH5Label("Version " + version + " (" + environement + ")");
      l.addClassName(CLASS_NAME + "__footer");
      l.addClassName("version");
      mainContent.add(l);
    }

    railButton = UIUtils.createSmallButton("Collapse",
        VaadinIcon.CHEVRON_LEFT_SMALL);
    railButton.addClassName(CLASS_NAME + "__footer");
    railButton.addClickListener(event -> toggleRailMode());
    railButton.getElement().setAttribute("aria-label", "Collapse menu");
    mainContent.add(railButton);
  }

  private void toggleRailMode() {
    if (getElement().hasAttribute(RAIL)) {
      getElement().setAttribute(RAIL, false);
      railButton.setIcon(new Icon(VaadinIcon.CHEVRON_LEFT_SMALL));
      railButton.setText("Collapse");
      UIUtils.setAriaLabel("Collapse menu", railButton);
    } else {
      getElement().setAttribute(RAIL, true);
      railButton.setIcon(new Icon(VaadinIcon.CHEVRON_RIGHT_SMALL));
      railButton.setText("Expand");
      UIUtils.setAriaLabel("Expand menu", railButton);
      getUI().get().getPage().executeJs(
          "var originalStyle = getComputedStyle($0).pointerEvents;" //
              + "$0.style.pointerEvents='none';" //
              + "setTimeout(function() {$0.style.pointerEvents=originalStyle;}, 170);",
          getElement());
    }
  }

  public void toggle() {
    if (getElement().hasAttribute(OPEN)) {
      close();
    } else {
      open();
    }
  }

  private void open() {
    getElement().setAttribute(OPEN, true);
  }

  private void close() {
    getElement().setAttribute(OPEN, false);
    applyIOS122Workaround();
  }

  private void applyIOS122Workaround() {
    // iOS 12.2 sometimes fails to animate the menu away.
    // It should be gone after 240ms
    // This will make sure it disappears even when the browser fails.
    getUI().ifPresent(ui -> ui.getPage().executeJs(
        "var originalStyle = getComputedStyle($0).transitionProperty;" //
            + "setTimeout(function() {$0.style.transitionProperty='padding'; requestAnimationFrame(function() {$0.style.transitionProperty=originalStyle})}, 250);",
        mainContent.getElement()));
  }

  public MenuData getFreshMenu() {
    menuData = new MenuData();
    return menuData;
  }

  public MenuData getMenu() {
    return menuData;
  }

  public Tree<MenuEntry> getMenuComponent() {
    return menu;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
    close();
  }

}