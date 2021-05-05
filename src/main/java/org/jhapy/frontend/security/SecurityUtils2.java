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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils2 {

  private SecurityUtils2() {
  }

  /**
   * Get the login of the current user.
   *
   * @return the login of the current user.
   */
  public static Optional<String> getCurrentUserLogin() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
  }

  public static Optional<String> getCurrentUserPicture() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();
    if (authentication.getPrincipal() instanceof DefaultOidcUser) {
      Map<String, Object> attributes = ((DefaultOidcUser) authentication.getPrincipal())
          .getAttributes();
      if (attributes.containsKey("picture")) {
        return Optional.ofNullable((String) attributes.get("picture"));
      }
    }
    return Optional.empty();
  }

  private static String extractPrincipal(Authentication authentication) {
    if (authentication == null) {
      return null;
    } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
      return springSecurityUser.getUsername();
    } else if (authentication instanceof JwtAuthenticationToken) {
      return (String) ((JwtAuthenticationToken) authentication).getToken().getClaims()
          .get("preferred_username");
    } else if (authentication.getPrincipal() instanceof DefaultOidcUser) {
      Map<String, Object> attributes = ((DefaultOidcUser) authentication.getPrincipal())
          .getAttributes();
      if (attributes.containsKey("preferred_username")) {
        return (String) attributes.get("preferred_username");
      }
    } else if (authentication.getPrincipal() instanceof String) {
      return (String) authentication.getPrincipal();
    }
    return null;
  }


  /**
   * Check if a user is authenticated.
   *
   * @return true if the user is authenticated, false otherwise.
   */
  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null &&
        getAuthorities(authentication).noneMatch(AuthoritiesConstants.ANONYMOUS::equals);
  }

  /**
   * If the current user has a specific authority (security role).
   * <p>
   * The name of this method comes from the {@code isUserInRole()} method in the Servlet API.
   *
   * @param authority the authority to check.
   * @return true if the current user has the authority, false otherwise.
   */
  public static boolean isCurrentUserInRole(String authority) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null &&
        getAuthorities(authentication).anyMatch(authority::equals);
  }

  private static Stream<String> getAuthorities(Authentication authentication) {
    Collection<? extends GrantedAuthority> authorities =
        authentication instanceof JwtAuthenticationToken ?
            extractAuthorityFromClaims(
                ((JwtAuthenticationToken) authentication).getToken().getClaims())
            : authentication.getAuthorities();
    return authorities.stream()
        .map(GrantedAuthority::getAuthority);
  }

  public static List<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
    return mapRolesToGrantedAuthorities(getRolesFromClaims(claims));
  }

  @SuppressWarnings("unchecked")
  private static Collection<String> getRolesFromClaims(Map<String, Object> claims) {
    final Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");

    return (Collection<String>) claims.getOrDefault("groups",
        claims.getOrDefault("roles", realmAccess.getOrDefault("roles", new ArrayList<>())));
  }

  private static List<GrantedAuthority> mapRolesToGrantedAuthorities(Collection<String> roles) {
    return roles.stream()
        .filter(role -> role.startsWith("ROLE_"))
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }
}
