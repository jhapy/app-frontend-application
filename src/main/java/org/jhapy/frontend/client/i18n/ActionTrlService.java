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
import org.jhapy.dto.domain.i18n.Action;
import org.jhapy.dto.domain.i18n.ActionTrl;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.actionTrl.CountByActionQuery;
import org.jhapy.dto.serviceQuery.i18n.actionTrl.FindByActionQuery;
import org.jhapy.dto.utils.Page;
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
@AuthorizedFeignClient(name = "${jhapy.remote-services.i18n-server.name:null}", url = "${jhapy.remote-services.i18n-server.url:}", path = "/api/actionTrlService")
@Primary
public interface ActionTrlService  extends RemoteServiceHandler {

  @PostMapping(value = "/findByAction")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findByActionFallback")
  ServiceResult<List<ActionTrl>> findByAction(@RequestBody FindByActionQuery query);

  default ServiceResult<List<ActionTrl>> findByActionFallback(FindByActionQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("findByActionFallback"), e, Collections.emptyList());
  }

  @PostMapping(value = "/countByAction")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "countByActionFallback")
  ServiceResult<Long> countByAction(@RequestBody CountByActionQuery query);

  default ServiceResult<Long> countByActionFallback(CountByActionQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("countByActionFallback"), e, 0L);
  }

  @PostMapping(value = "/findByIso3")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "findByIso3Fallback")
  ServiceResult<List<ActionTrl>> findByIso3(@RequestBody FindByIso3Query query);

  default ServiceResult<List<ActionTrl>> findByIso3Fallback(FindByIso3Query query, Exception e) {
    return defaultFallback(getLoggerPrefix("findByIso3Fallback"), e, Collections.emptyList());
  }

  @PostMapping(value = "/getByNameAndIso3")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByNameAndIso3Fallback")
  ServiceResult<ActionTrl> getByNameAndIso3(@RequestBody GetByNameAndIso3Query query);

  default ServiceResult<ActionTrl> getByNameAndIso3Fallback(GetByNameAndIso3Query query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByNameAndIso3Fallback"), e, null);
  }

  @PostMapping(value = "/getById")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getByIdFallback")
  ServiceResult<ActionTrl> getById(@RequestBody GetByIdQuery query);

  default ServiceResult<ActionTrl> getByIdFallback(GetByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getByIdFallback"), e, null);
  }

  @PostMapping(value = "/save")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "saveFallback")
  ServiceResult<ActionTrl> save(@RequestBody SaveQuery<ActionTrl> query);

  default ServiceResult<ActionTrl> saveFallback(SaveQuery<ActionTrl> query, Exception e) {
    return defaultFallback(getLoggerPrefix("saveFallback"), e, null);
  }

  @PostMapping(value = "/delete")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "deleteFallback")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);

  default ServiceResult<Void> deleteFallback(DeleteByIdQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("deleteFallback"), e, null);
  }
}
