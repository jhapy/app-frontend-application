package org.jhapy.frontend.renderer;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;
import org.jhapy.frontend.utils.AppConst;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public class BooleanRenderer<SOURCE> extends ComponentRenderer<Image, SOURCE> {

  protected ValueProvider<SOURCE, Boolean> valueProvider;

  public BooleanRenderer(ValueProvider<SOURCE, Boolean> valueProvider) {
    this.valueProvider = valueProvider;
  }

  public Image createComponent(SOURCE item) {
    Image image = new Image();

    Boolean val = valueProvider.apply(item);

    if (val != null && val) {
      image.setSrc(AppConst.ICON_OK);
    } else if (val != null) {
      image.setSrc(AppConst.ICON_KO);
    } else {
      image.setSrc(AppConst.ICON_BLANK);
    }

    return image;
  }
}