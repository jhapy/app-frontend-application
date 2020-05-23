package org.jhapy.frontend.views;

import com.vaadin.flow.spring.SpringServlet;
import javax.servlet.ServletContext;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.jhapy.frontend.views.login.LoginView;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-01
 */
@Component
public class ViewManager {

  public static ApplicationContext getApplicationContext() {
    ServletContext servletContext = SpringServlet.getCurrent().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }

  public static LoginView getLoginView() {
    return getApplicationContext().getBean(LoginView.class);
  }
}
