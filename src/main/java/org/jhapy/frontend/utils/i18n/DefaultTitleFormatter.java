package org.jhapy.frontend.utils.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import java.util.Locale;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-18
 */
public class DefaultTitleFormatter implements TitleFormatter {

  @Override
  public String applyWithException(I18NProvider i18NProvider, Locale locale, String key)
      throws Exception {
    return i18NProvider.getTranslation(key, locale);
  }
}