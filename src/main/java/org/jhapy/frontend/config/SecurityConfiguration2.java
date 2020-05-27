package org.jhapy.frontend.config;

import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.frontend.client.security.keycloak.KeycloakLogoutHandler;
import org.jhapy.frontend.client.security.keycloak.KeycloakOauth2UserService;
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
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.jhapy.frontend.client.security.SecurityRoleService;
import org.jhapy.frontend.security.AuthoritiesConstants;
import org.jhapy.frontend.security.CustomRequestCache;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.security.JHapyAccessDecisionVoter;
import org.jhapy.frontend.security.oauth2.AudienceValidator;
import org.jhapy.frontend.security.oauth2.JwtGrantedAuthorityConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Order(100)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration2 extends WebSecurityConfigurerAdapter implements HasLogger {

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    private final JHapyProperties appProperties;
    private final SecurityProblemSupport problemSupport;
    private final SecurityRoleService securityRoleService;
private final String realm;

@Autowired
private KeycloakOauth2UserService keycloakOidcUserService;

  private RestTemplate restTemplate  = new RestTemplate();

    public SecurityConfiguration2(JHapyProperties appProperties,
        SecurityProblemSupport problemSupport,
        SecurityRoleService securityRoleService,
        @Value("${kc.realm}") String realm) {
        this.problemSupport = problemSupport;
        this.appProperties = appProperties;
        this.securityRoleService = securityRoleService;
      this.realm = realm;
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
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
            .headers()
            .frameOptions()
            .disable()
            .and()
            .csrf()
            .disable()
            .authorizeRequests()
            .requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll()
            .anyRequest().fullyAuthenticated()
            .and()
            .logout().addLogoutHandler(new LogoutHandler() {
          @Override
          public void logout(HttpServletRequest request, HttpServletResponse response,
              Authentication authentication) {

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
        ).and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(authenticationConverter())
            .and().and()
            .oauth2Login().userInfoEndpoint().oidcUserService(keycloakOidcUserService).and()
            // I don't want a page with different clients as login options
            // So i use the constant from OAuth2AuthorizationRequestRedirectFilter
            // plus the configured realm as immediate redirect to Keycloak
            .loginPage(DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + realm);
    }
  Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthorityConverter());
    return jwtAuthenticationConverter;
  }
    @Bean
  public KeycloakOauth2UserService keycloakOidcUserService(
        OAuth2ClientProperties oauth2ClientProperties) {

    NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(oauth2ClientProperties.getProvider().get("keycloak").getIssuerUri());
    //NimbusJwtDecoderJwkSupport jwtDecoder = new NimbusJwtDecoderJwkSupport(oauth2ClientProperties.getProvider().get("keycloak").getJwkSetUri());

    SimpleAuthorityMapper authoritiesMapper = new SimpleAuthorityMapper();
    authoritiesMapper.setConvertToUpperCase(true);

    return new KeycloakOauth2UserService(jwtDecoder, authoritiesMapper);
  }

  @Bean
  JwtDecoder jwtDecoder() {
    NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

    OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(Arrays.asList("account"
        ,"api://default"));
    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

    jwtDecoder.setJwtValidator(withAudience);

    return jwtDecoder;
    }
  @Bean
  public KeycloakLogoutHandler keycloakLogoutHandler() {
    return new KeycloakLogoutHandler(new RestTemplate());
  }
    /*
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf()
            .disable()
            .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
        .and()
            .headers()
            .contentSecurityPolicy("default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
        .and()
            .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
        .and()
            .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
        .and()
            .frameOptions()
            .deny()
        .and()
            .requestCache().requestCache(new CustomRequestCache())
        .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .accessDecisionManager(accessDecisionManager())
            .requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll()
            .antMatchers("/VAADIN/**").permitAll()
            .antMatchers(        "/manifest.webmanifest").permitAll()
            .antMatchers("/sw.js").permitAll()
            .antMatchers("/icons/**").permitAll()
            .antMatchers("/images/**").permitAll()
            .antMatchers("/frontend/**").permitAll()
            .antMatchers("/webjars/**").permitAll()
            .antMatchers("/frontend-es5/**", "/frontend-es6/**").permitAll()
            .antMatchers("/offline-page.html").permitAll()
            .antMatchers("/api/auth-info").permitAll()
            .antMatchers("/api/**").authenticated()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/prometheus").permitAll()
            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .anyRequest().authenticated()
        .and()
            .oauth2Login();
        // @formatter:on
    }


    Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthorityConverter());
        return jwtAuthenticationConverter;
    }

    //@Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(appProperties.getSecurity().getOauth2().getAudience());
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

*/
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