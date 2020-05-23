package org.jhapy.frontend.client.reference;

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
public class ReferenceServices {

  public static CountryService getCountryService() {
    return getApplicationContext().getBean(CountryService.class);
  }

  public static CountryTrlService getCountryTrlService() {
    return getApplicationContext().getBean(CountryTrlService.class);
  }

  public static IntermediateRegionService getIntermediateRegionService() {
    return getApplicationContext().getBean(IntermediateRegionService.class);
  }

  public static IntermediateRegionTrlService getIntermediateRegionTrlService() {
    return getApplicationContext().getBean(IntermediateRegionTrlService.class);
  }

  public static RegionService getRegionService() {
    return getApplicationContext().getBean(RegionService.class);
  }

  public static RegionTrlService getRegionTrlService() {
    return getApplicationContext().getBean(RegionTrlService.class);
  }

  public static SubRegionService getSubRegionService() {
    return getApplicationContext().getBean(SubRegionService.class);
  }

  public static SubRegionTrlService getSubRegionTrlService() {
    return getApplicationContext().getBean(SubRegionTrlService.class);
  }

  public static ApplicationContext getApplicationContext() {
    ServletContext servletContext = SpringServlet.getCurrent().getServletContext();
    return WebApplicationContextUtils.getWebApplicationContext(servletContext);
  }
}
