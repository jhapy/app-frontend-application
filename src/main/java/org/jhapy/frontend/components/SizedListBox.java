package org.jhapy.frontend.components;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.listbox.ListBox;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-06
 */
public class SizedListBox<T> extends ListBox<T> implements HasSize {

  public SizedListBox() {
    super();
  }
}