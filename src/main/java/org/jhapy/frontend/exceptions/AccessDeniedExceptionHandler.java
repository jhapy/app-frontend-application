package org.jhapy.frontend.exceptions;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.ParentLayout;
import javax.servlet.http.HttpServletResponse;
import org.jhapy.frontend.annotations.PublicView;
import org.jhapy.frontend.exceptions.AccessDeniedException;
import org.jhapy.frontend.layout.ViewFrame;
import org.jhapy.frontend.views.JHapyMainView3;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 08/05/2020
 */
@PublicView
@ParentLayout(JHapyMainView3.class)
public class AccessDeniedExceptionHandler extends ViewFrame implements
    HasErrorParameter<AccessDeniedException> {

  @Override
  public int setErrorParameter(BeforeEnterEvent event,
      ErrorParameter<AccessDeniedException> parameter) {
    setViewContent(new Label(
        "Tried to navigate to a view without "
            + "correct access rights"));
    return HttpServletResponse.SC_FORBIDDEN;
  }
}
