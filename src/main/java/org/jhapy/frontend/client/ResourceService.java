package org.jhapy.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.utils.StoredFile;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-05-15
 */
@FeignClient(name = "${app.remote-services.resource-server.name:null}", url = "${app.remote-services.resource-server.url:}", path = "/resourceService", fallbackFactory = ResourceServiceFallback.class)
@Primary
public interface ResourceService {

  @PostMapping(value = "/save")
  ServiceResult<StoredFile> save(@RequestBody SaveQuery<StoredFile> query);

  @PostMapping(value = "/getById")
  ServiceResult<StoredFile> getById(@RequestBody GetByStrIdQuery query);

  @PostMapping(value = "/delete")
  ServiceResult<Void> delete(@RequestBody DeleteByStrIdQuery query);
}
