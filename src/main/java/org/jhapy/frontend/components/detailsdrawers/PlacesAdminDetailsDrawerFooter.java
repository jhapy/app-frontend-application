package org.jhapy.frontend.components.detailsdrawers;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.shared.Registration;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.layout.size.Horizontal;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.layout.size.Vertical;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.UIUtils;

public class PlacesAdminDetailsDrawerFooter extends FlexBoxLayout {

  private final Button create;
  private final Button cancel;

  public PlacesAdminDetailsDrawerFooter() {
    setBackgroundColor(LumoStyles.Color.Contrast._5);
    setPadding(Horizontal.RESPONSIVE_L, Vertical.S);
    setSpacing(Right.S);
    setWidthFull();

    create = UIUtils.createPrimaryButton(getTranslation("action.global.create"));
    cancel = UIUtils.createTertiaryButton(getTranslation("action.global.cancel"));

    add(create, cancel);
  }

  public Registration addCreateListener(
      ComponentEventListener<ClickEvent<Button>> listener) {
    return create.addClickListener(listener);
  }

  public Registration addCancelListener(
      ComponentEventListener<ClickEvent<Button>> listener) {
    return cancel.addClickListener(listener);
  }

  public void setCreateButtonVisible(boolean isVisible) {
    create.setVisible(isVisible);
  }

  public void setCancelButtonVisible(boolean isVisible) {
    cancel.setVisible(isVisible);
  }
}
