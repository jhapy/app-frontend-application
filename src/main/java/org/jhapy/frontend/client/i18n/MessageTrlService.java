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

package org.jhapy.frontend.client.i18n;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Collections;
import java.util.List;
import org.jhapy.dto.domain.i18n.MessageTrl;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.messageTrl.CountByMessageQuery;
import org.jhapy.dto.serviceQuery.i18n.messageTrl.FindByMessageQuery;
import org.jhapy.frontend.client.AuthorizedFeignClient;
import org.jhapy.frontend.client.RemoteServiceHandler;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@AuthorizedFeignClient(name = "${jhapy.remote-services.i18n-server.name:null}", url = "${jhapy.remote-services.i18n-server.url:}", path = "/api/messageTrlService")
@Primary
public interface MessageTrlService extends RemoteServiceHandler {

  @PostMapping(value = "/findByMessage")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findByMessageFallback")
  ServiceResult<List<MessageTrl>> findByMessage(@RequestBody FindByMessageQuery query);

  default ServiceResult<List<MessageTrl>> findByMessageFallback(FindByMessageQuery query,
      Exception e) {
    return defaultFallback(getLoggerPrefix("findByMessageFallback"), e, Collections.emptyList());
  }

  @PostMapping(value = "/countByMessage")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "countByMessageFallback")
  ServiceResult<Long> countByMessage(@RequestBody CountByMessageQuery query);

  default ServiceResult<Long> countByMessageFallback(CountByMessageQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("countByMessageFallback"), e, 0L);
  }

  @PostMapping(value = "/findByIso3")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findByIso3Fallback")
  ServiceResult<List<MessageTrl>> findByIso3(@RequestBody FindByIso3Query query);

  default ServiceResult<List<MessageTrl>> findByIso3Fallback(FindByIso3Query query, Exception e) {
    return defaultFallback(getLoggerPrefix("findByIso3Fallback"), e, Collections.emptyList());
  }

  @PostMapping(value = "/getByNameAndIso3")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByNameAndIso3Fallback")
  ServiceResult<MessageTrl> getByNameAndIso3(@RequestBody GetByNameAndIso3Query query);

  default ServiceResult<MessageTrl> getByNameAndIso3Fallback(
      @RequestBody GetByNameAndIso3Query query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByNameAndIso3Fallback"), e, null);
  }

  @PostMapping(value = "/getById")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByIdFallback")
  ServiceResult<MessageTrl> getById(@RequestBody GetByIdQuery query);

  default ServiceResult<MessageTrl> getByIdFallback(GetByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByIdFallback"), e, null);
  }

  @PostMapping(value = "/save")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "saveFallback")
  ServiceResult<MessageTrl> save(@RequestBody SaveQuery<MessageTrl> query);

  default ServiceResult<MessageTrl> saveFallback(SaveQuery<MessageTrl> query, Exception e) {
    return defaultFallback(getLoggerPrefix("saveFallback"), e, null);
  }

  @PostMapping(value = "/delete")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "deleteFallback")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);

  default ServiceResult<Void> deleteFallback(DeleteByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("deleteFallback"), e, null);
  }
}
