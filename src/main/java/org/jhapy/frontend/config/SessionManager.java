package org.jhapy.frontend.config;

import com.hazelcast.core.HazelcastInstance;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentMap;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.messageQueue.EndSession;
import org.jhapy.dto.messageQueue.NewSession;
import org.jhapy.frontend.client.audit.AuditServices;
import org.jhapy.frontend.security.SecurityUtils;
import org.springframework.context.annotation.Configuration;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 17/09/2020
 */
@Configuration
public class SessionManager implements VaadinServiceInitListener, HasLogger {

  private final HazelcastInstance hazelcastInstance;

  public SessionManager(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
  }

  private ConcurrentMap<String, LocalDateTime> retrieveMap() {
    return hazelcastInstance.getMap("userSessions");
  }

  @Override
  public void serviceInit(ServiceInitEvent event) {
    final VaadinService vaadinService = event.getSource();

    vaadinService.addSessionDestroyListener(e -> {
      String loggerPrefix = getLoggerPrefix("sessionDestroy");

      SecurityUser currentSecurityUser = SecurityUtils.getSecurityUser();

      logger().info(loggerPrefix + "Vaadin session destroyed. Current user is : " + (
          currentSecurityUser != null ? currentSecurityUser.getEmail() : "Not set"));

      if (currentSecurityUser != null) {
        if (e.getSession() != null && e.getSession().getSession() != null) {
          logger().info(loggerPrefix + "End remote session");
          AuditServices.getAuditServiceQueue()
              .endSession(new EndSession(e.getSession().getSession().getId(), Instant.now()));
        }

        retrieveMap().remove(currentSecurityUser.getEmail());
      }
    });

  }
}
