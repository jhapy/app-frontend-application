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

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-02
 */
@Component
public class I18NServiceFallback implements I18NService, HasLogger,
    FallbackFactory<I18NServiceFallback> {

  final Throwable cause;

  public I18NServiceFallback() {
    this(null);
  }

  I18NServiceFallback(Throwable cause) {
    this.cause = cause;
  }

  @Override
  public I18NServiceFallback create(Throwable cause) {
    if (cause != null) {
      String errMessage = StringUtils.isNotBlank(cause.getMessage()) ? cause.getMessage()
          : "Unknown error occurred : " + cause;
      // I don't see this log statement
      logger().debug("Client fallback called for the cause : {}", errMessage);
    }
    return new I18NServiceFallback(cause);
  }

  @Override
  public ServiceResult<List<String>> getExistingLanguages(BaseRemoteQuery query) {
    logger().error(getLoggerPrefix("getExistingLanguages") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", Collections.emptyList());
  }

  @Override
  public ServiceResult<Byte[]> getI18NFile(@RequestBody BaseRemoteQuery query) {
    logger().error(getLoggerPrefix("getI18NFile") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<Void> importI18NFile(@RequestBody ImportI18NFileQuery query) {
    logger().error(getLoggerPrefix("importI18NFile") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }
}
