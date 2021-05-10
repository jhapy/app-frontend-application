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
import org.jhapy.dto.domain.i18n.ActionTrl;
import org.jhapy.dto.domain.i18n.ElementTrl;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.CountByElementQuery;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.FindByElementQuery;
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
@AuthorizedFeignClient(name = "${jhapy.remote-services.i18n-server.name:null}", url = "${jhapy.remote-services.i18n-server.url:}", path = "/api/elementTrlService")
@Primary
public interface ElementTrlService  extends RemoteServiceHandler {

  @PostMapping(value = "/findByElement")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findByElementFallback")
  ServiceResult<List<ElementTrl>> findByElement(@RequestBody FindByElementQuery query);

  default ServiceResult<List<ElementTrl>> findByElementFallback(FindByElementQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("findByElementFallback"), e, Collections.emptyList());
  }

  @PostMapping(value = "/countByElement")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "countByElementFallback")
  ServiceResult<Long> countByElement(@RequestBody CountByElementQuery query);

  default ServiceResult<Long> countByElementFallback(CountByElementQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("countByElementFallback"), e, 0L);
  }

  @PostMapping(value = "/findByIso3")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findByIso3Fallback")
  ServiceResult<List<ElementTrl>> findByIso3(@RequestBody FindByIso3Query query);

  default ServiceResult<List<ElementTrl>> findByIso3Fallback(FindByIso3Query query, Exception e) {
    return defaultFallback(getLoggerPrefix("findByIso3Fallback"), e, Collections.emptyList());
  }

  @PostMapping(value = "/getByNameAndIso3")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByNameAndIso3Fallback")
  ServiceResult<ElementTrl> getByNameAndIso3(@RequestBody GetByNameAndIso3Query query);

  default ServiceResult<ElementTrl> getByNameAndIso3Fallback(GetByNameAndIso3Query query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByNameAndIso3Fallback"), e, null);
  }

  @PostMapping(value = "/getById")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByIdFallback")
  ServiceResult<ElementTrl> getById(@RequestBody GetByIdQuery query);

  default ServiceResult<ElementTrl> getByIdFallback(GetByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByIdFallback"), e, null);
  }

  @PostMapping(value = "/save")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "saveFallback")
  ServiceResult<ElementTrl> save(@RequestBody SaveQuery<ElementTrl> query);

  default ServiceResult<ElementTrl> saveFallback(SaveQuery<ElementTrl> query, Exception e) {
    return defaultFallback(getLoggerPrefix("saveFallback"), e, null);
  }

  @PostMapping(value = "/delete")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "deleteFallback")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);

  default ServiceResult<Void> deleteFallback(DeleteByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("deleteFallback"), e, null);
  }
}
