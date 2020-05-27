package org.jhapy.frontend.client.security.keycloak;

import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoderJwkSupport;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 07/05/2020
 */

@RequiredArgsConstructor
public class KeycloakOauth2UserService extends OidcUserService {

  private final OAuth2Error INVALID_REQUEST = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);

  private final JwtDecoder jwtDecoder;

  private final GrantedAuthoritiesMapper authoritiesMapper;

  /**
   * Augments {@link OidcUserService#loadUser(OidcUserRequest)} to add authorities
   * provided by Keycloak.
   *
   * Needed because {@link OidcUserService#loadUser(OidcUserRequest)} (currently)
   * does not provide a hook for adding custom authorities from a
   * {@link OidcUserRequest}.
   */
  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

    OidcUser user = super.loadUser(userRequest);

    Set<GrantedAuthority> authorities = new LinkedHashSet<>();
    authorities.addAll(user.getAuthorities());
    authorities.addAll(extractKeycloakAuthorities(userRequest));

    return new DefaultOidcUser(authorities, userRequest.getIdToken(), user.getUserInfo(), "preferred_username");
  }

  /**
   * Extracts {@link GrantedAuthority GrantedAuthorities} from the AccessToken in
   * the {@link OidcUserRequest}.
   *
   * @param userRequest
   * @return
   */
  private Collection<? extends GrantedAuthority> extractKeycloakAuthorities(OidcUserRequest userRequest) {

    Jwt token = parseJwt(userRequest.getAccessToken().getTokenValue());

    // Would be great if Spring Security would provide something like a plugable
    // OidcUserRequestAuthoritiesExtractor interface to hide the junk below...

    @SuppressWarnings("unchecked")
    Map<String, Object> resourceMap = (Map<String, Object>) token.getClaims().get("resource_access");
    String clientId = userRequest.getClientRegistration().getClientId();

    @SuppressWarnings("unchecked")
    Map<String, Map<String, Object>> clientResource = (Map<String, Map<String, Object>>) resourceMap.get(clientId);
    if (CollectionUtils.isEmpty(clientResource)) {
      return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    List<String> clientRoles = (List<String>) clientResource.get("roles");
    if (CollectionUtils.isEmpty(clientRoles)) {
      return Collections.emptyList();
    }

    Collection<? extends GrantedAuthority> authorities = AuthorityUtils
        .createAuthorityList(clientRoles.toArray(new String[0]));
    if (authoritiesMapper == null) {
      return authorities;
    }

    return authoritiesMapper.mapAuthorities(authorities);
  }

  private Jwt parseJwt(String accessTokenValue) {
    try {
      // Token is already verified by spring security infrastructure
      return jwtDecoder.decode(accessTokenValue);
    } catch (JwtException e) {
      throw new OAuth2AuthenticationException(INVALID_REQUEST, e);
    }
  }
}