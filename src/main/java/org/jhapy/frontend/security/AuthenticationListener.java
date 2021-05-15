package org.jhapy.frontend.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 15/05/2021
 */
public interface AuthenticationListener {
  void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication);
  void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception);
  void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication);
}
