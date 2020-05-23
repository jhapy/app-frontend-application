package org.jhapy.frontend.security;

import java.io.IOException;
import java.time.Instant;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.messageQueue.NewSession;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.frontend.client.audit.AuditServiceQueue;
import org.jhapy.frontend.client.security.SecurityUserService;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-01
 */
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  private final SecurityUserService securityUserService;
  private final AuditServiceQueue auditServiceQueue;

  public AuthenticationSuccessHandler(
      SecurityUserService securityUserService, AuditServiceQueue auditServiceQueue) {
    this.securityUserService = securityUserService;
    this.auditServiceQueue = auditServiceQueue;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication authentication)
      throws ServletException, IOException {
    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
    securityUser.setLastSuccessfulLogin(Instant.now());
    securityUser.setIsAccountLocked(false);
    securityUser.setFailedLoginAttempts(0);
    securityUserService.save(new SaveQuery<>(securityUser));

    auditServiceQueue.newSession(
        new NewSession(request.getRequestedSessionId(), securityUser.getUsername(), null,
            Instant.now(), true, null));
    super.onAuthenticationSuccess(request, response, authentication);
  }
}