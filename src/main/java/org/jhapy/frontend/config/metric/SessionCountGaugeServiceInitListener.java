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

package org.jhapy.frontend.config.metric;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import org.jhapy.commons.utils.HasLogger;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 30/05/2020
 */
@SpringComponent
public class SessionCountGaugeServiceInitListener implements VaadinServiceInitListener, HasLogger {


    private final MeterRegistry meterRegistry;

    public SessionCountGaugeServiceInitListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {

        final AtomicInteger sessionsCount = meterRegistry
            .gauge("vaadin.sessions", new AtomicInteger(0));

        final VaadinService vaadinService = event.getSource();

        vaadinService.addSessionInitListener(e -> {
            String loggerPrefix = getLoggerPrefix("sessionInit");
            logger().info(
                loggerPrefix + "New Vaadin session created. Current count is: " + sessionsCount
                    .incrementAndGet());
        });
        vaadinService.addSessionDestroyListener(e -> {
            String loggerPrefix = getLoggerPrefix("sessionDestroy");
            logger()
                .info(loggerPrefix + "Vaadin session destroyed. Current count is: " + sessionsCount
                    .decrementAndGet());
        });
    }
}

