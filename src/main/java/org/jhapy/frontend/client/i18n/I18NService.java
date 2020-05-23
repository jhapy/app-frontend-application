package org.jhapy.frontend.client.i18n;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.i18n.ImportI18NFileQuery;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@FeignClient(name = "${app.remote-services.i18n-server.name:null}", url = "${app.remote-services.i18n-server.url:}", path = "/i18NService", fallbackFactory = I18NServiceFallback.class)
@Primary
public interface I18NService {

  @PostMapping(value = "/getI18NFile")
  ServiceResult<Byte[]> getI18NFile(@RequestBody BaseRemoteQuery query);

  @PostMapping(value = "/importI18NFile")
  ServiceResult<Void> importI18NFile(@RequestBody ImportI18NFileQuery query);
}
