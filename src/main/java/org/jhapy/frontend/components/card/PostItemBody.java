package org.jhapy.frontend.components.card;

import com.github.appreciated.card.content.VerticalCardComponentContainer;
import com.github.appreciated.card.label.PrimaryLabelComponent;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-07-13
 */
public class PostItemBody extends VerticalCardComponentContainer {

  private final PrimaryLabelComponent primaryLabel;
  private final SecondaryLabelHtmlComponent secondaryLabel;

  public PostItemBody(String title, String description) {
    primaryLabel = new PrimaryLabelComponent(title);
    secondaryLabel = new SecondaryLabelHtmlComponent(description);
    add(primaryLabel, secondaryLabel);
    setTheme();
  }

  public PostItemBody withWhiteSpaceNoWrap() {
    primaryLabel.setWhiteSpaceNoWrap();
    secondaryLabel.setWhiteSpaceNoWrap();
    return this;
  }

  public void setTheme() {
    getElement().setAttribute("theme", "spacing-xs");
  }

  public PrimaryLabelComponent getPrimaryLabel() {
    return primaryLabel;
  }

  public SecondaryLabelHtmlComponent getSecondaryLabel() {
    return secondaryLabel;
  }
}
