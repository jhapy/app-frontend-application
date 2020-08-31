package org.jhapy.frontend.components.search.overlay;

import com.github.appreciated.card.content.VerticalCardComponentContainer;
import com.github.appreciated.card.label.PrimaryLabelComponent;
import com.github.appreciated.card.label.SecondaryLabelComponent;
import com.vaadin.flow.component.Component;

public class ItemBodyMixed extends VerticalCardComponentContainer {
    private final PrimaryLabelComponent primaryLabel;
    private final Component secondaryComponent;

    public ItemBodyMixed(String title, Component description) {
        primaryLabel = new PrimaryLabelComponent(title);
        secondaryComponent = description;
        add(primaryLabel, secondaryComponent);
        setTheme();
    }

    public void setTheme() {
        getElement().setAttribute("theme", "spacing-xs");
    }

    public PrimaryLabelComponent getPrimaryLabel() {
        return primaryLabel;
    }

    public Component getSecondaryComponent() {
        return secondaryComponent;
    }
}
