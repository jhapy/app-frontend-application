package org.jhapy.frontend.views;

import com.vaadin.flow.component.Component;
import org.jhapy.frontend.components.FlexBoxLayout;
import org.jhapy.frontend.layout.size.Bottom;
import org.jhapy.frontend.utils.css.Overflow;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 10/09/2020
 */
public class DefaultDetailsContent extends FlexBoxLayout {

  public DefaultDetailsContent(Component... components) {
    super(components);
    setSizeFull();
    setOverflow(Overflow.SCROLL);
  }

  public void setContent( Component... components ) {
    removeAll();
    add( components );
  }
}
