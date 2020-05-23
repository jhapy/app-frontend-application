package org.jhapy.frontend.client.reference;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.dto.domain.reference.IntermediateRegionTrl;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.reference.intermediateRegionTrl.GetIntermediateRegionTrlQuery;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@FeignClient(name = "${app.remote-services.backend-server.name:null}", url = "${app.remote-services.backend-server.url:}", path = "/intermediateRegionTrlService", fallback = IntermediateRegionTrlServiceFallback.class)
@Primary
public interface IntermediateRegionTrlService {

  @PostMapping(value = "/getIntermediateRegionTrl")
  ServiceResult<IntermediateRegionTrl> getIntermediateRegionTrl(
      @RequestBody GetIntermediateRegionTrlQuery query);

  @PostMapping(value = "/getById")
  ServiceResult<IntermediateRegionTrl> getById(@RequestBody GetByIdQuery query);

  @PostMapping(value = "/save")
  ServiceResult<IntermediateRegionTrl> save(@RequestBody SaveQuery<IntermediateRegionTrl> query);

  @PostMapping(value = "/delete")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);
}
