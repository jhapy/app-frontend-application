package org.jhapy.frontend.exceptions;

import com.flowingcode.vaadin.addons.errorwindow.ErrorManager;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.ParentLayout;
import de.codecamp.vaadin.security.spring.access.rules.PermitAll;
import javax.servlet.http.HttpServletResponse;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.views.JHapyMainView3;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 08/05/2020
 */
@PermitAll
@ParentLayout(JHapyMainView3.class)
public class InternalServerErrorExceptionHandler extends ViewFrame implements HasLogger,
    HasErrorParameter<Exception> {

  //@Override
  public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
    String exceptionText;
    if (parameter.hasCustomMessage()) {
      exceptionText = String.format(
          "There was an exception while trying to navigate to '%s' with the exception message '%s'",
          event.getLocation().getPath(), parameter.getCustomMessage());
    } else {
      exceptionText = String.format("There was an exception while trying to navigate to '%s'",
          event.getLocation().getPath());
    }
    ErrorManager.showError(parameter.getException());

    setViewContent(new Label(exceptionText));

    logger().error("Internal Server Error : " + parameter.getException().getMessage(),
        parameter.getException());
    return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
  }
}
