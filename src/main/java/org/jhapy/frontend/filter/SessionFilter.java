package org.jhapy.frontend.filter;

import com.hazelcast.core.HazelcastInstance;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.jhapy.commons.security.SecurityUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.frontend.utils.SessionInfo;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 17/09/2020
 */
@Component
@Order(2)
public class SessionFilter implements Filter, HasLogger {

  private final HazelcastInstance hazelcastInstance;

  public SessionFilter(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String sessionId = ((HttpServletRequest) request).getSession().getId();
    if (sessionId != null) {
      SessionInfo sessionInfo = retrieveMap().getOrDefault(sessionId, null);
      if (sessionInfo != null) {
        sessionInfo.setLastContact(LocalDateTime.now());
        retrieveMap().replace(sessionId, sessionInfo);
      }
    }
    chain.doFilter(request, response);
  }

  private ConcurrentMap<String, SessionInfo> retrieveMap() {
    return hazelcastInstance.getMap("userSessions");
  }

}
