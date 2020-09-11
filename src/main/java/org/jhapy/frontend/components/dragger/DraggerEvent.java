package org.jhapy.frontend.components.dragger;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import lombok.ToString;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 02/08/2020
 */
@ToString(callSuper = false)
@DomEvent("dragger-newWidth")
public class DraggerEvent extends ComponentEvent<Dragger> {

  private final String width;

  public DraggerEvent(
      Dragger source, boolean fromClient, @EventData("event.detail.width") String width) {
    super(source, fromClient);
    this.width = width;
  }

  public String getWidth() {
    return width;
  }
}