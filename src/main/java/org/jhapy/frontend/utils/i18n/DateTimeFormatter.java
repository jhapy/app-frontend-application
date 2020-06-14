/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    if (localDate == null) {
      return "";
    } else {
      return localDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM"));
    }
  }

  public static String format(LocalDate localDate) {
    if (localDate == null) {
      return "";
    } else {
      return localDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.YYYY"));
    }
  }

  public static String format(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return "";
    } else {
      return localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.YYYY HH:mm"));
    }
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
