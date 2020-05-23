package org.jhapy.frontend.components.card;

import com.github.appreciated.card.content.HorizontalCardComponentContainer;
import com.github.appreciated.card.label.PrimaryLabelComponent;
import com.vaadin.flow.component.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-13
 */
public class PostItem extends
    HorizontalCardComponentContainer<com.github.appreciated.card.content.Item> {

  private Component component;

  public PostItem(String title, String description) {
    component = new PostItemBody(title, description);
    ((PostItemBody) component).setPadding(false);
    add(component);
  }

  public PostItem withWhiteSpaceNoWrap() {
    if (component instanceof PrimaryLabelComponent) {
      ((PrimaryLabelComponent) component).setWhiteSpaceNoWrap();
    } else if (component instanceof PostItemBody) {
      ((PostItemBody) component).withWhiteSpaceNoWrap();
    }
    return this;
  }

}
