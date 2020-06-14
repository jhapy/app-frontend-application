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

import feign.Logger.Level;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig extends RibbonAutoConfiguration {

  @Bean
  public Level feignLoggerLevel() {
    return Level.BASIC;
  }
/*
  @Bean
  public Decoder feignDecoder() {
    HttpMessageConverter jacksonConverter =
        new MappingJackson2HttpMessageConverter(customObjectMapper());
    ObjectFactory<HttpMessageConverters> objectFactory =
        () -> new HttpMessageConverters(jacksonConverter);
    return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
  }

  public ObjectMapper customObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    //objectMapper.registerModule(new Jackson2HalModule());

    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    return objectMapper;
  }

x
 */

}
