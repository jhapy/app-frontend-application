package org.jhapy.frontend.client.i18n;

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
public class I18NServices {

  public static I18NService getI18NService() {
    return getApplicationContext().getBean(I18NService.class);
  }

  public static ActionService getActionService() {
    return getApplicationContext().getBean(ActionService.class);
  }

  public static ActionTrlService getActionTrlService() {
    return getApplicationContext().getBean(ActionTrlService.class);
  }

  public static ElementService getElementService() {
    return getApplicationContext().getBean(ElementService.class);
  }

  public static ElementTrlService getElementTrlService() {
    return getApplicationContext().getBean(ElementTrlService.class);
  }

  public static MessageService getMessageService() {
    return getApplicationContext().getBean(MessageService.class);
  }

  public static MessageTrlService getMessageTrlService() {
    return getApplicationContext().getBean(MessageTrlService.class);
  }

  public static ApplicationContext getApplicationContext() {
    ServletContext servletContext = SpringServlet.getCurrent().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }
}
