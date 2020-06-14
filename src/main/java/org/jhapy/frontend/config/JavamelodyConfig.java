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

package org.jhapy.frontend.config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 11/19/19
 */
@Configuration
public class JavamelodyConfig implements HasLogger {

  private final Environment env;

  public JavamelodyConfig(Environment env) {
    this.env = env;
  }

  @PostConstruct
  public boolean init() {
    String loggerPrefix = getLoggerPrefix("init");

    String applicationName =
        env.getProperty("spring.application.name");

    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }
    String serverPort = env.getProperty("management.server.port");
    String contextPath = env.getProperty("server.servlet.context-path");
    if (StringUtils.isBlank(contextPath)) {
      contextPath = "/";
    }
    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      logger().warn(
          loggerPrefix + "The host name could not be determined, using `localhost` as fallback");
    }

    String localUrl = String.format("%s://user:none@%s:%s%sactuator", protocol,
        hostAddress,
        serverPort,
        contextPath);

    String collectUrl = env.getProperty("COLLECT_URL");
    logger().debug(loggerPrefix + "Application name = " + applicationName);
    logger().debug(loggerPrefix + "Collect URL = " + collectUrl);
    logger().debug(loggerPrefix + "Local URL = " + localUrl);

    if (collectUrl != null && localUrl != null) {
      try {
        URL collectServerUrl = new URL(collectUrl);
        URL applicationNodeUrl = new URL(localUrl);
        net.bull.javamelody.MonitoringFilter
            .registerApplicationNodeInCollectServer(applicationName, collectServerUrl,
                applicationNodeUrl);
      } catch (MalformedURLException e) {
        logger().error(loggerPrefix + "Malformed URL : " + e.getMessage(), e);
      }
    } else {
      logger().warn(loggerPrefix
          + "COLLECT_URL or LOCAL_URL missing, no automatic registering to collect server");
    }
    return true;
  }

  @PreDestroy
  public void destroy() {
    String loggerPrefix = getLoggerPrefix("destroy");
    try {
      net.bull.javamelody.MonitoringFilter.unregisterApplicationNodeInCollectServer();
    } catch (IOException e) {
      logger().error(
          loggerPrefix + "Error while un-registering from collect server : " + e.getMessage(), e);
    }
  }

}
