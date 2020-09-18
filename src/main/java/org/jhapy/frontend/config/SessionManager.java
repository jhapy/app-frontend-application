package org.jhapy.frontend.config;

import com.hazelcast.core.HazelcastInstance;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import org.jhapy.commons.security.SecurityUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.messageQueue.EndSession;
import org.jhapy.dto.messageQueue.NewSession;
import org.jhapy.frontend.client.audit.AuditServices;
import org.jhapy.frontend.utils.SessionInfo;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 17/09/2020
 */
@Configuration
@EnableScheduling
public class SessionManager implements VaadinServiceInitListener, HasLogger {

  private final HazelcastInstance hazelcastInstance;

  public SessionManager(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
  }

  private ConcurrentMap<String, SessionInfo> retrieveMap() {
    return hazelcastInstance.getMap("userSessions");
  }

  @Override
  public void serviceInit(ServiceInitEvent event) {
    final VaadinService vaadinService = event.getSource();

    vaadinService.addSessionDestroyListener(e -> {
      String loggerPrefix = getLoggerPrefix("sessionDestroy");

      Optional<String> currentSecurityUser = SecurityUtils.getCurrentUserLogin();

      logger().info(loggerPrefix + "Vaadin session destroyed. Current user is : " + (
          currentSecurityUser.orElse("Not set")));

      if (e.getSession() != null && e.getSession().getSession() != null) {
        logger().info(loggerPrefix + "End remote session");

        String sessionId = e.getSession().getSession().getId();
        retrieveMap().remove(sessionId);

        AuditServices.getAuditServiceQueue()
            .endSession(new EndSession(sessionId, Instant.now()));
      }
    });
  }

  // @Scheduled(fixedDelay = 60 * 1000)
  public void removeDeadSessions() {
    String loggerPrefix = getLoggerPrefix("removedDeadSessions");
    retrieveMap().forEach((s, sessionInfo) -> {
      if (sessionInfo.getLastContact().plusMinutes(2).isBefore(LocalDateTime.now())) {
        logger().info(loggerPrefix + "Remove dead session : " + sessionInfo);
        retrieveMap().remove(s);
      }
    });
  }
}
