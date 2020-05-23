package org.jhapy.frontend.utils.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import java.util.Locale;
import org.rapidpm.frp.functions.CheckedTriFunction;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-18
 */
public interface TitleFormatter extends CheckedTriFunction<I18NProvider, Locale, String, String> {

}
