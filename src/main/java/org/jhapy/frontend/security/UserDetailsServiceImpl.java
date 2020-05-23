package org.jhapy.frontend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.serviceQuery.security.securityUser.GetSecurityUserByUsernameQuery;
import org.jhapy.frontend.client.security.SecurityUserService;

/**
 * Implements the {@link UserDetailsService}.
 *
 * This implementation searches for {@link User} entities by the e-mail address supplied in the
 * login screen.
 *
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
@Service
@Primary
public class UserDetailsServiceImpl implements UserDetailsService {

  private final SecurityUserService securityUserService;

  @Autowired
  public UserDetailsServiceImpl(SecurityUserService securityUserService) {
    this.securityUserService = securityUserService;
  }

  /**
   * Recovers the {@link SecurityUser} from the database using the e-mail address supplied in the
   * login screen. If the user is found, returns a {@link org.springframework.security.core.userdetails.User}.
   *
   * @param username User's e-mail address
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    SecurityUser securityUser = securityUserService
        .getSecurityUserByUsername(new GetSecurityUserByUsernameQuery(username)).getData();

    if (securityUser != null) {
      return securityUser;
    } else {
      throw new UsernameNotFoundException("No user present with username: " + username);
    }
  }
}