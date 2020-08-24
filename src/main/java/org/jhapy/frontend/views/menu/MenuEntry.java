package org.jhapy.frontend.views.menu;

import static org.jhapy.frontend.utils.AppConst.PAGE_LOGIN;
import static org.jhapy.frontend.utils.AppConst.PAGE_SECURITY_USERS;

import com.github.appreciated.app.layout.component.builder.interfaces.NavigationElement;
import com.github.appreciated.app.layout.navigation.UpNavigationHelper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLink;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.views.JHapyMainView2;
import org.jhapy.frontend.views.JHapyMainView3;
import org.jhapy.frontend.views.admin.security.SecurityUsersView;
import org.jhapy.frontend.views.login.LoginView;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 28/07/2020
 */
@Data
public class MenuEntry implements Serializable, HasLogger {

  public MenuEntry(String id) {
    this.id = id;
  }

  private String id;
  private VaadinIcon icon;
  private String title;
  private Class targetClass;
  private Object targetParams;
  private String targetId;
  private Object relatedObject;
  private MenuEntry parentMenuEntry;
  private Boolean canCreateSubEntries =Boolean.FALSE;
  private List<MenuAction> contextMenu;
  private Boolean hasChildNodes = Boolean.TRUE;

  public void setTargetClass(Class targetClass, Class routerLayout) {
    String loggerPrefix = getLoggerPrefix("setTargetClass");
    this.targetClass = targetClass;
    //UpNavigationHelper.registerNavigationRoute(targetClass);
    RouteConfiguration configuration = RouteConfiguration.forSessionScope();
/*
    String route = id;
    if ( ! configuration.isPathRegistered(route) )  {
      logger().debug(loggerPrefix+"Register Route : Path = " + route + ", Target = " + targetClass.getSimpleName());
      configuration.setRoute(route, targetClass, routerLayout);
    } else {
      //logger().warn(loggerPrefix+"Route already registered : Path = " + route + ", Target = " + targetClass.getSimpleName());
    }
 */
  }
}
