package org.jhapy.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.jhapy.commons.utils.SpringProfileConstants;
import org.jhapy.frontend.aop.logging.LoggingAspect;

@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

  @Bean
  @Profile(SpringProfileConstants.SPRING_PROFILE_DEVELOPMENT)
  public LoggingAspect loggingAspect(Environment env) {
    return new LoggingAspect(env);
  }
}
