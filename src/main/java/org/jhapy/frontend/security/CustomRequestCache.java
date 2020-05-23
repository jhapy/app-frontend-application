package org.jhapy.frontend.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

/**
 * HttpSessionRequestCache that avoids saving internal framework requests.
 *
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public class CustomRequestCache extends HttpSessionRequestCache {

  /**
   * {@inheritDoc}
   *
   * If the method is considered an internal request from the framework, we skip saving it.
   *
   * @see SecurityUtils#isFrameworkInternalRequest(HttpServletRequest)
   */
  @Override
  public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
    if (!SecurityUtils.isFrameworkInternalRequest(request)) {
      super.saveRequest(request, response);
    }
  }

}