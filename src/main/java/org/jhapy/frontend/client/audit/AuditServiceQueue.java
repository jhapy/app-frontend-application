package org.jhapy.frontend.client.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.jhapy.dto.messageQueue.EndSession;
import org.jhapy.dto.messageQueue.NewSession;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-15
 */
@Component
public class AuditServiceQueue {

  @Autowired
  JmsTemplate jmsTemplate;

  public void newSession(final NewSession newSession) {
    jmsTemplate.send("newSession", session -> session.createObjectMessage(newSession));
  }

  public void endSession(final EndSession endSession) {
    jmsTemplate.send("endSession", session -> session.createObjectMessage(endSession));
  }
}
