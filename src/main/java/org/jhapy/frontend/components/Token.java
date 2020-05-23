package org.jhapy.frontend.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import org.jhapy.frontend.layout.size.Left;
import org.jhapy.frontend.layout.size.Right;
import org.jhapy.frontend.utils.FontSize;
import org.jhapy.frontend.utils.LumoStyles;
import org.jhapy.frontend.utils.TextColor;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.BorderRadius;
import org.jhapy.frontend.utils.css.Display;

public class Token extends FlexBoxLayout {

  private final String CLASS_NAME = "token";

  public Token(String text) {
    setAlignItems(FlexComponent.Alignment.CENTER);
    setBackgroundColor(LumoStyles.Color.Primary._10);
    setBorderRadius(BorderRadius.M);
    setClassName(CLASS_NAME);
    setDisplay(Display.INLINE_FLEX);
    setPadding(Left.S, Right.XS);
    setSpacing(Right.XS);

    Label label = UIUtils.createLabel(FontSize.S, TextColor.BODY, text);
    Button button = UIUtils.createButton(VaadinIcon.CLOSE_SMALL,
        ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY_INLINE);
    add(label, button);
  }

}
