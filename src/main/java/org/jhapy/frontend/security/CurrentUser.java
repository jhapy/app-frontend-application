package org.jhapy.frontend.security;

import org.jhapy.dto.domain.security.SecurityUser;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
@FunctionalInterface
public interface CurrentUser {

  SecurityUser getUser();
}
