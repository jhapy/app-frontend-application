package org.jhapy.frontend.client.notification;

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
public class NotificationServices {

  public static MailService getMailService() {
    return getApplicationContext().getBean(MailService.class);
  }

  public static SmsService getSmsService() {
    return getApplicationContext().getBean(SmsService.class);
  }

  public static MailTemplateService getMailTemplateService() {
    return getApplicationContext().getBean(MailTemplateService.class);
  }

  public static SmsTemplateService getSmsTemplateService() {
    return getApplicationContext().getBean(SmsTemplateService.class);
  }

  public static ApplicationContext getApplicationContext() {
    ServletContext servletContext = SpringServlet.getCurrent().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }
}
