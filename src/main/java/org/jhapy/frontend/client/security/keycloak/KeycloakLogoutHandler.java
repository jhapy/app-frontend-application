package org.jhapy.frontend.client.security.keycloak;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jhapy.commons.utils.HasLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 07/05/2020
 */
@Component
@RequiredArgsConstructor
public class KeycloakLogoutHandler extends SecurityContextLogoutHandler implements HasLogger {

  private final RestTemplate restTemplate;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    super.logout(request, response, authentication);

    propagateLogoutToKeycloak((OidcUser) authentication.getPrincipal());
  }

  private void propagateLogoutToKeycloak(OidcUser user) {
String loggerPrefix = getLoggerPrefix("propagateLogoutToKeycloak");
    String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";

    UriComponentsBuilder builder = UriComponentsBuilder //
        .fromUriString(endSessionEndpoint) //
        .queryParam("id_token_hint", user.getIdToken().getTokenValue());

    ResponseEntity<String> logoutResponse = restTemplate.getForEntity(builder.toUriString(), String.class);
    if (logoutResponse.getStatusCode().is2xxSuccessful()) {
      logger().info(loggerPrefix+"Successfully logged out in Keycloak");
    } else {
      logger().info(loggerPrefix+"Could not propagate logout to Keycloak");
    }
  }
}
