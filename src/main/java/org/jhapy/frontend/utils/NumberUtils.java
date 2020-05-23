package org.jhapy.frontend.utils;

import java.text.DecimalFormat;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 10/8/19
 */
public class NumberUtils {

  private static DecimalFormat numberFormat = new DecimalFormat("#.0");

  public static String formatRangeNumner(double value) {
    return numberFormat.format(value);
  }
}
