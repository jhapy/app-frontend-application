package org.jhapy.frontend.utils.i18n;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-18
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface I18NPageTitle {

  String messageKey() default "";

  String defaultValue() default "";

  Class<? extends TitleFormatter> formatter() default DefaultTitleFormatter.class;
}
