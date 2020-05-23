package org.jhapy.frontend.components;

import com.vaadin.flow.component.checkbox.Checkbox;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 11/04/2020
 */
public class CheckboxColumnComponent extends Checkbox {

  public CheckboxColumnComponent(boolean initialValue) {
    super(initialValue);
    setReadOnly(false);
  }
}
