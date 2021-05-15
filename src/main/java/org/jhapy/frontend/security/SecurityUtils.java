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

package org.jhapy.frontend.security;

import static org.jhapy.frontend.utils.AppConst.SECURITY_USER_ATTRIBUTE;
import static org.jhapy.frontend.utils.AppConst.SECURITY_USER_ID_ATTRIBUTE;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import com.vaadin.flow.server.ServletHelper.RequestType;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.ApplicationConstants;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.messageQueue.EndSession;
import org.jhapy.dto.messageQueue.NewSession;
import org.jhapy.frontend.annotations.PublicView;
import org.jhapy.frontend.client.audit.AuditServices;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

/**
 * SecurityUtils takes care of all such static operations that have to do with security and querying
 * rights from different beans of the UI.
 *
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public final class SecurityUtils {

  public static final String SESSION_USERNAME = "username";
  private static final String COOKIE_NAME = "remember-me";


  private SecurityUtils() {
    // Util methods only
  }

  /**
   * Gets the user name of the currently signed in user.
   *
   * @return the user name of the current user or <code>null</code> if the user has not signed in
   */
  public static String getUsername() {
    SecurityContext context = SecurityContextHolder.getContext();
    if (!isUserLoggedIn()) {
      return "Anonymous";
    }
    Object principal = context.getAuthentication().getPrincipal();
    if (principal instanceof UserDetails) {
      UserDetails userDetails = (UserDetails) context.getAuthentication().getPrincipal();
      return userDetails.getUsername();
    }
    // Anonymous or no authentication.
    return "Anonymous";
  }

  public static SecurityUser getSecurityUser() {
    SecurityContext context = SecurityContextHolder.getContext();
    if (!isUserLoggedIn()) {
      return null;
    }
    if (VaadinSession.getCurrent() != null && VaadinSession.getCurrent().getAttribute(SECURITY_USER_ATTRIBUTE) != null) {
      return (SecurityUser) VaadinSession.getCurrent().getAttribute(SECURITY_USER_ATTRIBUTE);
    }
    Object principal = context.getAuthentication().getPrincipal();
    if (principal instanceof DefaultOidcUser) {
      Map<String, Object> attributes = ((DefaultOidcUser) principal).getAttributes();
      SecurityUser securityUser = new SecurityUser();
      securityUser.setEmail(attributes.get("email").toString());
      securityUser.setFirstName(attributes.get("given_name").toString());
      securityUser.setLastName(attributes.get("family_name").toString());
      securityUser.setUsername(attributes.get("preferred_username").toString());
      securityUser.setId(attributes.get("sub"));
      VaadinSession.getCurrent().setAttribute(SECURITY_USER_ATTRIBUTE, securityUser);
      VaadinSession.getCurrent().setAttribute(SECURITY_USER_ID_ATTRIBUTE, securityUser.getId());
      return securityUser;
    } else if (principal instanceof SecurityUser) {
      return (SecurityUser) principal;
    }
    // Anonymous or no authentication.
    return null;
  }

  public static SecurityUser getSecurityUser(Authentication authentication) {
    if (authentication == null) {
      return null;
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof DefaultOidcUser) {
      Map<String, Object> attributes = ((DefaultOidcUser) principal).getAttributes();
      SecurityUser securityUser = new SecurityUser();
      securityUser.setEmail(attributes.get("email").toString());
      securityUser.setFirstName(attributes.get("given_name").toString());
      securityUser.setLastName(attributes.get("family_name").toString());
      securityUser.setUsername(attributes.get("preferred_username").toString());
      return securityUser;
    } else if (principal instanceof SecurityUser) {
      return (SecurityUser) principal;
    }
    // Anonymous or no authentication.
    return null;
  }

  /**
   * Checks if access is granted for the current user for the given secured view, defined by the
   * view class.
   *
   * @param viewClass View class
   * @return true if access is granted, false otherwise.
   */
  public static boolean isAccessGranted(Class<?> viewClass) {
    // Always allow access to public views
    PublicView publicView = AnnotationUtils.findAnnotation(viewClass, PublicView.class);
    if (publicView != null) {
      return true;
    }

    Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();

    // All other views require authentication
    if (!isUserLoggedIn(userAuthentication)) {
      return false;
    }

    // Allow if no roles are required.
    Secured secured = AnnotationUtils.findAnnotation(viewClass, Secured.class);
    if (secured == null) {
      return true;
    }

    List<String> allowedRoles = Arrays.asList(secured.value());
    return userAuthentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .anyMatch(allowedRoles::contains);
  }

  public static boolean hasRole(String role) {
    Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();

    // All other views require authentication
    if (!isUserLoggedIn(userAuthentication)) {
      return false;
    }

    return userAuthentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .anyMatch(s -> s.equals(role));
  }

  /**
   * Checks if the user is logged in.
   *
   * @return true if the user is logged in. False otherwise.
   */
  public static boolean isUserLoggedIn() {
    return isUserLoggedIn(SecurityContextHolder.getContext().getAuthentication());
  }

  private static boolean isUserLoggedIn(Authentication authentication) {
    return (authentication != null
        && !(authentication instanceof AnonymousAuthenticationToken));
  }

  /**
   * Tests if the request is an internal framework request. The test consists of checking if the
   * request parameter is present and if its value is consistent with any of the request types
   * know.
   *
   * @param request {@link HttpServletRequest}
   * @return true if is an internal framework request. False otherwise.
   */
  static public boolean isFrameworkInternalRequest(HttpServletRequest request) {
    final String parameterValue = request
        .getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
    return parameterValue != null
        && Stream.of(RequestType.values())
        .anyMatch(r -> r.getIdentifier().equals(parameterValue));
  }

  public static void newSession(SecurityUser securityUser) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(securityUser, null,
            securityUser.getAuthorities()));

    VaadinSession.getCurrent().getSession()
        .setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    VaadinSession.getCurrent().setAttribute(SECURITY_USER_ATTRIBUTE, securityUser);

    AuditServices.getAuditServiceQueue().newSession(
        new NewSession(VaadinRequest.getCurrent().getWrappedSession().getId(),
            securityUser.getUsername(), VaadinRequest.getCurrent().getRemoteAddr(),
            Instant.now(),
            true, null));
  }

  public static void endSession(String jSessionId) {
    AuditServices.getAuditServiceQueue().endSession(new EndSession(jSessionId, Instant.now()));
  }

  private static Optional<Cookie> getRememberMeCookie() {
    if (VaadinService.getCurrentRequest() == null) {
      return Optional.empty();
    }

    Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
    if (cookies != null) {
      return Arrays.stream(cookies).filter(c -> c.getName().equals(COOKIE_NAME)).findFirst();
    }

    return Optional.empty();
  }

  public static List<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
    return mapRolesToGrantedAuthorities(getRolesFromClaims(claims));
  }

  @SuppressWarnings("unchecked")
  private static Collection<String> getRolesFromClaims(Map<String, Object> claims) {
    return (Collection<String>) claims.getOrDefault("groups",
        claims.getOrDefault("roles", new ArrayList<>()));
  }

  private static List<GrantedAuthority> mapRolesToGrantedAuthorities(Collection<String> roles) {
    return roles.stream()
        .filter(role -> role.startsWith("ROLE_"))
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }
}
