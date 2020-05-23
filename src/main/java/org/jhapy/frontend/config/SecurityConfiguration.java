package org.jhapy.frontend.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.serviceQuery.security.securityUser.GetSecurityUserByUsernameQuery;
import org.jhapy.frontend.client.audit.AuditServiceQueue;
import org.jhapy.frontend.client.audit.SessionService;
import org.jhapy.frontend.client.security.SecurityRoleService;
import org.jhapy.frontend.client.security.SecurityUserService;
import org.jhapy.frontend.security.AuthenticationFailureHandler;
import org.jhapy.frontend.security.AuthenticationSuccessHandler;
import org.jhapy.frontend.security.CurrentUser;
import org.jhapy.frontend.security.CustomRequestCache;
import org.jhapy.frontend.security.LogoutSuccessHandler;
import org.jhapy.frontend.security.SecurityUtils;
import org.jhapy.frontend.security.UserDetailsServiceImpl;
import org.jhapy.frontend.security.JHapyAccessDecisionVoter;
import org.jhapy.frontend.utils.AppConst;

/**
 * Configures spring security, doing the following:
 * <li>Bypass security checks for static resources,</li>
 * <li>Restrict access to the application, allowing only logged in users,</li>
 * <li>Set up the login form,</li>
 * <li>Configures the {@link UserDetailsServiceImpl}.</li>
 *
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
//@EnableWebSecurity
//@Configuration
//@Order(100)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  private static final String LOGIN_PROCESSING_URL = "/login";
  private static final String LOGIN_FAILURE_URL = "/login?error";
  private static final String LOGIN_URL = "/login";
  private static final String LOGOUT_SUCCESS_URL = "/";

  @Autowired
  private SecurityRoleService securityRoleService;

  @Autowired
  private SecurityUserService securityUserService;

  @Autowired
  private SessionService sessionService;

  @Autowired
  private AuditServiceQueue auditServiceQueue;

  /**
   * The password encoder to use when encrypting passwords.
   */
  /*
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
*/
  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public CurrentUser currentUser() {
    final String username = SecurityUtils.getUsername();
    SecurityUser user =
        username != null ? securityUserService
            .getSecurityUserByUsername(new GetSecurityUserByUsernameQuery(username)).getData() :
            null;
    return () -> user;
  }

  /**
   * Require login to access internal pages and configure login form.
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Not using Spring CSRF here to be able to use plain HTML for the login page
    http.csrf().disable()

        // Register our CustomRequestCache, that saves unauthorized access attempts, so
        // the user is redirected after login.
        .requestCache().requestCache(new CustomRequestCache())

        // Restrict access to our application.
        .and().authorizeRequests()
        .antMatchers("/ping").permitAll()
        // Allow all flow internal requests.
        .requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll()

        // Public access to Places
        .antMatchers("/", "/places").permitAll()

        // Allow all requests by logged in users.
       // .accessDecisionManager(accessDecisionManager())
        .antMatchers("/login").hasAnyAuthority("user", "admin");
        // Configure the login page.

        /*
        .and()
        .formLogin().loginPage(LOGIN_URL).permitAll().loginProcessingUrl(LOGIN_PROCESSING_URL)
        .failureHandler(new AuthenticationFailureHandler(LOGIN_FAILURE_URL, securityUserService,
            sessionService))
        .successHandler(new AuthenticationSuccessHandler(securityUserService, auditServiceQueue))
        .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())
        // Configure logout
        .and().logout().logoutSuccessUrl(LOGOUT_SUCCESS_URL)
        .logoutSuccessHandler(new LogoutSuccessHandler(sessionService));
         */
  }
/*
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
*/
  /**
   * Allows access to static resources, bypassing Spring security.
   */
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
}
