package org.jhapy.frontend.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.client.audit.SessionService;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-24
 */
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements HasLogger {

  private final SessionService sessionService;

  public LogoutSuccessHandler(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    String loggerPrefix = getLoggerPrefix("onLogoutSuccess");
    logger().debug(loggerPrefix + "Logout !");
    super.onLogoutSuccess(request, response, authentication);
  }
}
