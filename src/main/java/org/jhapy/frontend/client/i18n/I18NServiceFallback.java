package org.jhapy.frontend.client.i18n;

import feign.hystrix.FallbackFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;

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
          : "Unknown error occurred : " + cause.toString();
      // I don't see this log statement
      logger().debug("Client fallback called for the cause : {}", errMessage);
    }
    return new I18NServiceFallback(cause);
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
