package org.jhapy.frontend.client.security;

import com.vaadin.flow.spring.SpringServlet;
import javax.servlet.ServletContext;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-02
 */
@Service
public class SecurityServices {

  public static SecurityUserService getSecurityUserService() {
    return getApplicationContext().getBean(SecurityUserService.class);
  }

  public static SecurityRoleService getSecurityRoleService() {
    return getApplicationContext().getBean(SecurityRoleService.class);
  }

  public static ApplicationContext getApplicationContext() {
    ServletContext servletContext = SpringServlet.getCurrent().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }
}
