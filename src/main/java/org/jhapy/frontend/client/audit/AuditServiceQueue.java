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

package org.jhapy.frontend.client.audit;

import org.jhapy.dto.messageQueue.EndSession;
import org.jhapy.dto.messageQueue.NewSession;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-15
 */
@Component
public class AuditServiceQueue {

  private final AmqpTemplate amqpTemplate;
  private final Queue newSessionQueue;
  private final Queue endSessionQueue;

  public AuditServiceQueue(AmqpTemplate amqpTemplate,
      @Qualifier("newSessionQueue") Queue newSessionQueue,
      @Qualifier("endSessionQueue") Queue endSessionQueue) {
    this.amqpTemplate = amqpTemplate;
    this.newSessionQueue = newSessionQueue;
    this.endSessionQueue = endSessionQueue;
  }

  public void newSession(final NewSession newSession) {
    amqpTemplate.convertAndSend(newSessionQueue.getName(), newSession);
  }

  public void endSession(final EndSession endSession) {
    amqpTemplate.convertAndSend(endSessionQueue.getName(), endSession);
  }
}
