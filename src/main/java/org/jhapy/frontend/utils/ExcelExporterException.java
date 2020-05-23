package org.jhapy.frontend.utils;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 09/04/2020
 */
public class ExcelExporterException extends RuntimeException {

  ExcelExporterException(String message) {
    super(message);
  }

  ExcelExporterException(String message, Exception e) {
    super(message, e);
  }
}
