package org.jhapy.frontend.utils.i18n;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.FormatStyle;
import java.util.Date;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-22
 */
public class DateTimeFormatter {

  public static String formatNoYear(LocalDate localDate) {
    if ( localDate == null )
      return "";
    else
      return localDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM"));
  }

  public static String format(LocalDate localDate) {
    if ( localDate == null )
      return "";
    else
      return localDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.YYYY"));
  }

  public static String format(LocalDateTime localDateTime) {
    if ( localDateTime == null )
      return "";
    else
      return localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm"));
  }

  public static String format(Instant instant) {
    return java.time.format.DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault()).format(instant);
  }

  public static String format(Date date) {
    return java.time.format.DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault()).format(date.toInstant());
  }
}
