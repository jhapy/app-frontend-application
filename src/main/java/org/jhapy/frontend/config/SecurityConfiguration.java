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

package org.jhapy.frontend.config;

import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jhapy.commons.security.oauth2.AudienceValidator;
import org.jhapy.commons.security.oauth2.JwtGrantedAuthorityConverter;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.client.security.SecurityRoleService;
import org.jhapy.frontend.client.security.keycloak.KeycloakLogoutHandler;
import org.jhapy.frontend.client.security.keycloak.KeycloakOauth2UserService;
import org.jhapy.frontend.security.CustomRequestCache;
import org.jhapy.frontend.security.JHapyAccessDecisionVoter;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.utils.AttributeContextListener;
import org.jhapy.frontend.views.JHapyMainView3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Order(100)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter implements HasLogger {

  @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
  private String issuerUri;

  private final AppProperties appProperties;
  private final SecurityProblemSupport problemSupport;
  private final SecurityRoleService securityRoleService;
  private final String realm;
  private final boolean forceHttpsForRealm;
  private final ClientRegistrationRepository clientRegistrationRepository;

  @Autowired
  private KeycloakOauth2UserService keycloakOidcUserService;

  private final RestTemplate restTemplate = new RestTemplate();

  public SecurityConfiguration(AppProperties appProperties,
      SecurityProblemSupport problemSupport,
      SecurityRoleService securityRoleService,
      ClientRegistrationRepository clientRegistrationRepository) {
    this.problemSupport = problemSupport;
    this.appProperties = appProperties;
    this.securityRoleService = securityRoleService;
    this.clientRegistrationRepository = clientRegistrationRepository;
    this.realm = appProperties.getSecurity().getRealm();
    this.forceHttpsForRealm = appProperties.getAuthorization().getForceRealmToHttps();
  }

  @Override
  public void configure(WebSecurity web) {
    web.ignoring().antMatchers(
        // Vaadin Flow static resources
        "/VAADIN/**",
        // the standard favicon URI
        "/favicon.ico",

        // the robots exclusion standard
        "/robots.txt",

        // web application manifest
        "/manifest.webmanifest",
        "/sw.js",
        "/offline-page.html",

        // icons and images
        "/icons/**",
        "/images/**",

        // (development mode) static resources
        "/frontend/**",

        // (development mode) webjars
        "/webjars/**",

        // (development mode) H2 debugging console
        "/h2-console/**",

        // (production mode) static resources
        "/frontend-es5/**", "/frontend-es6/**");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    var loggerPrefix = getLoggerPrefix("configure");
    // @formatter:off
    http
        .csrf()
        .disable()
        .headers()
        .contentSecurityPolicy(appProperties.getSecurity().getContentSecurityPolicy())
    .and()
        .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
    .and()
        .featurePolicy(appProperties.getSecurity().getFeaturePolicy())
    .and()
        .frameOptions()
        .deny()
    .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .and()
        .requestCache().requestCache(new CustomRequestCache())
    .and()
        .authorizeRequests()
        .requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll()
        .antMatchers("/").permitAll()
        .antMatchers("/api/auth-info").permitAll()
        .antMatchers("/api/**").authenticated()
        .antMatchers("/management/health").permitAll()
        .antMatchers("/management/health/**").permitAll()
        .antMatchers("/management/info").permitAll()
        .antMatchers("/management/prometheus").permitAll()
        .antMatchers("/management/**").hasAuthority("ROLE_ADMIN")
        .anyRequest().fullyAuthenticated()
    .and()
        .logout().addLogoutHandler(keycloakLogoutHandler()).logoutSuccessUrl("/")
    .and()
        .oauth2ResourceServer()
        .jwt()
        .jwtAuthenticationConverter(authenticationConverter())
    .and()
        .and()
        .oauth2Login().authorizationEndpoint()
        .authorizationRequestResolver(
            new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
                "/oauth2/authorization", forceHttpsForRealm)).and().userInfoEndpoint()
        .oidcUserService(keycloakOidcUserService).and()
        .loginPage(appProperties.getAuthorization().getLoginRootUrl()
            + DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + realm);
    // @formatter:on
    debug(loggerPrefix, "Using login root url : {0}{1}/{2}",
        appProperties.getAuthorization().getLoginRootUrl(), DEFAULT_AUTHORIZATION_REQUEST_BASE_URI,
        realm);
  }

  Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
    var jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter
        .setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthorityConverter());
    return jwtAuthenticationConverter;
  }

  @Bean
  public KeycloakOauth2UserService keycloakOidcUserService(
      OAuth2ClientProperties oauth2ClientProperties) {

    var jwtDecoder = (NimbusJwtDecoder) JwtDecoders
        .fromIssuerLocation(oauth2ClientProperties.getProvider().get("oidc").getIssuerUri());
    //NimbusJwtDecoderJwkSupport jwtDecoder = new NimbusJwtDecoderJwkSupport(oauth2ClientProperties.getProvider().get("keycloak").getJwkSetUri());

    var authoritiesMapper = new SimpleAuthorityMapper();
    authoritiesMapper.setConvertToUpperCase(true);

    return new KeycloakOauth2UserService(jwtDecoder, authoritiesMapper);
  }

  @Bean
  JwtDecoder jwtDecoder() {
    var jwtDecoder = (NimbusJwtDecoder) JwtDecoders
        .fromOidcIssuerLocation(issuerUri);

    var audienceValidator = new AudienceValidator(
        appProperties.getSecurity().getOauth2().getAudience());
    var withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
    var withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer,
        audienceValidator);

    jwtDecoder.setJwtValidator(withAudience);

    return jwtDecoder;
  }

  @Bean
  public KeycloakLogoutHandler keycloakLogoutHandler() {
    return new KeycloakLogoutHandler(new RestTemplate());
  }

  @Bean
  public AccessDecisionManager accessDecisionManager() {
    List<AccessDecisionVoter<?>> decisionVoters
        = Arrays.asList(
        new WebExpressionVoter(),
        new RoleVoter(),
        new AuthenticatedVoter(),
        new JHapyAccessDecisionVoter(securityRoleService));
    return new UnanimousBased(decisionVoters);
  }
}
