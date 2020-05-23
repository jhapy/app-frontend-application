package org.jhapy.frontend.exceptions;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public class AccessDeniedException extends RuntimeException {

  public AccessDeniedException() {
  }

  public AccessDeniedException(String message) {
    super(message);
  }
}
