package org.jhapy.frontend.components;

import com.vaadin.flow.component.html.Span;
import java.util.StringJoiner;
import org.jhapy.frontend.utils.UIUtils;
import org.jhapy.frontend.utils.css.lumo.BadgeColor;
import org.jhapy.frontend.utils.css.lumo.BadgeShape;
import org.jhapy.frontend.utils.css.lumo.BadgeSize;

public class Badge extends Span {

  public Badge(String text) {
    this(text, BadgeColor.NORMAL);
  }

  public Badge(String text, BadgeColor color) {
    super(text);
    UIUtils.setTheme(color.getThemeName(), this);
  }

  public Badge(String text, BadgeColor color, BadgeSize size,
      BadgeShape shape) {
    super(text);
    StringJoiner joiner = new StringJoiner(" ");
    joiner.add(color.getThemeName());

    if (shape.equals(BadgeShape.PILL)) {
      joiner.add(shape.getThemeName());
    }

    if (size.equals(BadgeSize.S)) {
      joiner.add(size.getThemeName());
    }
    UIUtils.setTheme(joiner.toString(), this);
  }

}
