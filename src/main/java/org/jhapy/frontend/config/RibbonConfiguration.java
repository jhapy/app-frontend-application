package org.jhapy.frontend.config;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AvailabilityFilteringRule;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-08-06
 */
public class RibbonConfiguration {

  @Bean
  public IPing ribbonPing(final IClientConfig config) {
    return new MyPingUrl();
  }

  @Bean
  public IRule ribbonRule(final IClientConfig config) {
    return new AvailabilityFilteringRule();
  }

}