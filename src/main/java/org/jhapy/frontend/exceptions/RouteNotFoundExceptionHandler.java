package org.jhapy.frontend.exceptions;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.ParentLayout;
import de.codecamp.vaadin.security.spring.access.rules.PermitAll;
import javax.servlet.http.HttpServletResponse;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.views.JHapyMainView3;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 08/05/2020
 */
@PermitAll
@ParentLayout(JHapyMainView3.class)
public class RouteNotFoundExceptionHandler extends ViewFrame implements
    HasErrorParameter<NotFoundException> {

  @Override
  public int setErrorParameter(BeforeEnterEvent event,
      ErrorParameter<NotFoundException> parameter) {
    setViewContent(new Label("Could not navigate to '"
        + event.getLocation().getPath()
        + "'"));
    return HttpServletResponse.SC_NOT_FOUND;
  }
}
