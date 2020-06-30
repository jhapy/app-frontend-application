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

import feign.hystrix.FallbackFactory;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.i18n.ElementTrl;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.CountByElementQuery;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.FindByElementQuery;
import org.springframework.stereotype.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-02
 */
@Component
public class ElementTrlServiceFallback implements ElementTrlService, HasLogger,
    FallbackFactory<ElementTrlServiceFallback> {

  final Throwable cause;

  public ElementTrlServiceFallback() {
    this(null);
  }

  ElementTrlServiceFallback(Throwable cause) {
    this.cause = cause;
  }

  @Override
  public ElementTrlServiceFallback create(Throwable cause) {
    if (cause != null) {
      String errMessage = StringUtils.isNotBlank(cause.getMessage()) ? cause.getMessage()
          : "Unknown error occurred : " + cause.toString();
      // I don't see this log statement
      logger().debug("Client fallback called for the cause : {}", errMessage);
    }
    return new ElementTrlServiceFallback(cause);
  }

  @Override
  public ServiceResult<List<ElementTrl>> findByElement(FindByElementQuery query) {
    logger().error(getLoggerPrefix("findByElement") + "Cannot connect to the server");

    return new ServiceResult(false, "Cannot connect to server", Collections.emptyList());
  }

  @Override
  public ServiceResult<Long> countByElement(CountByElementQuery query) {
    logger().error(getLoggerPrefix("countByElement") + "Cannot connect to the server");

    return new ServiceResult(false, "Cannot connect to server", 0L);
  }

  @Override
  public ServiceResult<List<ElementTrl>> findByIso3(FindByIso3Query query) {
    logger().error(getLoggerPrefix("findByIso3") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", Collections.emptyList());
  }

  @Override
  public ServiceResult<ElementTrl> getByNameAndIso3(GetByNameAndIso3Query query) {
    logger().error(getLoggerPrefix("getByNameAndIso3") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<ElementTrl> getById(GetByIdQuery query) {
    logger().error(getLoggerPrefix("getById") + "Cannot connect to the server");

    return new ServiceResult(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<ElementTrl> save(SaveQuery<ElementTrl> query) {
    logger().error(getLoggerPrefix("save") + "Cannot connect to the server");

    return new ServiceResult(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<Void> delete(DeleteByIdQuery query) {
    logger().error(getLoggerPrefix("delete") + "Cannot connect to the server");

    return new ServiceResult(false, "Cannot connect to server", null);
  }
}
