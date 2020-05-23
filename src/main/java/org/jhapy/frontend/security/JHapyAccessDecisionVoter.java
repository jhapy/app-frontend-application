package org.jhapy.frontend.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.SecurityRole;
import org.jhapy.frontend.client.security.SecurityRoleService;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-20
 */
@Component
public class JHapyAccessDecisionVoter implements AccessDecisionVoter, HasLogger {

  private final SecurityRoleService securityRoleService;
  private List<String> allowedRoles;

  public JHapyAccessDecisionVoter(
      SecurityRoleService securityRoleService) {
    this.securityRoleService = securityRoleService;

  }

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  public int vote(Authentication authentication, Object object, Collection collection) {
    String loggerPrefix = getLoggerPrefix("vote");

    int result = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(getAllowedRoles()::contains)
        .findAny()
        .map(s -> ACCESS_GRANTED)
        .orElse(ACCESS_ABSTAIN);

    logger().trace(loggerPrefix + "Result = " + (result == ACCESS_GRANTED ? " Access Granted"
        : " Abstain voting"));

    return result;
  }

  protected List<String> getAllowedRoles() {
    if (allowedRoles == null) {
      allowedRoles = securityRoleService.getAllowedLoginRoles().getData().stream().map(
          SecurityRole::getName).collect(
          Collectors.toList());
    }

    return allowedRoles;
  }

  @Override
  public boolean supports(Class clazz) {
    return true;
  }
}