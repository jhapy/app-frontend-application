package org.jhapy.frontend.client.reference;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.dto.domain.reference.RegionTrl;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.reference.regionTrl.GetRegionTrlQuery;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@FeignClient(name = "${app.remote-services.backend-server.name:null}", url = "${app.remote-services.backend-server.url:}", path = "/regionTrlService", fallback = RegionTrlServiceFallback.class)
@Primary
public interface RegionTrlService {

  @PostMapping(value = "/getRegionTrl")
  ServiceResult<RegionTrl> getRegionTrl(@RequestBody GetRegionTrlQuery query);

  @PostMapping(value = "/getById")
  ServiceResult<RegionTrl> getById(@RequestBody GetByIdQuery query);

  @PostMapping(value = "/save")
  ServiceResult<RegionTrl> save(@RequestBody SaveQuery<RegionTrl> query);

  @PostMapping(value = "/delete")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);
}
