package org.jhapy.frontend.components.card;

import com.github.appreciated.card.label.WhiteSpaceLabel;
import com.vaadin.flow.component.Html;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-13
 */
public class SecondaryLabelHtmlComponent extends Html implements WhiteSpaceLabel {

  public SecondaryLabelHtmlComponent(String text) {
    super(text);
    init();
  }

  private void init() {
    getElement().getStyle()
        .set("font-size", "var(--lumo-font-size-s)")
        .set("text-overflow", "ellipsis")
        .set("overflow", "scroll")
        .set("color", "var(--lumo-secondary-text-color)")
        .set("width", "100%")
        .set("min-height", "100px")
        .set("max-height", "200px");
  }

}

